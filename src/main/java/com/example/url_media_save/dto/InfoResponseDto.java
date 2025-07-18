package com.example.url_media_save.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InfoResponseDto {
    private Integer searchFiles;
    private Integer downloadFiles;
    private Integer deleteDuplicates;
    private Long requiredTimeMs;
    private String fromUrl;

    private Integer totalProcessed;
    private Integer totalSuccessful;
    private Integer totalFailed;
    private Integer invalidUrls;
    private Integer urlsWithoutExtension;
    private List<String> failedDownloadUrls;
    private List<String> invalidUrlList;
    private List<String> urlsWithoutExtensionList;
}
