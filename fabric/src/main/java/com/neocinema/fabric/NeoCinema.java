package com.neocinema.fabric;

import com.neocinema.fabric.util.DependencyDownloader;
import com.sun.jna.NativeLibrary;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import javax.swing.*;

public class NeoCinema implements ModInitializer {

    public static final String MODID = "neocinema";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        DependencyDownloader.downloadDependencies();

        NativeLibrary.addSearchPath(
                RuntimeUtil.getLibVlcLibraryName(),
                DependencyDownloader.LIBRARIES_DIRECTORY.toAbsolutePath().toString()
        );

        boolean result = new NativeDiscovery().discover();

        if (!result) {
            LOGGER.warn("Could not discover NeoCinema libraries! Video playback will not work.");
            errorDialog("""
                    NeoCinema failed to load required native libraries.
                    Video playback will be disabled.
                    
                    If you're on Linux, ensure you have the latest VLC 4.0.0 nightlies installed on your system.""");
        }
    }

    private void errorDialog(String message) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        message,
                        "NeoCinema Initialization Error",
                        JOptionPane.ERROR_MESSAGE
                );
            });
        } catch (Exception e) {
            LOGGER.error("Failed to show error dialog: {}", e.getMessage(), e);
        }
    }
}
