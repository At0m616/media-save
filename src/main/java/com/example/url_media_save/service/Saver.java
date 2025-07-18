package com.example.url_media_save.service;

import com.example.url_media_save.config.MediaSaverConfig;
import com.example.url_media_save.dto.DownloadResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class Saver {
    private static final List<String> validExtension = List.of(".jpg", ".jpeg", ".gif", ".mp4", ".mp3", ".png");
    
    private final MediaSaverConfig config;


    /**
     * Downloads media files from URLs with their extensions
     * @param urlToExtensionMap Map of URL to file extension
     * @param pathName Directory path to save files
     * @return List of URLs that couldn't be downloaded due to missing extensions
     */
    public List<String> downloadUrlToFile(Map<String, String> urlToExtensionMap, String pathName) {
        return downloadUrlToFile(urlToExtensionMap, pathName, config.isUseUrlAsFilename());
    }

    /**
     * Downloads media files from URLs with their extensions
     * @param urlToExtensionMap Map of URL to file extension
     * @param pathName Directory path to save files
     * @param useUrlAsFileName Whether to use URL filename instead of random name
     * @return List of URLs that couldn't be downloaded due to missing extensions
     */
    public List<String> downloadUrlToFile(Map<String, String> urlToExtensionMap, String pathName, boolean useUrlAsFileName) {
        long start = System.currentTimeMillis();
        List<String> checkUrl = new ArrayList<>();
        List<String> failedDownloads = new ArrayList<>();
        
        urlToExtensionMap.forEach((url, extension) -> {
            if (!isValidUrl(url)) {
                log.warn("Invalid URL: {}", url);
                checkUrl.add(url);
                return;
            }
            
            if (extension.length() == 0) {
                log.debug("Bad extension: {}", url);
                checkUrl.add(url);
            } else {
                try {
                    log.debug("try to save {}", url);
                    String fileName = useUrlAsFileName ? extractFileNameFromUrl(url, extension) : null;
                    File destFile = new File(getFullPathName(pathName, extension, fileName));
                    downloadWithGzipSupport(new URL(url), destFile, config.getConnectTimeout(), config.getReadTimeout());
                } catch (IOException e) {
                    log.error("Failed to save {}: {}", url, e.getMessage(), e);
                    failedDownloads.add(url);
                }
            }
        });
        log.debug(Thread.currentThread() + ": " + (System.currentTimeMillis() - start) + " ms");
        
        if (!failedDownloads.isEmpty()) {
            log.warn("Failed to download {} files: {}", failedDownloads.size(), failedDownloads);
        }
        
        return checkUrl;
    }

    /**
     * Скачивает файл с поддержкой автоматической распаковки gzip-ответов
     */
    private void downloadWithGzipSupport(URL url, File destFile, int connectTimeout, int readTimeout) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(connectTimeout);
        urlConnection.setReadTimeout(readTimeout);
        // Копируем все заголовки, как в getUrlConnection
        urlConnection.addRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
        urlConnection.addRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        urlConnection.addRequestProperty("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        urlConnection.addRequestProperty("Accept-Encoding", "gzip, deflate");
        // urlConnection.addRequestProperty("Referer", "https://example.com/"); // если нужно

        try (InputStream is = urlConnection.getInputStream();
             OutputStream os = Files.newOutputStream(destFile.toPath())) {
            String encoding = urlConnection.getHeaderField("Content-Encoding");
            InputStream actualIs = is;
            if (encoding != null && encoding.toLowerCase().contains("gzip")) {
                actualIs = new GZIPInputStream(is);
            }
            actualIs.transferTo(os);
        }
    }

    /**
     * Downloads media files and returns detailed results
     * @param urlToExtensionMap Map of URL to file extension
     * @param pathName Directory path to save files
     * @param useUrlAsFileName Whether to use URL filename instead of random name
     * @return DownloadResultDto with detailed download statistics
     */
    public DownloadResultDto downloadUrlToFileWithDetails(Map<String, String> urlToExtensionMap, String pathName, boolean useUrlAsFileName) {
        long start = System.currentTimeMillis();
        List<String> successfulDownloads = new ArrayList<>();
        List<String> failedDownloads = new ArrayList<>();
        List<String> invalidUrls = new ArrayList<>();
        List<String> urlsWithoutExtension = new ArrayList<>();
        
        urlToExtensionMap.forEach((url, extension) -> {
            if (!isValidUrl(url)) {
                log.warn("Invalid URL: {}", url);
                invalidUrls.add(url);
                return;
            }
            
            if (extension.length() == 0) {
                log.debug("Bad extension: {}", url);
                urlsWithoutExtension.add(url);
            } else {
                try {
                    log.debug("try to save {}", url);
                    String fileName = useUrlAsFileName ? extractFileNameFromUrl(url, extension) : null;
                    File destFile = new File(getFullPathName(pathName, extension, fileName));
                    downloadWithGzipSupport(new URL(url), destFile, config.getConnectTimeout(), config.getReadTimeout());
                    successfulDownloads.add(url);
                } catch (IOException e) {
                    log.error("Failed to save {}: {}", url, e.getMessage(), e);
                    failedDownloads.add(url);
                }
            }
        });
        
        long processingTime = System.currentTimeMillis() - start;
        log.debug(Thread.currentThread() + ": " + processingTime + " ms");
        
        return DownloadResultDto.builder()
                .successfulDownloads(successfulDownloads)
                .failedDownloads(failedDownloads)
                .invalidUrls(invalidUrls)
                .urlsWithoutExtension(urlsWithoutExtension)
                .totalProcessed(urlToExtensionMap.size())
                .totalSuccessful(successfulDownloads.size())
                .totalFailed(failedDownloads.size() + invalidUrls.size() + urlsWithoutExtension.size())
                .processingTimeMs(processingTime)
                .build();
    }

    /**
     * Downloads media files and returns detailed results using default settings
     * @param urlToExtensionMap Map of URL to file extension
     * @param pathName Directory path to save files
     * @return DownloadResultDto with detailed download statistics
     */
    public DownloadResultDto downloadUrlToFileWithDetails(Map<String, String> urlToExtensionMap, String pathName) {
        return downloadUrlToFileWithDetails(urlToExtensionMap, pathName, config.isUseUrlAsFilename());
    }

    /**
     * Validates if the given string is a valid URL
     * @param urlString The string to validate as URL
     * @return true if valid URL, false otherwise
     */
    public boolean isValidUrl(String urlString) {
        if (StringUtils.isBlank(urlString)) {
            return false;
        }
        
        try {
            new URL(urlString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts filename from URL and sanitizes it for file system
     * @param url The URL to extract filename from
     * @param extension File extension to append
     * @return Sanitized filename with extension
     */
    private String extractFileNameFromUrl(String url, String extension) {
        try {
            String path = new URL(url).getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            
            // Remove query parameters and fragments
            fileName = fileName.split("[?#]")[0];
            
            // If filename is empty or just extension, use a fallback
            if (StringUtils.isBlank(fileName) || fileName.equals(extension)) {
                return null; // Will use random name
            }
            
            // Sanitize filename for file system
            fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            
            // Ensure filename is not too long
            if (fileName.length() > config.getMaxFilenameLength()) {
                fileName = fileName.substring(0, config.getMaxFilenameLength());
            }
            
            return fileName;
        } catch (Exception e) {
            log.debug("Could not extract filename from URL {}: {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * Generates full path name for file
     * @param pathName Directory path
     * @param fileExtension File extension
     * @param customFileName Optional custom filename (without extension)
     * @return Full path with filename and extension
     */
    private String getFullPathName(String pathName, String fileExtension, String customFileName) {
        String fileName = customFileName != null ? customFileName : RandomStringUtils.randomAlphabetic(15);
        return String.format("%s/%s%s", pathName, fileName, fileExtension);
    }

    /**
     * Generates full path name for file with random filename
     * @param pathName Directory path
     * @param fileExtension File extension
     * @return Full path with random filename and extension
     */
    private String getFullPathName(String pathName, String fileExtension) {
        return getFullPathName(pathName, fileExtension, null);
    }

    /**
     * Extracts file extension from URL
     * @param url The URL to extract extension from
     * @return File extension in lowercase or empty string if not found
     */
    public String getFileExtension(String url) {
        int lastDot = url.lastIndexOf(".");
        if (lastDot != -1 && lastDot != 0) {
            String extension = url.substring(lastDot).split("[?&#]", 2)[0].toLowerCase();
            if (validExtension.contains(extension)) {
                return extension;
            }
        }
        return "";
    }
}
