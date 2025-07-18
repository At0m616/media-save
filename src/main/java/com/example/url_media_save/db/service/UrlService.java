package com.example.url_media_save.db.service;

import com.example.url_media_save.db.mapper.UrlMapper;
import com.example.url_media_save.db.repository.UrlRepository;
import com.example.url_media_save.dto.UrlRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.url_media_save.db.entity.UrlRequestEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlRepository urlRepository;
    private final UrlMapper urlMapper;

    public UrlRequestEntity save(UrlRequestDto urlRequestDto) {
       return urlRepository.save(urlMapper.toEntity(urlRequestDto));
    }

    @Transactional
    public void saveResultInfo(UrlRequestEntity entity, Long requiredTimeMs, Integer deleteDuplicates, Integer totalProcessed, Integer totalSuccessful, Integer totalFailed) {
        urlMapper.updateResultFields(entity, requiredTimeMs, deleteDuplicates, totalProcessed, totalSuccessful, totalFailed);
        urlRepository.save(entity);
    }
}
