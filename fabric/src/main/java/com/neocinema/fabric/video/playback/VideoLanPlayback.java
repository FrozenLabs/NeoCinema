package com.neocinema.fabric.video.playback;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.StandardBufferFormat;

import java.nio.ByteBuffer;

import static com.neocinema.fabric.util.TextureUtil.emptyTexture;
import static com.neocinema.fabric.util.TextureUtil.resetTextureUploadState;
import static org.lwjgl.opengl.GL11.*;

public class VideoLanPlayback {
    private final MediaPlayerFactory mediaPlayerFactory;
    private final EmbeddedMediaPlayer mediaPlayer;

    private final int videoWidth = 1920;
    private final int videoHeight = 1090;

    private final int[] buffer = new int[videoWidth * videoHeight];
    private final int[] pboIds = new int[2];
    private final int[] stagingBuffer = new int[videoWidth * videoHeight];
    private int activePboIndex = 0;
    private boolean frameAvailable = false;

    private boolean hasPlayed = false;

    private final int textureIdVideo;

    public VideoLanPlayback() {
        mediaPlayerFactory = new MediaPlayerFactory(
                "--no-video-title-show",
                ":avcodec-hw=dxva2",
                ":avcodec-codec=h264",
                "--directx-hw-yuv",
                "--codec=avcodec",
                "--vout=none",
                "--verbose=3"
        );
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        GL15.glGenBuffers(pboIds);
        for (int pboId : pboIds) {
            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboId);
            GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER, videoWidth * videoHeight * 4L,
                    GL15.GL_STREAM_DRAW);
        }
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);

        mediaPlayer.videoSurface().set(new CallbackVideoSurface(
                new CubeBufferFormatCallback(),
                new CubeRenderFormatCallback(buffer),
                true
        ));

        textureIdVideo = emptyTexture(videoWidth, videoHeight);
    }

    public void play(String media, String... options) {
        mediaPlayer.media().play(media, options);
        hasPlayed = true;
    }

    public void sync() {
        if (!hasPlayed || !frameAvailable) return;

        synchronized (this) {
            // copy data from staging buffer to PBO
            int pboIndex = activePboIndex;
            activePboIndex = (activePboIndex + 1) % pboIds.length;

            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboIds[pboIndex]);
            ByteBuffer pboBuffer = GL15.glMapBuffer(GL21.GL_PIXEL_UNPACK_BUFFER,
                    GL15.GL_WRITE_ONLY, null);

            if (pboBuffer != null) {
                pboBuffer.asIntBuffer().put(stagingBuffer);
                GL15.glUnmapBuffer(GL21.GL_PIXEL_UNPACK_BUFFER);
            }

            // upload texture data
            resetTextureUploadState();
            glBindTexture(GL_TEXTURE_2D, textureIdVideo);
            GL11.glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight,
                    GL_RGBA, GL_UNSIGNED_BYTE, 0);

            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
            frameAvailable = false;
        }
    }

    public void close() {
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

    private static class CubeBufferFormatCallback extends BufferFormatCallbackAdapter {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            return new StandardBufferFormat(sourceWidth, sourceHeight);
        }
    }

    private class CubeRenderFormatCallback extends RenderCallbackAdapter {
        public CubeRenderFormatCallback(int[] videoBuffer) {
            setBuffer(videoBuffer);
        }

        @Override
        protected void onDisplay(MediaPlayer mediaPlayer, int[] buffer) {
            if (!mediaPlayer.status().isPlaying()) return;

            synchronized (VideoLanPlayback.this) {
                System.arraycopy(buffer, 0, stagingBuffer, 0, buffer.length);
                frameAvailable = true;
            }
        }
    }
}