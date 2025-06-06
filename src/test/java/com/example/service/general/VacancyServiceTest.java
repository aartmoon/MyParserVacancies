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
    private VacancyFetcher vacancyFetcher1;

    @Mock
    private VacancyFetcher vacancyFetcher2;

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private VacancyFilter vacancyFilter;

    @Mock
    private VacancySortService vacancySortService;

    @Mock
    private VacancyCleaner vacancyCleaner;

    @InjectMocks
    private VacancyService vacancyService;

    private List<VacancyFetcher> vacancyFetchers;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vacancyFetchers = Arrays.asList(vacancyFetcher1, vacancyFetcher2);
        vacancyService = new VacancyService(vacancyFetchers, vacancyRepository, vacancyFilter, vacancyCleaner);
    }

    @Test
    void refreshVacanciesWithLanguageOnlyCallsFetchOnce() throws Exception {
        String language = "Java";
        String city = "Москва";

        vacancyService.refreshVacancies(language, city);

        // Проверяем, что каждый фетчер вызван один раз
        for (VacancyFetcher fetcher : vacancyFetchers) {
            verify(fetcher, times(1)).fetchVacancies(language, city);
            verifyNoMoreInteractions(fetcher);
        }
    }

    @Test
    void refreshVacanciesWithEmptyLanguageCallsFetchForAllLanguages() throws Exception {
        String language = "";
        String city = "Москва";

        List<String> allLanguages = Constants.LANGUAGES;

        vacancyService.refreshVacancies(language, city);

        // Проверяем, что каждый фетчер вызван для каждого языка
        for (VacancyFetcher fetcher : vacancyFetchers) {
            for (String lang : allLanguages) {
                verify(fetcher).fetchVacancies(lang, city);
            }
            verify(fetcher, times(allLanguages.size())).fetchVacancies(anyString(), eq(city));
        }
    }

    @Test
    void refreshVacanciesWithNullLanguageCallsFetchForAllLanguages() throws Exception {
        String language = null;
        String city = "Москва";

        List<String> allLanguages = Constants.LANGUAGES;

        vacancyService.refreshVacancies(language, city);

        // Проверяем, что каждый фетчер вызван для каждого языка
        for (VacancyFetcher fetcher : vacancyFetchers) {
            for (String lang : allLanguages) {
                verify(fetcher).fetchVacancies(lang, city);
            }
            verify(fetcher, times(allLanguages.size())).fetchVacancies(anyString(), eq(city));
        }
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
    void getVacanciesWithEmptyLanguageAndEmptyCity_СейчасВызываетПоВсемПарам() {
        String language = "";
        String city = "";
        boolean withSalary = false;

        List<String> languages = List.of("Java", "Python", "PHP");
        List<String> cities    = List.of("Москва", "Санкт-Петербург", "Екатеринбург");

        Vacancy jM = new Vacancy(); jM.setId(1L);   // Java × Москва
        Vacancy jSP = new Vacancy(); jSP.setId(2L);// Java × Санкт-Петербург
        Vacancy jE = new Vacancy(); jE.setId(3L);   // Java × Екатеринбург

        Vacancy pM = new Vacancy(); pM.setId(4L);   // Python × Москва
        Vacancy pSP = new Vacancy(); pSP.setId(5L);// Python × Санкт-Петербург
        Vacancy pE = new Vacancy(); pE.setId(6L);   // Python × Екатеринбург

        Vacancy phM = new Vacancy(); phM.setId(7L);    // PHP × Москва
        Vacancy phSP = new Vacancy(); phSP.setId(8L);  // PHP × Санкт-Петербург
        Vacancy phE = new Vacancy(); phE.setId(9L);    // PHP × Екатеринбург

        when(vacancyRepository.findByLanguageAndCity("Java", "Москва"))
                .thenReturn(List.of(jM));
        when(vacancyRepository.findByLanguageAndCity("Java", "Санкт-Петербург"))
                .thenReturn(List.of(jSP));
        when(vacancyRepository.findByLanguageAndCity("Java", "Екатеринбург"))
                .thenReturn(List.of(jE));

        when(vacancyRepository.findByLanguageAndCity("Python", "Москва"))
                .thenReturn(List.of(pM));
        when(vacancyRepository.findByLanguageAndCity("Python", "Санкт-Петербург"))
                .thenReturn(List.of(pSP));
        when(vacancyRepository.findByLanguageAndCity("Python", "Екатеринбург"))
                .thenReturn(List.of(pE));

        when(vacancyRepository.findByLanguageAndCity("PHP", "Москва"))
                .thenReturn(List.of(phM));
        when(vacancyRepository.findByLanguageAndCity("PHP", "Санкт-Петербург"))
                .thenReturn(List.of(phSP));
        when(vacancyRepository.findByLanguageAndCity("PHP", "Екатеринбург"))
                .thenReturn(List.of(phE));

        List<Vacancy> rawCombined = Arrays.asList(
                jM, jSP, jE,
                pM, pSP, pE,
                phM, phSP, phE
        );

        List<Vacancy> expectedCleaned = Arrays.asList(
                jM, jSP, jE,
                pM, pSP, pE,
                phM, phSP, phE
        );
        when(vacancyCleaner.clean(anyList()))
                .thenReturn(expectedCleaned);

        List<Vacancy> result = vacancyService.getVacancies(language, city, withSalary);


        assertThat(result).isEqualTo(expectedCleaned);

        for (String lang : languages) {
            for (String c : cities) {
                verify(vacancyRepository).findByLanguageAndCity(lang, c);
            }
        }

        verify(vacancyCleaner).clean(eq(rawCombined));

        verifyNoMoreInteractions(vacancyRepository);
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
