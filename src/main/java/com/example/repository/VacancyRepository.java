package com.example.repository;

import com.example.model.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {

    @Query("SELECT v FROM Vacancy v WHERE " +
            "(:language IS NULL OR v.language = :language) AND " +
            "(:city IS NULL OR v.city = :city)")
    List<Vacancy> findByLanguageAndCity(@Param("language") String language, @Param("city") String city);

    boolean existsByLink(String link);
}