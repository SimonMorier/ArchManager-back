package com.archmanager_back.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Data
@EqualsAndHashCode(exclude = "permissions")
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String slug;

    private String containerId;
    private Integer boltPort;
    private String volumeName;

    @Column(nullable = false)
    private String password;

    private String name;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Permission> permissions = new HashSet<>();

     public static String generateSlug(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String generateVolumeName(String slug, String suffix) {
        return slug + suffix;
    }

    public static String generatePassword(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        var rnd = new Random();
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    // helper pour bidirectionnalité
    public void addPermission(Permission perm) {
        permissions.add(perm);
        perm.setProject(this);
    }

    public void removePermission(Permission perm) {
        permissions.remove(perm);
        perm.setProject(null);
    }
}