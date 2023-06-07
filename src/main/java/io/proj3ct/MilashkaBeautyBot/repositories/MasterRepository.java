package io.proj3ct.MilashkaBeautyBot.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MasterRepository extends JpaRepository<Master, Long> {
    List<Master> findByServicesName(String serviceName);
    Master findByName(String masterName);
}