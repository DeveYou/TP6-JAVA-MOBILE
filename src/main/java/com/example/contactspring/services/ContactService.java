package com.example.contactspring.services;

import com.example.contactspring.models.Contact;
import com.example.contactspring.repositories.IContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactService {

    private IContactRepository contactRepository;
    public ContactService(IContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Transactional
    public Contact updateContact(Contact contact) {
        Contact existing = contactRepository.findById(contact.getId())
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        existing.setName(contact.getName());
        existing.setNumber(contact.getNumber());

        return contactRepository.save(existing);
    }


    @Transactional
    public Contact addContact(Contact contact) {
        return contactRepository.save(contact);
    }

    @Transactional
    public List<Contact> addAllContacts(List<Contact> contacts) {
        return contactRepository.saveAll(contacts);
    }
}