package com.javarush.module4.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "language", schema = "movie")
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "language_id")
    private Byte id;

    //@Column(name = "name", nullable = false, length = 20)
    @Column(name = "name", nullable = false, columnDefinition = "CHAR(20)")
    private String name;

    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;
}