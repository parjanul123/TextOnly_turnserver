package com.textonly.backend.repository;

import com.textonly.backend.model.Contact;
import com.textonly.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByUser(User user);
    Optional<Contact> findByUserAndContact(User user, User contact);
    void deleteByUserAndContact(User user, User contact);
}
