package com.neocinema.fabric.mixins.cef;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.cef.CefUtil;
import com.neocinema.fabric.cef.Platform;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * A mixin is used here to load JCEF at the earliest point in the MC bootstrap process
 * See: net.minecraft.client.main.Main
 * This reduces issues with CEF initialization
 * Due to AWT issues on macOS, we cannot initialize CEF here
 */
@Mixin(Main.class)
public class MixinCefInit {
    @Unique
    private static void setUnixExecutable(File file) {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        try {
            Files.setPosixFilePermissions(file.toPath(), perms);
        } catch (IOException e) {
            // Ignore
        }
    }

    @Unique
    private static void setupLibraryPath(Platform platform) throws IOException, URISyntaxException {
        // Check for development environment
        // i.e. neocinema-repo/build/cef/<platform>
        File cefPlatformDir = new File("../build/cef/" + platform.getNormalizedName());
        if (cefPlatformDir.exists()) {
            System.setProperty("jcef.path", cefPlatformDir.getCanonicalPath());
            return;
        }

        // Check for .minecraft/mods/neocinema-libraries directory, create if not exists
        File neocinemaLibrariesDir = new File("mods/neocinema-libraries");
        if (!neocinemaLibrariesDir.exists()) {
            if (!neocinemaLibrariesDir.mkdirs()) throw new IOException("Failed to create directory " + neocinemaLibrariesDir.getAbsolutePath());
        }
        System.setProperty("jcef.path", neocinemaLibrariesDir.getCanonicalPath());

        //
        // CEF library extraction
        //
        URL cefManifestURL = MixinCefInit.class.getClassLoader().getResource("cef/manifest.txt");

        if (cefManifestURL == null) {
            return;
        }

        try (InputStream cefManifestInputStream = cefManifestURL.openStream();
             Scanner scanner = new Scanner(cefManifestInputStream)) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                // String fileHash = line.split(" {2}")[0]; // TODO: check hash
                String relFilePath = line.split(" {2}")[1];
                URL cefResourceURL = MixinCefInit.class.getClassLoader().getResource("cef/" + relFilePath);

                if (cefResourceURL == null) {
                    continue;
                }

                try (InputStream cefResourceInputStream = cefResourceURL.openStream()) {
                    File cefResourceFile = new File(neocinemaLibrariesDir, relFilePath);

                    if (cefResourceFile.exists()) {
                        continue;
                    }

                    // For when we run across a nested file, i.e. locales/sl.pak
                    if (!cefResourceFile.getParentFile().mkdirs()) throw new IOException("Failed to create directory " + cefResourceFile.getParentFile().getAbsolutePath());

                    Files.copy(cefResourceInputStream, cefResourceFile.toPath());
                    if (platform.isLinux()) {
                        if (cefResourceFile.getName().contains("chrome-sandbox")
                                || cefResourceFile.getName().contains("jcef_helper")) {
                            setUnixExecutable(cefResourceFile);
                        }
                    }
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "main ([Ljava/lang/String;)V", remap = false)
    private static void cefInit(CallbackInfo info) {
        Platform platform = Platform.getPlatform();

        try {
            setupLibraryPath(platform);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (platform.isLinux()) {
            System.loadLibrary("jawt");
        }

        if (platform.isLinux() || platform.isWindows()) {
            if (CefUtil.init()) {
                NeoCinema.LOGGER.info("Chromium Embedded Framework initialized");
            } else {
                NeoCinema.LOGGER.warn("Could not initialize Chromium Embedded Framework");
            }
        }
    }

}
