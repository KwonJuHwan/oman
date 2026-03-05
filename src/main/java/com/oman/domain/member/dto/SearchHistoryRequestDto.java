package com.oman.domain.member.dto;

import com.oman.domain.member.entity.SearchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryRequestDto {
    private SearchType type;
    private Long keywordId;
    private String keyword;
}