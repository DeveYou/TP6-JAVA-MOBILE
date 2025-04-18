package com.example.contacttelephony.adapter;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacttelephony.MainActivity;
import com.example.contacttelephony.R;
import com.example.contacttelephony.entities.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements Filterable {
    private List<Contact> contacts;
    private LayoutInflater inflater;
    private NewFilter filter;
    private List<Contact> contactsFilter;
    private Context context;

    public ContactAdapter(List<Contact> contacts, Context context) {
        this.contacts = contacts;
        this.context = context;
        contactsFilter = new ArrayList<>(contacts);
        filter = new NewFilter(this);
    }

    @NonNull
    @Override
    public ContactAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ContactViewHolder holder, int position) {
        Contact contact = contactsFilter.get(position);

        //holder.id.setText(String.valueOf(contact.getId()));
        holder.name.setText(contact.getName());
        holder.number.setText(String.valueOf(contact.getNumber()));
        if(!contact.getName().isEmpty()){
            char first = contact.getName().toUpperCase().charAt(0);
            holder.firstLetter.setText(String.valueOf(first));
        }

        holder.itemView.setOnClickListener(v -> showContactOptionsDialog(contact));
    }


    private void showContactOptionsDialog(Contact contact){
        if(contact == null || context ==null ) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Contact: " + contact.getName());
        builder.setItems(new CharSequence[]{"Make a call", "Send a message"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + contact.getNumber()));
                        context.startActivity(callIntent);
                        break;
                    case 1:
                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                        smsIntent.setData(Uri.parse("smsto:" + contact.getNumber()));
                        smsIntent.putExtra("sms_body", "Bonjour !");
                        context.startActivity(smsIntent);
                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return contactsFilter.size();
    }

    @Override
    public NewFilter getFilter() {
        return filter;
    }

    public void updateContacts(List<Contact> newContacts) {
        this.contacts = newContacts;
        this.contactsFilter = new ArrayList<>(contacts);
        notifyDataSetChanged();
    }
    public class ContactViewHolder extends RecyclerView.ViewHolder{
        TextView id;
        TextView name;
        TextView number;
        TextView firstLetter;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            this.id = itemView.findViewById(R.id.id);
            this.name = itemView.findViewById(R.id.name);
            this.number = itemView.findViewById(R.id.number);
            this.firstLetter = itemView.findViewById(R.id.firstLetter);
        }
    }


    public class NewFilter extends Filter {
        public RecyclerView.Adapter myAdapter;

        public NewFilter(RecyclerView.Adapter myAdapter){
            super();
            this.myAdapter = myAdapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            contactsFilter.clear();
            final FilterResults results = new FilterResults();
            if(constraint.length() == 0){
                contactsFilter.addAll(contacts);
            }else{
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for(Contact contact : contacts){
                    if(contact.getName().toLowerCase().startsWith(filterPattern)){
                        contactsFilter.add(contact);
                    }
                }
            }
            results.values = contactsFilter;
            results.count = contactsFilter.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contactsFilter = (List<Contact>) results.values;
            this.myAdapter.notifyDataSetChanged();
        }
    }
}
