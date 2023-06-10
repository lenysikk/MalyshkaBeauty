package io.proj3ct.milashkabeautybot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "textsResponseTable")
public class StandardTextResponse {
    @Id
    private Long id;

    private String name;

    @Column(length = 1000)
    private String text;
}
