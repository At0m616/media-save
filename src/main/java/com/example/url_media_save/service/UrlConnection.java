package com.example.url_media_save.service;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@UtilityClass
@Slf4j
public class UrlConnection {
    static {
        TrustManager[] trustAllCertificates = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null; // Not relevant.
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Do nothing. Just allow them all.
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Do nothing. Just allow them all.
                    }
                }
        };

        HostnameVerifier trustAllHostnames = (hostname, session) -> {
            return true; // Just allow them all.
        };

        try {
            System.setProperty("jsse.enableSNIExtension", "false");
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCertificates, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostnames);
        }
        catch (GeneralSecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static HttpsURLConnection getHttpURLConnection(String toDownload) throws IOException {
        URL url = new URL(toDownload);
        URLConnection urlConnection = url.openConnection();
        urlConnection.addRequestProperty("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
        if (urlConnection instanceof HttpsURLConnection connection) {
            return (HttpsURLConnection) urlConnection;
        } else {
            throw new UnsupportedOperationException("Невалидный URL");
        }
    }
}
