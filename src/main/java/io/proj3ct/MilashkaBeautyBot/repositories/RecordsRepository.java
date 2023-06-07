package io.proj3ct.MilashkaBeautyBot.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface RecordsRepository extends JpaRepository<Records, Long> {
}
