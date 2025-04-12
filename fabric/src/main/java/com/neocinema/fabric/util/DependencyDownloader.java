package com.neocinema.fabric.util;

import com.neocinema.fabric.NeoCinema;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class DependencyDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyDownloader.class);

    private static final Path ROOT = MinecraftClient.getInstance().runDirectory.toPath().resolve(NeoCinema.MODID);

    private static final Path FILE = ROOT.resolve("downloads");
    public static final Path LIBRARIES_DIRECTORY = ROOT.resolve("vlc");

    private static final String VERSION = "4.0.0";
    private static final String LIBRARY = "libvlc";

    public static void downloadDependencies() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        if (osName.contains("linux")) return;

        String platform = findPlatform(osName, arch);
        if (platform == null) {
            LOGGER.warn("Unsupported platform: {} {}", osName, arch);
            return;
        }

        String dependency = "%s-%s-%s".formatted(LIBRARY, VERSION, platform);
        String fileName = "%s.zip".formatted(dependency);
        String fileUrl = "https://stream.lunasa.dev/dependencies/%s/%s/%s".formatted(LIBRARY, VERSION, fileName);
        String shaUrl = fileUrl + ".sha256";

        try {
            String expectedHash = findHash(shaUrl);
            if (expectedHash == null) throw new IOException("Failed to fetch SHA256 hash.");

            if (Files.exists(FILE)) {
                String actualHash = sha256().toLowerCase();
                if (!actualHash.equalsIgnoreCase(expectedHash)) {
                    LOGGER.warn("Cached file hash mismatch. Redownloading...");
                    Files.delete(FILE);
                } else {
                    LOGGER.info("Using cached dependency: {}", fileName);
                }
            }

            if (!Files.exists(FILE)) {
                Files.createDirectories(FILE.getParent());
                DownloaderUtil.download(fileName, fileUrl, FILE.toString());

                String actualHash = sha256().toLowerCase();
                if (!actualHash.equalsIgnoreCase(expectedHash)) {
                    Files.delete(FILE);
                    throw new IOException("Downloaded file hash does not match expected SHA256.");
                }
                LOGGER.info("Downloaded and verified: {}", fileName);
            }

            extract();
            LOGGER.info("Extraction complete to {}", LIBRARIES_DIRECTORY);

        } catch (Exception e) {
            LOGGER.error("Failed to download and extract dependencies: {}", e.getMessage(), e);
        }
    }

    private static String findPlatform(String osName, String arch) {
        if (osName.contains("win")) {
            return arch.contains("64") ? "win64" : null;
        } else if (osName.contains("mac")) {
            return arch.contains("aarch64") || arch.contains("arm") ? "macos-arm64" : "macos-amd64";
        }
        return null;
    }

    private static String findHash(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (InputStream in = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                return reader.readLine().trim().toLowerCase();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to fetch SHA256 from {}: {}", url, e.getMessage());
            return null;
        }
    }

    private static String sha256() throws IOException {
        try (InputStream fis = Files.newInputStream(DependencyDownloader.FILE)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }

            byte[] hashBytes = digest.digest();
            StringBuilder result = new StringBuilder();
            for (byte b : hashBytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new IOException("SHA-256 calculation failed", e);
        }
    }

    private static void extract() throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(DependencyDownloader.FILE))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                Path filePath = DependencyDownloader.LIBRARIES_DIRECTORY.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zipIn, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipIn.closeEntry();
            }
        }
    }
}
