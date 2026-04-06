package com.javarush.module4.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "film_text", schema = "movie")
public class FilmText {

    @Id
    @Column(name = "film_id")
    private Short id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // @MapsId: id этой сущности берётся из связанного Film
    @OneToOne
    @MapsId
    @JoinColumn(name = "film_id")
    private Film film;
}