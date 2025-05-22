package com.example.repository;

import com.example.model.VacancyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacancyRepository extends JpaRepository<VacancyEntity, Long> {
    
    @Query("SELECT v FROM VacancyEntity v WHERE " +
           "(:language IS NULL OR LOWER(v.title) LIKE LOWER(CONCAT('%', :language, '%'))) AND " +
           "(:city IS NULL OR v.city = :city)")
    List<VacancyEntity> findByLanguageAndCity(@Param("language") String language, @Param("city") String city);

    boolean existsByLink(String link);
}
