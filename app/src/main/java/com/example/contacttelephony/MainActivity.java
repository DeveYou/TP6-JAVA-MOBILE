package com.example.contacttelephony;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;

import com.example.contacttelephony.adapter.ContactAdapter;
import com.example.contacttelephony.controllers.ContactController;
import com.example.contacttelephony.entities.Contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ContactPrefs";
    private static final String CONTACTS_LOADED_KEY = "contactsLoaded";
    private ContactController contactController = null;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private List<Contact> arrayList = new ArrayList<>();
    private RecyclerView listView;
    private AppCompatButton addButton;
    private Toolbar toolbar;
    private ContactAdapter adapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(contactController == null){
            contactController = new ContactController();
            Log.d("ADDCONTACT", "ADDCONTACTS INITIALIZED SUCCESSFULLY");
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = findViewById(R.id.listView);
        addButton = findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.activity_add_contact, null);
                builder.setView(dialogView);

                EditText nameInput = dialogView.findViewById(R.id.contact_name);
                EditText numberInput = dialogView.findViewById(R.id.contact_number);
                AppCompatButton addBtn = dialogView.findViewById(R.id.add_button);

                AlertDialog dialog = builder.create();

                addBtn.setOnClickListener(v1 -> {
                    String name = nameInput.getText().toString().trim();
                    String number = numberInput.getText().toString().trim();

                    if (name.isEmpty() || number.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Contact contact = new Contact(name, number);
                    Log.d("DEBUG", "Sending Contact -> name: " + contact.getName() + ", number: " + contact.getNumber());

                    contactController.create(contact, new ContactController.ContactCreateCallback() {
                        @Override
                        public void onSuccess(Contact contact) {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Contact added successfully", Toast.LENGTH_SHORT).show();
                                arrayList.add(contact);
                                adapter.updateContacts(arrayList);
                                dialog.dismiss();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Failed to add contact: " + error, Toast.LENGTH_SHORT).show();
                                Log.e("AddContact", error);
                            });
                        }
                    });
                    dialog.dismiss();
                });

                dialog.show();
            }
        });

        adapter = new ContactAdapter(arrayList, this);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        // Check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            // Permission already granted, load contacts
            getAndLoadNumbers();
        }

    }

    public void getAndLoadNumbers() {
        ContentResolver contentResolver = getContentResolver();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean contactsLoaded = prefs.getBoolean(CONTACTS_LOADED_KEY, false);

            Log.d("MAINCONTACT", "GET NUMBER ACTIVATED");

            Cursor phones = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );

            if (phones != null) {
                List<Contact> tempList = new ArrayList<>();
                Set<String> uniqueNumbers = new HashSet<>();

                try {
                    while (phones.moveToNext()) {
                        int nameIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        int numberIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                        if (nameIndex >= 0 && numberIndex >= 0) {
                            String name = phones.getString(nameIndex);
                            String number = phones.getString(numberIndex);

                            if (uniqueNumbers.contains(number)) continue;

                            uniqueNumbers.add(number);
                            Contact contact = new Contact(name, number);
                            Log.d("MAINNUMBER", "GET NUMBER: " + contact.getNumber());
                            tempList.add(contact);
                        } else {
                            Log.e("ContactsError", "Column index not found");
                        }
                    }

                    // Upload all contacts if not yet uploaded
                    if (!contactsLoaded && !tempList.isEmpty()) {
                        contactController.createAll(tempList, new ContactController.ContactBulkCreateCallback() {
                            @Override
                            public void onSuccess(List<Contact> contacts) {
                                mainHandler.post(() -> {
                                    Toast.makeText(MainActivity.this, "All contacts added successfully!", Toast.LENGTH_SHORT).show();
                                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                            .edit()
                                            .putBoolean(CONTACTS_LOADED_KEY, true)
                                            .apply();
                                });
                            }

                            @Override
                            public void onFailure(String error) {
                                mainHandler.post(() ->
                                        Toast.makeText(MainActivity.this, "Failed to upload contacts: " + error, Toast.LENGTH_LONG).show()
                                );
                            }
                        });
                    }

                    // Update UI list
                    mainHandler.post(() -> {
                        arrayList.clear();
                        arrayList.addAll(tempList);
                        adapter.updateContacts(arrayList);
                    });

                } finally {
                    phones.close();
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAndLoadNumbers();
            } else {
                Toast.makeText(this, "Permission requise pour lire les contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Search here...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("MainActivity", newText);
                if(adapter != null){
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });

        return true;
    }


}