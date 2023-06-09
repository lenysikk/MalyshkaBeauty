package io.proj3ct.milashkabeautybot.repositories;

import io.proj3ct.milashkabeautybot.model.Master;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterRepository extends JpaRepository<Master, Long> {
    List<Master> findByServicesName(String serviceName);
    Master findByName(String masterName);
}