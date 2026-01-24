package com.oman.domain.culinary.repository;


import com.oman.domain.culinary.entity.Culinary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CulinaryRepository extends JpaRepository<Culinary, Long> {
    Optional<Culinary> findByName(String name);
    Page<Culinary> findAll(Pageable pageable);
}
