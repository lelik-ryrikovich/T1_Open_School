package ru.t1.account_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.account_processing.entity.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
}
