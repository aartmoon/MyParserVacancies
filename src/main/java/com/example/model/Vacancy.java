package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private Integer salaryFrom;

    @Column
    private Integer salaryTo;

    @Column
    private String currency;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String language;

    @Column(columnDefinition = "TEXT")
    private String requirement;

    @Column(columnDefinition = "TEXT")
    private String responsibility;

    @Column
    private LocalDateTime publishedAt;
} 