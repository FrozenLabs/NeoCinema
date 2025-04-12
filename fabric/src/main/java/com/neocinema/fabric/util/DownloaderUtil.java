package com.neocinema.fabric.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class DownloaderUtil {
    private static final int BUFFER_SIZE = 8192;
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 30_000;

    public static void download(String fileName, String fileUrl, String savePath) throws IOException {
        Logger LOGGER = LoggerFactory.getLogger("Downloader-%s".formatted(fileName));

        HttpURLConnection connection = null;
        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "DownloaderUtil/1.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error " + responseCode + ": " + connection.getResponseMessage());
            }

            long contentLength = connection.getContentLengthLong();
            try (
                    InputStream in = connection.getInputStream();
                    FileOutputStream out = new FileOutputStream(savePath)
            ) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    if (contentLength > 0) {
                        int progress = (int) ((totalBytes * 100) / contentLength);
                        if (progress % 10 == 0) {
                            LOGGER.info("[{}] Downloading... {}%", fileName, progress);
                        }
                    }
                }

                if (contentLength > 0) {
                    LOGGER.info("[{}] Download complete!", fileName);
                }
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
