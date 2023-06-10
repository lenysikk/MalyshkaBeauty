package io.proj3ct.milashkabeautybot.repositories;

import io.proj3ct.milashkabeautybot.model.Records;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordsRepository extends JpaRepository<Records, Long> {
    List<Records> findAllByUserId(Long userId);

    List<Records> findByUserId(Long id);
}
