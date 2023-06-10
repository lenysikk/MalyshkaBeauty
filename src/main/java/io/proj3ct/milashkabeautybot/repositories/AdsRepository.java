package io.proj3ct.milashkabeautybot.repositories;

import io.proj3ct.milashkabeautybot.model.Ads;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AdsRepository extends JpaRepository<Ads, Long> {
}
