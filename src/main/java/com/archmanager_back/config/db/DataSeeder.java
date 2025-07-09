package com.archmanager_back.config.db;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.archmanager_back.model.entity.Permission;
import com.archmanager_back.model.entity.Project;
import com.archmanager_back.model.entity.Role;
import com.archmanager_back.model.entity.User;
import com.archmanager_back.repository.jpa.ProjectRepository;
import com.archmanager_back.repository.jpa.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository        userRepository;
    private final ProjectRepository     projectRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Users already exist, skipping seeding");
            return;
        }

        // projet #1
        Project proj1 = new Project();
        proj1.setSlug("proj-75969c6d");
        proj1.setBoltPort(60130);
        proj1.setContainerId("6a2785b5944cfe3f31431542b31011260f969034847375d8552c59025e4ff020");
        proj1.setVolumeName("proj-75969c6d_data");
        proj1.setPassword("password1"); 
        proj1.setName("Project One");
        projectRepository.save(proj1);

        // projet #2
        Project proj2 = new Project();
        proj2.setSlug("proj-a64eeed2");
        proj2.setBoltPort(60161);
        proj2.setContainerId("7c1f2f3f8d0268309ae94c4beb6bf680af6d9b130f8596e5515775b648bb56c7");
        proj2.setVolumeName("proj-a64eeed2_data");
        proj2.setPassword("password2"); 
        proj2.setName("Project Two");
        projectRepository.save(proj2);

        // -- user1
        User user1 = new User();
        user1.setUsername("user1");
        user1.setFirstname("User");
        user1.setLastname("One");
        user1.setPassword(passwordEncoder.encode("password1"));

        // Permissions et association bidirectionnelle
        Permission perm1 = new Permission(user1, proj1, Role.ADMIN);
        user1.addPermission(perm1);
        proj1.addPermission(perm1);

        Permission perm2 = new Permission(user1, proj2, Role.EDIT);
        user1.addPermission(perm2);
        proj2.addPermission(perm2);

        userRepository.save(user1);

        // -- user2
        User user2 = new User();
        user2.setUsername("user2");
        user2.setFirstname("User");
        user2.setLastname("Two");
        user2.setPassword(passwordEncoder.encode("password2"));

        Permission perm3 = new Permission(user2, proj1, Role.READ);
        user2.addPermission(perm3);
        proj1.addPermission(perm3);

        Permission perm4 = new Permission(user2, proj2, Role.ADMIN);
        user2.addPermission(perm4);
        proj2.addPermission(perm4);

        userRepository.save(user2);

        log.info("Seeded 2 users with permissions");
    }
}
