package io.proj3ct.MilashkaBeautyBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "mastersTable")
public class Master {
    @Id
    private Long id;

    private String Name;

    private String serviceType;
}
