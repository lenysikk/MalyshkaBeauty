package io.proj3ct.milashkabeautybot.repositories;

import io.proj3ct.milashkabeautybot.model.StandardTextResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StandardTextResponseRepository extends JpaRepository<StandardTextResponse, Long> {
    StandardTextResponse findByName(String searchName);

    StandardTextResponse findOneByName(String info);
}
