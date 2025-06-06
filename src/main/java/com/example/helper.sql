CREATE TABLE IF NOT EXISTS vacancies (
                                         id BIGSERIAL PRIMARY KEY,
                                         title TEXT NOT NULL,
                                         salary_from INTEGER,
                                         salary_to INTEGER,
                                         currency TEXT,
                                         link TEXT NOT NULL UNIQUE,
                                         company TEXT NOT NULL,
                                         city TEXT NOT NULL,
                                         language TEXT NOT NULL,
                                         requirement TEXT,
                                         responsibility TEXT,
                                         published_at TIMESTAMP
);

TRUNCATE TABLE vacancies;

select count(*) from vacancies;
