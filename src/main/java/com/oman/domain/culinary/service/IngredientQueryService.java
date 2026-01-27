package com.oman.domain.culinary.service;

import com.oman.domain.culinary.entity.Ingredient;
import com.oman.domain.culinary.repository.IngredientRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngredientQueryService {
    private final IngredientRepository ingredientRepository;

    public Optional<Ingredient> findIngredientById(Long id) {
        return ingredientRepository.findById(id);
    }

    public Map<Long,String> findAll(){
        return ingredientRepository.findAll()
            .stream().collect(Collectors.toMap(Ingredient::getId, Ingredient::getName));
    }


    public List<Ingredient> findAllById(Set<Long> ingredientId){
        return ingredientRepository.findAllById(ingredientId);
    }
}
