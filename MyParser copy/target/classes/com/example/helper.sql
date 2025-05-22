CREATE TABLE IF NOT EXISTS vacancies (
                                         id BIGSERIAL PRIMARY KEY,
                                         title VARCHAR(255) NOT NULL,
                                         salary VARCHAR(255),
                                         link VARCHAR(255) NOT NULL,
                                         company VARCHAR(255) NOT NULL,
                                         city VARCHAR(255) NOT NULL,
                                         requirement TEXT,
                                         responsibility TEXT,
                                         published_at TIMESTAMP,
                                         CONSTRAINT uk_vacancy_link UNIQUE (link)
);
TRUNCATE TABLE vacancies;