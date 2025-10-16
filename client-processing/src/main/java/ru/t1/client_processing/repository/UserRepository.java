package ru.t1.client_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.client_processing.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    Optional <User> findByLogin(String login);
}
