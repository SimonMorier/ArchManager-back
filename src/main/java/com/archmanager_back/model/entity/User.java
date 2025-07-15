package com.archmanager_back.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(exclude = "permissions")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstname;

    @Column(name = "last_name", nullable = false)
    private String lastname;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Permission> permissions = new HashSet<>();

    public void addPermission(Permission perm) {
        permissions.add(perm);
        perm.setUser(this);
    }

    public void removePermission(Permission perm) {
        permissions.remove(perm);
        perm.setUser(null);
    }
}
