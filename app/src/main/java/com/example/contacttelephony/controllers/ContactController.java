package com.example.contacttelephony.controllers;

import android.util.Log;

import com.example.contacttelephony.api.ApiClient;
import com.example.contacttelephony.api.ContactApi;
import com.example.contacttelephony.entities.Contact;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactController {
    private final ContactApi apiService;
    public ContactController(){
        this.apiService = ApiClient.getRetrofitInstance();
    }

    public void create(Contact contact, ContactCreateCallback callback) {
        Call<Contact> call = apiService.addContact(contact);
        call.enqueue(new Callback<Contact>() {
            @Override
            public void onResponse(Call<Contact> call, Response<Contact> response) {
                if (response.isSuccessful()) {
                    Log.d("API", "Contact saved: " + response.body().getName());
                    callback.onSuccess(response.body());
                } else {
                    Log.e("API", "Error saving contact: " + response.code());
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Contact> call, Throwable t) {
                Log.e("API", "Failed to save contact: " + t.getMessage());
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    public void createAll(List<Contact> contacts, ContactBulkCreateCallback callback) {
        Call<List<Contact>> call = apiService.addAllContacts(contacts);
        call.enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                if (response.isSuccessful()) {
                    Log.d("API", "All contacts saved successfully.");
                    callback.onSuccess(response.body());
                } else {
                    Log.e("API", "Error saving contacts: " + response.code());
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                Log.e("API", "Failed to save contacts: " + t.getMessage());
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }


    public interface ContactCreateCallback {
        void onSuccess(Contact contact);
        void onFailure(String error);
    }

    public interface ContactBulkCreateCallback {
        void onSuccess(List<Contact> contacts);
        void onFailure(String error);
    }
}
