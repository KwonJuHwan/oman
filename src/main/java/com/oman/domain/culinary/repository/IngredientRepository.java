package com.oman.domain.culinary.repository;

import com.oman.domain.culinary.entity.Ingredient;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findAllByNameIn(Set<String> names);

    Optional<Ingredient> findById(Long ingredientId);
}
