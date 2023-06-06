package io.proj3ct.MilashkaBeautyBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity(name = "usersDataTable")
public class User {
    @Id
    private Long id; //chatId


    private String Name;


    private String userName;


    private String email;


    private String phoneNumber;

    private Timestamp registerAt;
}
