package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest(classes = App.class)
class AppTest {
    @Test
    void contextLoads() {
    }
    @Test
    void mainMethodShouldNotThrowException() {
        assertThatCode(() -> App.main(new String[]{}))
                .doesNotThrowAnyException();
    }
}
