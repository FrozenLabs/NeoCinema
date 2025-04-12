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

    private final int videoWidth = 1920;
    private final int videoHeight = 1090;
    private final int bufferSize = videoWidth * videoHeight * 4;

    private final int[] pboIds = new int[2];
    private final ByteBuffer stagingBuffer = ByteBuffer.allocateDirect(bufferSize);
    private int activePboIndex = 0;
    private boolean frameAvailable = false;
    private boolean hasPlayed = false;

    private final int textureIdVideo;

    public VideoLanPlayback() {
        mediaPlayerFactory = new MediaPlayerFactory(
                "--no-video-title-show",
                ":avcodec-hw=dxva2",
                ":avcodec-codec=h264",
                "--codec=avcodec"
        );
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        GL15.glGenBuffers(pboIds);
        for (int pboId : pboIds) {
            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboId);
            GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER, bufferSize, GL15.GL_STREAM_DRAW);
        }
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);

        mediaPlayer.videoSurface().set(new CallbackVideoSurface(
                new DirectBufferFormatCallback(),
                new DirectRenderCallback(),
                true
        ));

        textureIdVideo = emptyTexture(videoWidth, videoHeight);
        glBindTexture(GL_TEXTURE_2D, textureIdVideo);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void play(String media, String... options) {
        mediaPlayer.media().play(media, options);
        hasPlayed = true;
    }

    public void sync() {
        if (!hasPlayed || !frameAvailable) return;

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
                    videoWidth,
                    videoHeight,
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

    private static final class DirectBufferFormatCallback extends BufferFormatCallbackAdapter {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
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
                nativeBuffer.rewind();
                stagingBuffer.rewind();
                stagingBuffer.put(nativeBuffer);
                frameAvailable = true;
            }
        }
    }
}