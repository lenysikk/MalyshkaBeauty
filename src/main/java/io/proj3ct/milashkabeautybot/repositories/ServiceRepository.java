package io.proj3ct.milashkabeautybot.repositories;

import io.proj3ct.milashkabeautybot.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    Service findByName(String serviceName);
}
