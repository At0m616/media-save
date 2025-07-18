package com.example.url_media_save.db.service;

import com.example.url_media_save.db.mapper.UrlMapper;
import com.example.url_media_save.db.repository.UrlRepository;
import com.example.url_media_save.dto.UrlRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlRepository urlRepository;
    private final UrlMapper urlMapper;

    public boolean save(UrlRequestDto urlRequestDto) {
        try {
            urlRepository.save(urlMapper.toUrlModel(urlRequestDto));
        } catch (Exception e) {
            log.error("Can't save to db: {}", e.getMessage());
            return false;
        }
        return true;
    }

}
