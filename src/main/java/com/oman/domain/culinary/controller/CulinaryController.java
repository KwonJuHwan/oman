package com.oman.domain.culinary.controller;

import com.oman.domain.culinary.service.IngredientAutocompleteService;
import com.oman.domain.recipe.dto.response.IngredientSimpleDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CulinaryController {

    private final IngredientAutocompleteService autocompleteService;

    // 재료 자동완성
    @GetMapping("/ingredients/autocomplete")
    public ResponseEntity<List<IngredientSimpleDto>> autocomplete(@RequestParam String keyword) {
        return ResponseEntity.ok(autocompleteService.getAutocompleteResults(keyword));
    }
}
