package com.oman.domain.youtube.service;


import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.oman.domain.culinary.entity.Culinary;
import com.oman.domain.culinary.entity.Ingredient;
import com.oman.domain.culinary.repository.CulinaryRepository;
import com.oman.domain.culinary.repository.IngredientRepository;
import com.oman.domain.fastapi.dto.InferenceResultForVideo;
import com.oman.domain.youtube.entity.YoutubeChannel;
import com.oman.domain.youtube.entity.YoutubeIngredient;
import com.oman.domain.youtube.entity.YoutubeVideo;
import com.oman.domain.youtube.repository.YoutubeChannelRepository;
import com.oman.domain.youtube.repository.YoutubeIngredientRepository;
import com.oman.domain.youtube.repository.YoutubeVideoRepository;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.services.youtube.model.VideoStatistics;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Thumbnail;



@Service
@RequiredArgsConstructor
public class YoutubeCommandService {
    private final CulinaryRepository culinaryRepository;
    private final YoutubeChannelRepository channelRepository;
    private final IngredientRepository ingredientRepository;
    private final YoutubeIngredientRepository youtubeIngredientRepository;
    private final YoutubeVideoRepository videoRepository;
    private final IngredientProcessor ingredientProcessor;

    @Transactional
    public List<YoutubeVideo> saveOrUpdateYoutubeData(String recipeName, List<SearchResult> searchResults,
        Map<String, Video> videoMap, Map<String, Channel> channelMap) {

        // 음식 저장
        Culinary culinary = culinaryRepository.findByName(recipeName)
            .orElseGet(() -> culinaryRepository.save(Culinary.builder().name(recipeName).build()));

        // 채널 Bulk 처리
        List<String> channelIds = new ArrayList<>(channelMap.keySet());
        Map<String, YoutubeChannel> existingChannels = channelRepository.findAllByApiChannelIdIn(channelIds)
            .stream().collect(Collectors.toMap(YoutubeChannel::getApiChannelId, c -> c));

        // 비디오 Bulk 처리 준비
        List<String> videoIds = new ArrayList<>(videoMap.keySet());
        Map<String, YoutubeVideo> existingVideos = videoRepository.findAllByApiVideoIdIn(videoIds)
            .stream().collect(Collectors.toMap(YoutubeVideo::getApiVideoId, v -> v));

        List<YoutubeVideo> resultVideos = new ArrayList<>();

        for (SearchResult res : searchResults) {
            String vidId = res.getId().getVideoId();
            if (vidId == null) continue;

            // 채널 처리
            YoutubeChannel channel = existingChannels.get(res.getSnippet().getChannelId());
            Channel detail = channelMap.get(res.getSnippet().getChannelId());
            channel = updateOrCreateChannel(channel, res, detail);
            if (channel.getId() == null) channelRepository.save(channel);

            // 비디오 처리
            YoutubeVideo video = existingVideos.get(vidId);
            Video vDetail = videoMap.get(vidId);
            video = updateOrCreateVideo(video, res, vDetail, channel, culinary);

            resultVideos.add(videoRepository.save(video));
        }
        return resultVideos;
    }
    public void saveInferenceResults(List<YoutubeVideo> savedVideos, Map<String, InferenceResultForVideo> results) {
        Map<String, YoutubeVideo> videoMap = savedVideos.stream()
            .collect(Collectors.toMap(YoutubeVideo::getApiVideoId, v -> v));

        // 1. 모든 추출된 재료 이름 모으기 (Bulk 조회를 위해)
        Set<String> allFilteredNames = new HashSet<>();
        results.values().forEach(res ->
            res.getExtractedIngredients().forEach(ext -> {
                String filtered = ingredientProcessor.filterIngredientName(ext.getIngredient());
                if (!filtered.isEmpty()) allFilteredNames.add(filtered);
            })
        );
        // 2. 마스터 재료(Ingredient) 테이블 한 번에 조회 및 신규 생성
        Map<String, Ingredient> ingredientMasterMap = prepareIngredientMaster(allFilteredNames);

        // 3. 필터링 및 엔티티 생성
        List<YoutubeIngredient> allToSave = new ArrayList<>();
        results.forEach((videoId, result) -> {
            YoutubeVideo video = videoMap.get(videoId);
            if (video != null) {
                List<YoutubeIngredient> entities = ingredientProcessor.filterAndCreateEntities(
                    video, result.getExtractedIngredients(), ingredientMasterMap
                );
                allToSave.addAll(entities);
            }
        });

        // 4. 최종 Bulk Save
        youtubeIngredientRepository.saveAll(allToSave);
    }

    private Map<String, Ingredient> prepareIngredientMaster(Set<String> names) {
        // 이미 존재하는 재료 조회
        List<Ingredient> existing = ingredientRepository.findAllByNameIn(names);
        Map<String, Ingredient> masterMap = existing.stream()
            .collect(Collectors.toMap(Ingredient::getName, i -> i));

        // 없는 재료는 새로 생성하여 저장
        List<Ingredient> toCreate = names.stream()
            .filter(name -> !masterMap.containsKey(name))
            .map(name -> Ingredient.builder().name(name).build())
            .toList();

        if (!toCreate.isEmpty()) {
            List<Ingredient> newlySaved = ingredientRepository.saveAll(toCreate);
            newlySaved.forEach(i -> masterMap.put(i.getName(), i));
        }

        return masterMap;
    }
    private YoutubeChannel updateOrCreateChannel(YoutubeChannel youtubeChannel, SearchResult res, Channel detail) {

        Long subscriberCount = Optional.ofNullable(detail)
            .map(Channel::getStatistics)
            .map(com.google.api.services.youtube.model.ChannelStatistics::getSubscriberCount)
            .map(BigInteger::longValueExact).orElse(null);
        Long viewCount = Optional.ofNullable(detail)
            .map(Channel::getStatistics)
            .map(com.google.api.services.youtube.model.ChannelStatistics::getViewCount)
            .map(BigInteger::longValueExact).orElse(null);
        Long videoCount = Optional.ofNullable(detail)
            .map(Channel::getStatistics)
            .map(com.google.api.services.youtube.model.ChannelStatistics::getVideoCount)
            .map(BigInteger::longValueExact).orElse(null);

        if (youtubeChannel!= null) {
            youtubeChannel.updateStatistics(subscriberCount, viewCount, videoCount);
            return youtubeChannel;
        } else {

            return YoutubeChannel.builder()
                .apiChannelId(res.getSnippet().getChannelId())
                .title(res.getSnippet().getChannelTitle())
                .subscriberCount(subscriberCount)
                .viewCount(viewCount)
                .videoCount(videoCount)
                .build();
        }
    }

    private YoutubeVideo updateOrCreateVideo(YoutubeVideo video, SearchResult searchResult, Video videoDetail,YoutubeChannel channel, Culinary culinary) {

        Long viewCount = Optional.ofNullable(videoDetail)
            .map(Video::getStatistics)
            .map(VideoStatistics::getViewCount)
            .map(BigInteger::longValueExact).orElse(null);
        Long likeCount = Optional.ofNullable(videoDetail)
            .map(Video::getStatistics)
            .map(VideoStatistics::getLikeCount)
            .map(BigInteger::longValueExact).orElse(null);
        Long commentCount = Optional.ofNullable(videoDetail)
            .map(Video::getStatistics)
            .map(VideoStatistics::getCommentCount)
            .map(BigInteger::longValueExact).orElse(null);

        if (video != null){
            video.updateStatistics(viewCount,likeCount,commentCount);
            return video;
        }
        else {
            String thumbnailUrl = Optional.ofNullable(videoDetail)
                .map(Video::getSnippet)
                .map(VideoSnippet::getThumbnails)
                .map(ThumbnailDetails::getMedium)
                .map(Thumbnail::getUrl)
                .orElse(null);

            return YoutubeVideo.builder()
                .apiVideoId(searchResult.getId().getVideoId())
                .title(searchResult.getSnippet().getTitle())
                .thumbnailUrl(thumbnailUrl)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .channel(channel)
                .culinary(culinary)
                .build();
        }

    }
}
