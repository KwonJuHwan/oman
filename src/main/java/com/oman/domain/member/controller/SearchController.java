package com.oman.domain.member.controller;

import com.oman.domain.member.dto.SearchHistoryRequestDto;
import com.oman.domain.member.entity.SearchType;
import com.oman.domain.member.service.SearchHistoryService;
import com.oman.domain.recipe.dto.response.IngredientSimpleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search-history")
@RequiredArgsConstructor
public class SearchController {

    private final SearchHistoryService searchHistoryService;

    @GetMapping
    public ResponseEntity<List<IngredientSimpleDto>> getRecentSearches(
        Principal principal,
        @RequestParam SearchType type) {

        String email = principal.getName();
        List<IngredientSimpleDto> response = searchHistoryService.getRecentSearches(email, type);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> saveRecentSearch(Principal principal, @RequestBody SearchHistoryRequestDto request) {
        String email = principal.getName();
        searchHistoryService.saveRecentSearch(email, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteRecentSearch(Principal principal, @RequestBody SearchHistoryRequestDto request) {
        String email = principal.getName();
        searchHistoryService.deleteRecentSearch(email, request);
        return ResponseEntity.ok().build();
    }
}