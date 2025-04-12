package com.neocinema.fabric.util;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20C;

import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL42C.glTexStorage2D;

public class TextureUtil {
    public static int emptyTexture(int width, int height) {
        int textureID = GL11.glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, width, height);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        return textureID;
    }

    public static void resetTextureUploadState() {
        GlStateManager._pixelStore(GL20C.GL_UNPACK_ROW_LENGTH, 0);
        GlStateManager._pixelStore(GL20C.GL_UNPACK_SKIP_ROWS, 0);
        GlStateManager._pixelStore(GL20C.GL_UNPACK_SKIP_PIXELS, 0);
        GlStateManager._pixelStore(GL20C.GL_UNPACK_ALIGNMENT, 4);
    }
}
