package com.neocinema.fabric.video.playback;

import org.lwjgl.opengl.*;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.StandardBufferFormat;

import java.nio.ByteBuffer;

import static com.neocinema.fabric.util.TextureUtil.emptyTexture;
import static com.neocinema.fabric.util.TextureUtil.resetTextureUploadState;
import static org.lwjgl.opengl.GL11.*;

public final class VideoLanPlayback {
    private final MediaPlayerFactory mediaPlayerFactory;
    private final EmbeddedMediaPlayer mediaPlayer;

    private int sourceWidth;
    private int sourceHeight;
    private int targetWidth;
    private int targetHeight;
    private int bufferSize;
    private boolean dimensionsChanged = false;

    private final int[] pboIds = new int[2];
    private ByteBuffer stagingBuffer;
    private int activePboIndex = 0;
    private boolean frameAvailable = false;
    private boolean hasPlayed = false;

    private int textureIdVideo;

    public VideoLanPlayback() {
        mediaPlayerFactory = new MediaPlayerFactory(
                "--no-video-title-show",
                ":avcodec-hw=dxva2",
                ":avcodec-codec=h264",
                "--codec=avcodec"
        );
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        mediaPlayer.videoSurface().set(new CallbackVideoSurface(
                new DirectBufferFormatCallback(),
                new DirectRenderCallback(),
                true
        ));

        textureIdVideo = 0;
        stagingBuffer = ByteBuffer.allocateDirect(0);
    }

    public void play(String media, String... options) {
        mediaPlayer.media().play(media, options);
        hasPlayed = true;
    }

    public void sync() {
        if (!hasPlayed) return;

        if (dimensionsChanged) {
            initGraphics();
            dimensionsChanged = false;
        }

        if (!frameAvailable) return;

        synchronized (this) {
            final int pboIndex = activePboIndex;
            activePboIndex = (activePboIndex + 1) % pboIds.length;

            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboIds[pboIndex]);

            ByteBuffer pboBuffer = GL30.glMapBufferRange(
                    GL21.GL_PIXEL_UNPACK_BUFFER,
                    0,
                    bufferSize,
                    GL30.GL_MAP_WRITE_BIT | GL30.GL_MAP_INVALIDATE_BUFFER_BIT,
                    null
            );

            if (pboBuffer != null) {
                stagingBuffer.rewind();
                pboBuffer.put(stagingBuffer);
                GL15.glUnmapBuffer(GL21.GL_PIXEL_UNPACK_BUFFER);
            }

            resetTextureUploadState();
            glBindTexture(GL_TEXTURE_2D, textureIdVideo);
            GL11.glTexSubImage2D(
                    GL_TEXTURE_2D,
                    0,
                    0,
                    0,
                    targetWidth,
                    targetHeight,
                    GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE,
                    0
            );

            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
            frameAvailable = false;
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.controls().stop();
            mediaPlayer.release();
        }
        if (mediaPlayerFactory != null) {
            mediaPlayerFactory.release();
        }
        GL15.glDeleteBuffers(pboIds);
        GL11.glDeleteTextures(textureIdVideo);
    }

    public int texture() {
        return textureIdVideo;
    }

    public MediaPlayer mediaPlayer() {
        return mediaPlayer;
    }

    private void initGraphics() {
        if (textureIdVideo != 0) {
            GL11.glDeleteTextures(textureIdVideo);
        }
        GL15.glDeleteBuffers(pboIds);

        bufferSize = targetWidth * targetHeight * 4;
        stagingBuffer = ByteBuffer.allocateDirect(bufferSize);

        GL15.glGenBuffers(pboIds);
        for (int pboId : pboIds) {
            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboId);
            GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER, bufferSize, GL15.GL_STREAM_DRAW);
        }
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);

        textureIdVideo = emptyTexture(targetWidth, targetHeight);
        glBindTexture(GL_TEXTURE_2D, textureIdVideo);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private final class DirectBufferFormatCallback extends BufferFormatCallbackAdapter {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            VideoLanPlayback.this.sourceWidth = sourceWidth;
            VideoLanPlayback.this.sourceHeight = sourceHeight;

            float targetAspect = 16.0f / 9.0f;
            float sourceAspect = (float) sourceWidth / sourceHeight;

            if (sourceAspect > targetAspect) {
                targetWidth = sourceWidth;
                targetHeight = (int) (targetWidth / targetAspect);
            } else {
                targetHeight = sourceHeight;
                targetWidth = (int) (targetHeight * targetAspect);
            }

            dimensionsChanged = true;

            return new StandardBufferFormat(sourceWidth, sourceHeight);
        }
    }

    private final class DirectRenderCallback implements RenderCallback {
        @Override
        public void lock(MediaPlayer mediaPlayer) {}

        @Override
        public void unlock(MediaPlayer mediaPlayer) {}

        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat, int displayHeight, int displayWidth) {
            if (!mediaPlayer.status().isPlaying()) return;

            ByteBuffer nativeBuffer = nativeBuffers[0];
            synchronized (VideoLanPlayback.this) {
                if (stagingBuffer == null || stagingBuffer.capacity() != bufferSize) {
                    return;
                }

                stagingBuffer.rewind();
                for (int i = 0; i < bufferSize; i += 4) {
                    stagingBuffer.put((byte) 0);     // R
                    stagingBuffer.put((byte) 0);     // G
                    stagingBuffer.put((byte) 0);     // B
                    stagingBuffer.put((byte) 0xFF);  // A (opaque)
                }

                stagingBuffer.rewind();

                int targetX = Math.max(0, (targetWidth - sourceWidth) / 2);
                int targetY = Math.max(0, (targetHeight - sourceHeight) / 2);

                int copyWidth = Math.min(sourceWidth, targetWidth - targetX);
                int copyHeight = Math.min(sourceHeight, targetHeight - targetY);

                for (int y = 0; y < copyHeight; y++) {
                    int srcPos = y * sourceWidth * 4;
                    int destPos = ((targetY + y) * targetWidth + targetX) * 4;

                    nativeBuffer.position(srcPos);
                    nativeBuffer.limit(srcPos + copyWidth * 4);
                    ByteBuffer srcRow = nativeBuffer.slice();

                    stagingBuffer.position(destPos);
                    stagingBuffer.put(srcRow);
                }

                frameAvailable = true;
            }
        }
    }
}