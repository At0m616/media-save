package com.example.url_media_save.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MediaParser {
    /**
     * Returns the site as a string
     */
    @SneakyThrows
    public String getWebPageParse(HttpURLConnection connection) {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        StringBuilder webParse = new StringBuilder();
        String now;
        while ((now = in.readLine()) != null) {
            webParse.append(now);
        }
//        log.trace("Page Parse: {}", webParse);
        return webParse.toString();
    }

    /**
     * Returns a list with all links contained in the input
     */
    public List<String> extractUrlsFromPage(String webPageParse, String cleanUrl) {
        List<String> containedUrls = new ArrayList<>();
//        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
//        Pattern patternUrl = Pattern.compile("(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])");
        matchingUrlWithoutSiteName(webPageParse, containedUrls, cleanUrl);
        matchingUrlWithoutHttp(webPageParse, containedUrls);
        log.info("Valid url in file: " + containedUrls);
        log.info("Files to download: " + containedUrls.size());
        return containedUrls;
    }

    private void matchingUrlWithoutHttp(String text, List<String> containedUrls) {
        Pattern patternUrlWithoutHttp = Pattern.compile("\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])");
        Matcher withoutHttpMatcher = patternUrlWithoutHttp.matcher(text);
        while (withoutHttpMatcher.find()) {
            containedUrls.add("https:" + text.substring(withoutHttpMatcher.start(0),
                    withoutHttpMatcher.end(0)));
        }
    }

    private void matchingUrlWithoutSiteName(String text, List<String> containedUrls, String cleanUrl) {
        Pattern patternVideo = Pattern.compile("/file/[a-zA-Z0-9]*\\.\\w+");
        Matcher videoMatcher = patternVideo.matcher(text);
        while (videoMatcher.find()) {
            containedUrls.add(cleanUrl + text.substring(videoMatcher.start(0),
                    videoMatcher.end(0)));
        }
    }
}
