package com.sky.cache._1_practice;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean soldOut;

    @Builder
    public Book(String name, boolean soldOut) {
        this.name = name;
        this.soldOut = soldOut;
    }
}
