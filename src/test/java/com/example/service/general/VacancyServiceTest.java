package com.example.service.general;

import com.example.model.Vacancy;
import com.example.repository.VacancyRepository;
import com.example.config.Constants;
import com.example.service.trudvsem.TrudVsemFetcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


public class VacancyServiceTest {

    @Mock
    private VacancyFetcher vacancyFetcher;

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private TrudVsemFetcher trudVsemFetcher;

    @Mock
    private VacancyFilter vacancyFilter;

    @Mock
    private VacancySortService vacancySortService; // не используется в методах, но нужен для конструктора

    @Mock
    private VacancyCleaner vacancyCleaner;

    @InjectMocks
    private VacancyService vacancyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void refreshVacanciesWithLanguageOnlyCallsFetchOnce() throws Exception {
        String language = "Java";
        String city = "Москва";

        vacancyService.refreshVacancies(language, city);

        // Должен вызвать vacancyFetcher.fetchVacancies только один раз с заданными параметрами
        verify(vacancyFetcher, times(1)).fetchVacancies(language, city);
        // Больше вызовов не должно быть
        verifyNoMoreInteractions(vacancyFetcher);
    }

    @Test
    void refreshVacanciesWithEmptyLanguageCallsFetchForAllLanguages() throws Exception {
        String language = "";
        String city = "Москва";

        // Все языки, определённые в Constants.LANGUAGES
        List<String> allLanguages = Constants.LANGUAGES;

        vacancyService.refreshVacancies(language, city);

        // Должен вызвать fetchVacancies(lang, city) для каждого lang из Constants.LANGUAGES
        for (String lang : allLanguages) {
            verify(vacancyFetcher).fetchVacancies(lang, city);
        }
        // Всего вызовов равно числу языков
        verify(vacancyFetcher, times(allLanguages.size())).fetchVacancies(anyString(), eq(city));
    }

    @Test
    void refreshVacanciesWithNullLanguageCallsFetchForAllLanguages() throws Exception {
        String language = null;
        String city = "Москва";

        List<String> allLanguages = Constants.LANGUAGES;

        vacancyService.refreshVacancies(language, city);

        for (String lang : allLanguages) {
            verify(vacancyFetcher).fetchVacancies(lang, city);
        }
        verify(vacancyFetcher, times(allLanguages.size())).fetchVacancies(anyString(), eq(city));
    }

    @Test
    void getVacanciesWithLanguageAndCityReturnsFilteredIfWithSalaryTrue() {
        String language = "Java";
        String city = "Москва";
        boolean withSalary = true;

        Vacancy v1 = new Vacancy();
        v1.setId(1L);
        Vacancy v2 = new Vacancy();
        v2.setId(2L);

        // Репозиторий возвращает список из двух вакансий
        when(vacancyRepository.findByLanguageAndCity(language, city))
                .thenReturn(Arrays.asList(v1, v2));

        // Метод cleaner возвращает тот же список
        List<Vacancy> cleanedList = Arrays.asList(v1, v2);
        when(vacancyCleaner.clean(anyList()))
                .thenReturn(cleanedList);

        // При фильтрации по зарплате оставим только одну вакансию
        List<Vacancy> filteredList = Collections.singletonList(v2);
        when(vacancyFilter.filterBySalary(cleanedList))
                .thenReturn(filteredList);

        List<Vacancy> result = vacancyService.getVacancies(language, city, withSalary);

        assertThat(result).isEqualTo(filteredList);

        InOrder inOrder = inOrder(vacancyRepository, vacancyCleaner, vacancyFilter);
        inOrder.verify(vacancyRepository).findByLanguageAndCity(language, city);
        inOrder.verify(vacancyCleaner).clean(Arrays.asList(v1, v2));
        inOrder.verify(vacancyFilter).filterBySalary(cleanedList);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void getVacanciesWithLanguageAndCityReturnsCleanedIfWithSalaryFalse() {
        String language = "Python";
        String city = "London";
        boolean withSalary = false;

        Vacancy v1 = new Vacancy();
        v1.setId(10L);

        when(vacancyRepository.findByLanguageAndCity(language, city))
                .thenReturn(Collections.singletonList(v1));

        List<Vacancy> cleanedList = Collections.singletonList(v1);
        when(vacancyCleaner.clean(anyList()))
                .thenReturn(cleanedList);

        List<Vacancy> result = vacancyService.getVacancies(language, city, withSalary);

        assertThat(result).isEqualTo(cleanedList);

        InOrder inOrder = inOrder(vacancyRepository, vacancyCleaner);
        inOrder.verify(vacancyRepository).findByLanguageAndCity(language, city);
        inOrder.verify(vacancyCleaner).clean(Collections.singletonList(v1));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void getVacanciesWithLanguageAndEmptyCityQueriesAllCities() {
        String language = "Go";
        String city = "";
        boolean withSalary = false;

        Vacancy v1 = new Vacancy();
        v1.setId(100L);
        Vacancy v2 = new Vacancy();
        v2.setId(200L);

        List<String> allCities = Constants.CITIES;

        when(vacancyRepository.findByLanguageAndCity(eq(language), anyString()))
                .thenAnswer(invocation -> {
                    String c = invocation.getArgument(1);
                    if (c.equals(allCities.get(0))) {
                        return Collections.singletonList(v1);
                    } else if (c.equals(allCities.get(1))) {
                        return Collections.singletonList(v2);
                    } else {
                        return Collections.emptyList();
                    }
                });

        List<Vacancy> combined = Arrays.asList(v1, v2);
        when(vacancyCleaner.clean(anyList()))
                .thenReturn(combined);

        List<Vacancy> result = vacancyService.getVacancies(language, city, withSalary);

        assertThat(result).isEqualTo(combined);

        for (String c : allCities) {
            verify(vacancyRepository).findByLanguageAndCity(language, c);
        }
        verify(vacancyCleaner).clean(Arrays.asList(v1, v2));
    }

    @Test
    void getVacanciesWithEmptyLanguageAndCityQueriesAllLanguages() {
        String language = "";
        String city = "Екатеринбург";
        boolean withSalary = false;

        Vacancy v1 = new Vacancy();
        v1.setId(300L);
        Vacancy v2 = new Vacancy();
        v2.setId(400L);

        List<String> allLanguages = Constants.LANGUAGES;

        when(vacancyRepository.findByLanguageAndCity(anyString(), eq(city)))
                .thenAnswer(invocation -> {
                    String lang = invocation.getArgument(0);
                    if (lang.equals(allLanguages.get(0))) {
                        return Collections.singletonList(v1);
                    } else if (lang.equals(allLanguages.get(1))) {
                        return Collections.singletonList(v2);
                    } else {
                        return Collections.emptyList();
                    }
                });

        List<Vacancy> combined = Arrays.asList(v1, v2);
        when(vacancyCleaner.clean(anyList()))
                .thenReturn(combined);

        List<Vacancy> result = vacancyService.getVacancies(language, city, withSalary);

        assertThat(result).isEqualTo(combined);

        for (String lang : allLanguages) {
            verify(vacancyRepository).findByLanguageAndCity(lang, city);
        }
        verify(vacancyCleaner).clean(Arrays.asList(v1, v2));
    }

    @Test
    void getVacanciesWithEmptyLanguageAndEmptyCityUsesFindAll() {
        String language = "";
        String city = "";
        boolean withSalary = false;

        Vacancy v1 = new Vacancy();
        v1.setId(500L);
        Vacancy v2 = new Vacancy();
        v2.setId(600L);

        when(vacancyRepository.findAll())
                .thenReturn(Arrays.asList(v1, v2));

        List<Vacancy> combined = Arrays.asList(v1, v2);
        when(vacancyCleaner.clean(anyList()))
                .thenReturn(combined);

        List<Vacancy> result = vacancyService.getVacancies(language, city, withSalary);

        assertThat(result).isEqualTo(combined);

        verify(vacancyRepository).findAll();
        verify(vacancyCleaner).clean(Arrays.asList(v1, v2));
    }

    @Test
    void getVacanciesFilterBySalaryReturnsEmpty() {
        String language = "Go";
        String city = "Москва";
        boolean withSalary = true;

        Vacancy v1 = new Vacancy();
        v1.setId(700L);

        when(vacancyRepository.findByLanguageAndCity(language, city))
                .thenReturn(Collections.singletonList(v1));

        List<Vacancy> cleanedList = Collections.singletonList(v1);
        when(vacancyCleaner.clean(anyList()))
                .thenReturn(cleanedList);

        when(vacancyFilter.filterBySalary(cleanedList))
                .thenReturn(Collections.emptyList());

        List<Vacancy> result = vacancyService.getVacancies(language, city, withSalary);

        assertThat(result).isEmpty();

        InOrder inOrder = inOrder(vacancyRepository, vacancyCleaner, vacancyFilter);
        inOrder.verify(vacancyRepository).findByLanguageAndCity(language, city);
        inOrder.verify(vacancyCleaner).clean(Collections.singletonList(v1));
        inOrder.verify(vacancyFilter).filterBySalary(cleanedList);
        inOrder.verifyNoMoreInteractions();
    }
}
