package com.example.contactspring.repositories;

import com.example.contactspring.models.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IContactRepository extends JpaRepository<Contact, Integer> {
}
