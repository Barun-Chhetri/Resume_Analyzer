package com.resume.resume_api.controller;
import com.resume.resume_api.entity.Contact;
import com.resume.resume_api.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ContactController {
    private final ContactRepository contactRepository;

    @PostMapping
    public Contact addContact(@RequestBody Contact contact) {
        return contactRepository.save(contact);
    }

    @GetMapping
    public List<Contact> getContacts() {
        return contactRepository.findAll();
    }

    @PutMapping("/{id}")
    public Contact updateContact(@PathVariable Long id, @RequestBody Contact contactDetails) {
        return contactRepository.findById(id)
                .map(contact -> {
                    contact.setPhone(contactDetails.getPhone());
                    contact.setEmail(contactDetails.getEmail());
                    contact.setWebsite(contactDetails.getWebsite());
                    contact.setAddress(contactDetails.getAddress());
                    return contactRepository.save(contact);
                })
                .orElseThrow(() -> new RuntimeException("Contact not found"));
    }
}
