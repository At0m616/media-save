package com.example.url_media_save.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DownloadResultDto {
    private List<String> successfulDownloads;
    private List<String> failedDownloads;
    private List<String> invalidUrls;
    private List<String> urlsWithoutExtension;
    private int totalProcessed;
    private int totalSuccessful;
    private int totalFailed;
    private long processingTimeMs;
} 