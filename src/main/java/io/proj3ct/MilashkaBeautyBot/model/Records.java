package io.proj3ct.MilashkaBeautyBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "recordsTable")
public class Records {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String serviceType;

    @ManyToOne
    @JoinColumn(name = "master_id")
    private Master master;

    private String comment;
}
