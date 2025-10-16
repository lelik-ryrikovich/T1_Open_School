package ru.t1.client_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.client_processing.entity.Role;
import ru.t1.client_processing.entity.enums.RoleEnum;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
}
