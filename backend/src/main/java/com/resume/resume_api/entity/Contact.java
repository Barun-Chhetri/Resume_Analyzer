package com.resume.resume_api.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String title;
    private String phone;
    private String email;
    private String website;
    private String address;
    @Column(columnDefinition = "TEXT")
    private String summary;
}
