package com.example.contactspring.controllers;


import com.example.contactspring.models.Contact;
import com.example.contactspring.services.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
@CrossOrigin(origins = "*") // Optional, for frontend to access it
public class ContactController {
    private ContactService contactService;

    public ContactController(ContactService contactService){
         this.contactService = contactService;
    }

    // Add new contact
    @PostMapping("/addContact")
    public Contact addContact(@RequestBody Contact contact) {
        System.out.println("Received: " + contact);
        return contactService.addContact(contact);
    }

    @PostMapping("/addAllContacts")
    public List<Contact> addAllContacts(@RequestBody List<Contact> contacts) {
        return contactService.addAllContacts(contacts);
    }

    @PutMapping("/updateContact")
    public Contact updateContact(@RequestBody Contact contact) {
        return contactService.updateContact(contact);
    }

}
