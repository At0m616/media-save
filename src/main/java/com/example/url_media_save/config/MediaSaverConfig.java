package com.example.url_media_save.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties()
public class MediaSaverConfig {
    private int connectTimeout = 10000;
    private int readTimeout = 10000;
    private int maxFilenameLength = 100;
    private String defaultSavePath = "C:/temp";
    private boolean useUrlAsFilename = false;
} 