package com.example.service.general;

import com.example.model.Vacancy;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
public class VacancySortService {
    public void sortBySalary(List<Vacancy> vacancies, boolean ascending) {
        Comparator<Vacancy> comparator;
        if (ascending) {
            comparator = Comparator
                    .comparing(
                            (Vacancy v) -> (v.getSalaryFrom() == null || v.getSalaryFrom() == 0) ? null : v.getSalaryFrom(),
                            Comparator.nullsLast(Integer::compareTo)
                    )
                    .thenComparing(
                            (Vacancy v) -> (v.getSalaryTo() == null || v.getSalaryTo() == 0) ? null : v.getSalaryTo(),
                            Comparator.nullsLast(Integer::compareTo)
                    );
        } else {
            comparator = Comparator
                    .comparing(
                            (Vacancy v) -> (v.getSalaryTo() == null || v.getSalaryTo() == 0) ? null : v.getSalaryTo(),
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
                    .thenComparing(
                            (Vacancy v) -> (v.getSalaryFrom() == null || v.getSalaryFrom() == 0) ? null : v.getSalaryFrom(),
                            Comparator.nullsLast(Comparator.reverseOrder())
                    );
        }
        vacancies.sort(comparator);
    }
}