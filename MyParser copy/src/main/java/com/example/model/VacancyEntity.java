package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "vacancies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vacancy_link",
                        columnNames = "link")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacancyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String salary;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String city;

    @Column(columnDefinition = "TEXT")
    private String requirement;

    @Column(columnDefinition = "TEXT")
    private String responsibility;

    @Column
    private LocalDateTime publishedAt;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
} 