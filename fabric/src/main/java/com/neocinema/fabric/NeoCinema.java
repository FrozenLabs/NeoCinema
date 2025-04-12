package com.neocinema.fabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

public class NeoCinema implements ModInitializer {

    public static final String MODID = "neocinema";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        System.out.println(new NativeDiscovery().discover());
    }
}
