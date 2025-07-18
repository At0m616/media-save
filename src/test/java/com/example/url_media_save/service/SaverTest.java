package com.example.url_media_save.service;

import com.example.url_media_save.config.MediaSaverConfig;
import com.example.url_media_save.dto.DownloadResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SaverTest {

    private Saver saver;
    private MediaSaverConfig config;

    @BeforeEach
    void setUp() {
        config = new MediaSaverConfig();
        config.setConnectTimeout(5000);
        config.setReadTimeout(5000);
        config.setMaxFilenameLength(50);
        config.setUseUrlAsFilename(false);
        saver = new Saver(config);
    }

    @Test
    void testGetFileExtension() {
        assertEquals(".jpg", saver.getFileExtension("https://example.com/image.jpg"));
        assertEquals(".png", saver.getFileExtension("https://example.com/image.png?param=value"));
        assertEquals(".mp4", saver.getFileExtension("https://example.com/video.mp4#fragment"));
        assertEquals("", saver.getFileExtension("https://example.com/page"));
        assertEquals("", saver.getFileExtension("https://example.com/image.txt"));
    }

    @Test
    void testIsValidUrl() {
        assertTrue(saver.isValidUrl("https://example.com/image.jpg"));
        assertTrue(saver.isValidUrl("http://example.com/image.png"));
        assertFalse(saver.isValidUrl("not-a-url"));
        assertFalse(saver.isValidUrl(""));
        assertFalse(saver.isValidUrl(null));
    }

    @Test
    void testDownloadUrlToFileWithDetails(@TempDir Path tempDir) {
        Map<String, String> urlToExtensionMap = new HashMap<>();
        urlToExtensionMap.put("https://example.com/image.jpg", ".jpg");
        urlToExtensionMap.put("https://example.com/image.png", ".png");
        urlToExtensionMap.put("invalid-url", "");
        urlToExtensionMap.put("https://example.com/page", "");

        DownloadResultDto result = saver.downloadUrlToFileWithDetails(urlToExtensionMap, tempDir.toString());

        assertEquals(4, result.getTotalProcessed());
        assertEquals(0, result.getTotalSuccessful()); // Should fail due to invalid URLs
        assertEquals(4, result.getTotalFailed());
        assertEquals(1, result.getInvalidUrls().size());
        assertEquals(1, result.getUrlsWithoutExtension().size());
        assertTrue(result.getInvalidUrls().contains("invalid-url"));
    }

    @Test
    void testDownloadUrlToFile(@TempDir Path tempDir) {
        Map<String, String> urlToExtensionMap = new HashMap<>();
        urlToExtensionMap.put("https://example.com/image.jpg", ".jpg");
        urlToExtensionMap.put("https://example.com/page", "");

        // This should return URLs without extensions
        var result = saver.downloadUrlToFile(urlToExtensionMap, tempDir.toString());
        
        assertEquals(1, result.size());
        assertTrue(result.contains("https://example.com/page"));
    }
}