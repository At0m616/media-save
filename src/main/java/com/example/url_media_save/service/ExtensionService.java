package com.example.url_media_save.service;


import java.util.List;

public class ExtensionService {
    private static final List<String> validExtension = List.of(".jpg", ".jpeg", ".gif", ".mp4", ".mp3", ".png");

    public static String getFileExtension(String url) {
        if (url.lastIndexOf(".") != -1 && url.lastIndexOf(".") != 0) {
            String extension = url.substring(url.lastIndexOf("."));
            if (validExtension.stream().anyMatch(extension::equals))
                return extension;
        }
        return "";
    }
}
