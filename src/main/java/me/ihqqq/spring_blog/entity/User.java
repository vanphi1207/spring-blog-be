package me.ihqqq.spring_blog.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    @Column(name = "username", unique = true, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String username;
    String password;

    @Column(unique = true, nullable = false)
    String email;
    String firstName;
    String lastName;

    @Column(columnDefinition = "TEXT")
    String bio;

    String avatarUrl;
    LocalDate dob;

    @ManyToMany
    Set<Role> roles;

    boolean emailVerified = false;



}
