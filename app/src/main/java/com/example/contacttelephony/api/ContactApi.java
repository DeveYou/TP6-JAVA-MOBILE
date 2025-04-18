package com.example.contacttelephony.api;

import com.example.contacttelephony.entities.Contact;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ContactApi {
    @POST("/contacts/addContact")
    Call<Contact> addContact(@Body Contact contact);

    @POST("/contacts/addAllContacts")
    Call<List<Contact>> addAllContacts(@Body List<Contact> contacts);
}
