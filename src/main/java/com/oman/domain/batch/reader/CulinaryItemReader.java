package com.oman.domain.batch.reader;

import com.oman.domain.culinary.entity.Culinary;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CulinaryItemReader {

    private final EntityManagerFactory emf;

    public JpaPagingItemReader<Culinary> reader() {
        return new JpaPagingItemReaderBuilder<Culinary>()
            .name("culinaryReader")
            .entityManagerFactory(emf)
            .queryString("SELECT c FROM Culinary c")
            .pageSize(10)
            .build();
    }
}
