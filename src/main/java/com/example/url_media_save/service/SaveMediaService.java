package com.example.url_media_save.service;

import com.example.url_media_save.dto.DownloadResultDto;
import com.example.url_media_save.dto.InfoResponseDto;
import com.example.url_media_save.service.duplicate.FileDuplicatesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveMediaService {
    private final MediaParser mediaParser;
    private final FileDuplicatesService fileDuplicatesService;
    private final Saver saver;

    /**
     * Creates a map with URL as key and its file extension as value
     * @param url The URL to process
     * @return Map with URL and its extension
     */
    private Map<String, String> createUrlExtensionMap(String url) {
        Map<String, String> urlExtensionMap = new HashMap<>();
        urlExtensionMap.put(url, saver.getFileExtension(url));
        return urlExtensionMap;
    }

    /**
     * Extracts and downloads media files from a given URL
     * @param baseUrl The base URL to extract media from
     * @param pathToSave Directory path to save files
     * @param notSaveFileInKb Minimum file size in KB to save
     * @param checkNested Whether to check nested URLs
     * @return InfoResponseDto with download statistics
     * @throws IOException if connection fails
     */
    public InfoResponseDto getMediaFromUrl(String baseUrl, String pathToSave, int notSaveFileInKb, Boolean checkNested) throws IOException {
        if (!saver.isValidUrl(baseUrl)) {
            throw new IllegalArgumentException("Invalid base URL: " + baseUrl);
        }
        String cleanBaseUrl = baseUrl.substring(0, baseUrl.indexOf("/", 8));

        long start = System.currentTimeMillis();
        String pathName = Objects.requireNonNullElseGet(pathToSave, () -> "C:/temp/" + baseUrl.substring(baseUrl.lastIndexOf("/")));
        Integer downloadFiles = 0;
        List<String> urls;
        HttpURLConnection connection = null;
        
        // Collect all download results
        List<DownloadResultDto> allDownloadResults = new ArrayList<>();
        
        try {
            connection = UrlConnection.getHttpURLConnection(baseUrl);

            // Extract URLs from the main page
            urls = mediaParser.extractUrlsFromPage(mediaParser.getWebPageParse(connection), cleanBaseUrl);
            log.debug("URLs from main page: {}", urls);

            // Filter URLs with valid extensions
            List<String> mediaUrls = urls.stream()
                    .filter(s -> !saver.getFileExtension(s).isEmpty())
                    .toList();

            // Download files with valid extensions and get detailed results
            List<String> nestedUrls = urls.stream()
                    .parallel()
                    .map(this::createUrlExtensionMap)
                    .map(it -> {
                        DownloadResultDto result = saver.downloadUrlToFileWithDetails(it, pathName);
                        allDownloadResults.add(result);
                        return result.getUrlsWithoutExtension();
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            
            log.info("Map of nested URLs {}", nestedUrls);
            
            // Process nested URLs if enabled
            if (Boolean.TRUE.equals(checkNested)) {
                List<String> collect = nestedUrls.stream()
                        .filter(s -> s.startsWith(cleanBaseUrl))
                        .toList();
                tryToDownloadNested(new LinkedList<>(collect), pathName, cleanBaseUrl, notSaveFileInKb, allDownloadResults);
            }
            
            fileDuplicatesService.deleteFilesSize(pathName, notSaveFileInKb);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        // Delete duplicates
        int deleteDuplicatesNumber = 0;
        try {
            deleteDuplicatesNumber = fileDuplicatesService.deleteDuplicatesInFolder(pathName);
            log.info("Delete duplicates {}", deleteDuplicatesNumber);
        } catch (Exception e) {
            log.error("Failed to delete duplicates in folder {}: {}", pathName, e.getMessage(), e);
        }

        Long requiredTime = (System.currentTimeMillis() - start);
        log.info("It works: {} ms", requiredTime);
        
        // Aggregate download statistics
        int totalProcessed = allDownloadResults.stream().mapToInt(DownloadResultDto::getTotalProcessed).sum();
        int totalSuccessful = allDownloadResults.stream().mapToInt(DownloadResultDto::getTotalSuccessful).sum();
        int totalFailed = allDownloadResults.stream().mapToInt(DownloadResultDto::getTotalFailed).sum();
        int invalidUrls = allDownloadResults.stream().mapToInt(r -> r.getInvalidUrls().size()).sum();
        int urlsWithoutExtension = allDownloadResults.stream().mapToInt(r -> r.getUrlsWithoutExtension().size()).sum();
        
        List<String> allFailedDownloads = allDownloadResults.stream()
                .flatMap(r -> r.getFailedDownloads().stream())
                .collect(Collectors.toList());
        List<String> allInvalidUrls = allDownloadResults.stream()
                .flatMap(r -> r.getInvalidUrls().stream())
                .collect(Collectors.toList());
        List<String> allUrlsWithoutExtension = allDownloadResults.stream()
                .flatMap(r -> r.getUrlsWithoutExtension().stream())
                .collect(Collectors.toList());
        
        return InfoResponseDto.builder()
                .requiredTimeMs(requiredTime)
                .downloadFiles(totalSuccessful)
                .fromUrl(baseUrl)
                .searchFiles(urls.size())
                .deleteDuplicates(deleteDuplicatesNumber)
                .totalProcessed(totalProcessed)
                .totalSuccessful(totalSuccessful)
                .totalFailed(totalFailed)
                .invalidUrls(invalidUrls)
                .urlsWithoutExtension(urlsWithoutExtension)
                .failedDownloadUrls(allFailedDownloads)
                .invalidUrlList(allInvalidUrls)
                .urlsWithoutExtensionList(allUrlsWithoutExtension)
                .build();
    }

    /**
     * Attempts to download media from nested URLs
     * @param checkUrls Queue of URLs to check
     * @param pathName Directory path to save files
     * @param cleanBaseUrl Base URL for filtering
     * @param notSaveFileInKb Minimum file size in KB to save
     * @param allDownloadResults List to collect all download results
     */
    private void tryToDownloadNested(LinkedList<String> checkUrls, String pathName, String cleanBaseUrl,
                                     int notSaveFileInKb, List<DownloadResultDto> allDownloadResults) {
        while (!checkUrls.isEmpty()) {
            log.debug("Nested url: {}, left urls: {}", checkUrls.getFirst(), checkUrls.size());
            HttpURLConnection connection = null;
            String url = checkUrls.poll();
            List<String> urls;
            
            try {
                connection = UrlConnection.getHttpURLConnection(url);

                urls = mediaParser.extractUrlsFromPage(mediaParser.getWebPageParse(connection), cleanBaseUrl);
                log.debug("Find in [{}] links to download: {}", url, urls);

                urls.stream()
                        .parallel()
                        .map(this::createUrlExtensionMap)
                        .forEach(it -> {
                            DownloadResultDto result = saver.downloadUrlToFileWithDetails(it, pathName);
                            allDownloadResults.add(result);
                        });

                fileDuplicatesService.deleteFilesSize(pathName, notSaveFileInKb);

            } catch (IOException e) {
                log.error("Failed to process nested URL {}: {}", url, e.getMessage(), e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }
}
