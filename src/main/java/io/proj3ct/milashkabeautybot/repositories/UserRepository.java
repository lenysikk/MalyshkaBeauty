package io.proj3ct.milashkabeautybot.repositories;

import io.proj3ct.milashkabeautybot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameContainingIgnoreCase(String searchQuery);
}
