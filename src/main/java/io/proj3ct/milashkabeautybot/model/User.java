package io.proj3ct.milashkabeautybot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity(name = "usersDataTable")
public class User {
    @Id
    private Long id;

    private String name;

    private String userName; // телеграмм имя

    @Column(unique = true)
    private String email;

    @Column(length = 1000, nullable = true)
    private String password;

    private String phoneNumber;

    @Column(nullable = true)
    private Timestamp registerAt;

    private String accessCode;

    private boolean access;
}
