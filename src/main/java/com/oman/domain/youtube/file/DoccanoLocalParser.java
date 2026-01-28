package com.oman.domain.youtube.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;



@Slf4j
@Component
public class DoccanoLocalParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Map<Integer, List<String>>> parseFullData(String filePath) {
        Map<String, Map<Integer, List<String>>> recipeToVideoData = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                JsonNode root = objectMapper.readTree(line);
                String fullText = root.path("text").asText("");

                // 1. 요리명 추출
                String recipeName = extractRecipeName(fullText);
                if (recipeName == null) continue;

                // 2. 라벨 노드 찾기 (다양한 키 대응: label, labels, entities)
                JsonNode labelsNode = findLabelsNode(root);

                if (labelsNode == null || !labelsNode.isArray() || labelsNode.isEmpty()) {
                    log.warn("'{}' 요리: 라벨 노드를 찾았으나 비어있거나 배열이 아닙니다.", recipeName);
                    continue;
                }

                Map<Integer, List<String>> videoMap = recipeToVideoData.computeIfAbsent(recipeName, k -> new HashMap<>());

                // 3. 비디오 섹션 인덱싱
                Pattern p = Pattern.compile("<<(\\d+)번째 동영상 설명>>");
                Matcher m = p.matcher(fullText);
                List<VideoSection> sections = new ArrayList<>();
                while (m.find()) {
                    sections.add(new VideoSection(Integer.parseInt(m.group(1)), m.start()));
                }

                // 4. 라벨 순회 및 매칭
                int matchCount = 0;
                for (JsonNode node : labelsNode) {
                    // Doccano 객체 포맷 대응 (label, start_offset, end_offset)
                    String labelTag = node.path("label").asText("");
                    if (labelTag.isEmpty()) {
                        // [start, end, "TAG"] 배열 포맷 대응
                        labelTag = node.path(2).asText("");
                    }

                    if (!"INGREDIENT".equals(labelTag)) continue;

                    int start = node.has("start_offset") ? node.get("start_offset").asInt() : node.get(0).asInt();
                    int end = node.has("end_offset") ? node.get("end_offset").asInt() : node.get(1).asInt();

                    String ingredient = fullText.substring(start, end).trim();
                    Integer sequence = findVideoSequence(sections, start, fullText.length());

                    if (sequence != null) {
                        videoMap.computeIfAbsent(sequence, k -> new ArrayList<>()).add(ingredient);
                        matchCount++;
                    }
                }
                log.info("성공: '{}' 요리 - 섹션 {}개, 재료 {}개 매칭됨", recipeName, sections.size(), matchCount);
            }
        } catch (Exception e) {
            log.error("JSONL 파싱 실패: {}", e.getMessage());
        }
        return recipeToVideoData;
    }

    private JsonNode findLabelsNode(JsonNode root) {
        // Doccano가 사용하는 대표적인 라벨 키들 순차적 확인
        if (root.has("label") && root.get("label").isArray()) return root.get("label");
        if (root.has("labels") && root.get("labels").isArray()) return root.get("labels");
        if (root.has("entities") && root.get("entities").isArray()) return root.get("entities");
        return null;
    }

    private String extractRecipeName(String text) {
        Pattern p = Pattern.compile("\\[검색 쿼리\\]:\\s*([^\\n\\r\\s\\-]+)");
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    private Integer findVideoSequence(List<VideoSection> sections, int offset, int totalLen) {
        for (int i = 0; i < sections.size(); i++) {
            int currentStart = sections.get(i).startIdx;
            int nextStart = (i + 1 < sections.size()) ? sections.get(i + 1).startIdx : totalLen;
            if (offset >= currentStart && offset < nextStart) return sections.get(i).sequence;
        }
        return null;
    }

    private record VideoSection(int sequence, int startIdx) {}
}