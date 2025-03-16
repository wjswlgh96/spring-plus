package org.example.expert.domain.auth.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class AuthServiceTest {

    private static final int USER_MAX_COUNT = 1_000_000;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    @Rollback(false)
    void 유저_100만건_생성() {
        long start = System.currentTimeMillis();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < USER_MAX_COUNT; i++) {
            users.add(
                new User(
                    generateUniqueEmail(i),
                    "Password1234!",
                    UserRole.ROLE_ADMIN,
                    generateRandomNickname()
                )
            );

            if (users.size() == 1000) {
                userRepository.saveAll(users);
                entityManager.flush();
                entityManager.clear();
                users.clear();
            }

        }
        if (!users.isEmpty()) {
            userRepository.saveAll(users);
        }

        Long count = entityManager.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
        System.out.println("Inserted users: " + count + ", Time: " + (System.currentTimeMillis() - start) + "ms");
    }

    private String generateUniqueEmail(int index) {
        return "user" + index + "@example.com";
    }

    private String generateRandomNickname() {
        return "user" + UUID.randomUUID().toString().substring(0, 8);
    }
}