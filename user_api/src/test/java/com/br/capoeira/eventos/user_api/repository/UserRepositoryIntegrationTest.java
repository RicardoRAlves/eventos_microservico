package com.br.capoeira.eventos.user_api.repository;

import com.br.capoeira.eventos.user_api.enums.Role;
import com.br.capoeira.eventos.user_api.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private UserRepository repository;

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        var user = getMockUser(
                null,
                "Ricardo",
                "ricardo@test.com",
                "123456",
                Role.CLIENT,
                true,
                "avatar.png",
                1L,
                10L
        );

        var savedUser = repository.save(user);

        Optional<User> result = repository.findByEmail("ricardo@test.com");

        assertTrue(result.isPresent());
        assertEquals(savedUser.getId(), result.get().getId());
        assertEquals("ricardo@test.com", result.get().getEmail());
        assertEquals("Ricardo", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty when user email does not exist")
    void shouldReturnEmptyWhenUserEmailDoesNotExist() {
        Optional<User> result = repository.findByEmail("notfound@test.com");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find all users by organization id ordered by id asc")
    void shouldFindAllUsersByOrganizationIdOrderedByIdAsc() {
        var user1 = repository.save(getMockUser(
                null,
                "User B",
                "userb@test.com",
                "123456",
                Role.CLIENT,
                true,
                "avatar-b.png",
                100L,
                10L
        ));

        var user2 = repository.save(getMockUser(
                null,
                "User A",
                "usera@test.com",
                "123456",
                Role.CLIENT,
                true,
                "avatar-a.png",
                100L,
                20L
        ));

        repository.save(getMockUser(
                null,
                "Other Org User",
                "otherorg@test.com",
                "123456",
                Role.CLIENT,
                true,
                "avatar-c.png",
                999L,
                10L
        ));

        List<User> result = repository.findAllByOrganizationIdOrderByIdAsc(100L);

        assertEquals(2, result.size());
        assertEquals(user1.getId(), result.get(0).getId());
        assertEquals(user2.getId(), result.get(1).getId());
        assertEquals(100L, result.get(0).getOrganizationId());
        assertEquals(100L, result.get(1).getOrganizationId());
    }

    @Test
    @DisplayName("Should return empty list when organization id does not exist")
    void shouldReturnEmptyListWhenOrganizationIdDoesNotExist() {
        List<User> result = repository.findAllByOrganizationIdOrderByIdAsc(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find all users by organization unit id ordered by id asc")
    void shouldFindAllUsersByOrganizationUnitIdOrderedByIdAsc() {
        var user1 = repository.save(getMockUser(
                null,
                "User One",
                "userone@test.com",
                "123456",
                Role.CLIENT,
                true,
                "avatar-1.png",
                1L,
                500L
        ));

        var user2 = repository.save(getMockUser(
                null,
                "User Two",
                "usertwo@test.com",
                "123456",
                Role.ADMIN,
                true,
                "avatar-2.png",
                2L,
                500L
        ));

        repository.save(getMockUser(
                null,
                "Other Unit User",
                "otherunit@test.com",
                "123456",
                Role.CLIENT,
                true,
                "avatar-3.png",
                3L,
                999L
        ));

        List<User> result = repository.findAllByOrganizationUnitIdOrderByIdAsc(500L);

        assertEquals(2, result.size());
        assertEquals(user1.getId(), result.get(0).getId());
        assertEquals(user2.getId(), result.get(1).getId());
        assertEquals(500L, result.get(0).getOrganizationUnitId());
        assertEquals(500L, result.get(1).getOrganizationUnitId());
    }

    @Test
    @DisplayName("Should return empty list when organization unit id does not exist")
    void shouldReturnEmptyListWhenOrganizationUnitIdDoesNotExist() {
        List<User> result = repository.findAllByOrganizationUnitIdOrderByIdAsc(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return true when email already exists")
    void shouldReturnTrueWhenEmailAlreadyExists() {
        repository.save(getMockUser(
                null,
                "Ricardo",
                "ricardo@test.com",
                "123456",
                Role.CLIENT,
                true,
                "avatar.png",
                1L,
                10L
        ));

        Boolean result = repository.existsByEmail("ricardo@test.com");

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        Boolean result = repository.existsByEmail("notfound@test.com");

        assertFalse(result);
    }

    private User getMockUser(
            Long id,
            String name,
            String email,
            String password,
            Role role,
            Boolean active,
            String avatarUrl,
            Long organizationId,
            Long organizationUnitId
    ) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setActive(active);
        user.setAvatarUrl(avatarUrl);
        user.setOrganizationId(organizationId);
        user.setOrganizationUnitId(organizationUnitId);
        return user;
    }
}
