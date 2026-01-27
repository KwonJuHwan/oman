package com.oman.domain.culinary.service;

import com.oman.domain.culinary.repository.CulinaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CulinaryQueryService {
    private final CulinaryRepository culinaryRepository;

    public boolean culinaryExistByName(String culinaryName){
        return culinaryRepository.existsByName(culinaryName);
    }
}
