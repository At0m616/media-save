package com.example.url_media_save.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UrlRequestDto {
    String url;
    String pathToSave;
    Integer notSaveFileInKb;
    Boolean checkNested;
    LocalDateTime requestTime = LocalDateTime.now();
}
