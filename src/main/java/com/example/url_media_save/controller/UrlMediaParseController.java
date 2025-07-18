package com.example.url_media_save.controller;

import com.example.url_media_save.db.entity.UrlRequestEntity;
import com.example.url_media_save.db.service.UrlService;
import com.example.url_media_save.dto.InfoResponseDto;
import com.example.url_media_save.dto.UrlRequestDto;
import com.example.url_media_save.service.SaveMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UrlMediaParseController {

    private final SaveMediaService saveMediaService;
    private final UrlService urlService;

    @PostMapping("/mediaget")
    ResponseEntity<?> getMedia(@RequestBody UrlRequestDto url, @RequestHeader HttpHeaders headers) {
        log.info("Request from: {} time: {}", headers, url.getRequestTime());
        InfoResponseDto mediaFromUrl;
        try {
            UrlRequestEntity entity = urlService.save(url);
            mediaFromUrl = saveMediaService.getMediaFromUrl(url.getUrl(), url.getPathToSave(), url.getNotSaveFileInKb(), url.getCheckNested());
            urlService.saveResultInfo(entity, mediaFromUrl.getRequiredTimeMs(), mediaFromUrl.getDeleteDuplicates(), mediaFromUrl.getTotalProcessed(), mediaFromUrl.getTotalSuccessful(), mediaFromUrl.getTotalFailed());
        } catch (Exception e) {
            log.error("Bad request: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mediaFromUrl);
    }

}
