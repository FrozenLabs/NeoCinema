package com.neocinema.fabric.util;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public final class ImageUtil {

    public static CompletableFuture<NativeImageBackedTexture> fetchImageTextureFromUrl(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                try (InputStream stream = URI.create(url).toURL().openStream()) {
                    return new NativeImageBackedTexture(NativeImage.read(stream));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

}
