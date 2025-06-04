package com.example.controller;

import com.example.config.Constants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / должен вернуть view 'index' и заполнить модель языками и городами")
    void home_shouldReturnIndexViewAndPopulateModel() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                // Проверяем, что view называется "index"
                .andExpect(view().name("index"))
                // Проверяем, что в модель добавлен атрибут "languages" со значением Constants.LANGUAGES
                .andExpect(model().attributeExists("languages"))
                .andExpect(model().attribute("languages", Constants.LANGUAGES))
                // Проверяем, что в модель добавлен атрибут "cities" со значением Constants.CITIES
                .andExpect(model().attributeExists("cities"))
                .andExpect(model().attribute("cities", Constants.CITIES));
    }
}
