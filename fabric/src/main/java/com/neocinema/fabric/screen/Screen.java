package com.neocinema.fabric.screen;

import com.neocinema.fabric.block.screen.ScreenBlock;
import com.neocinema.fabric.screen.preview.PreviewScreen;
import com.neocinema.fabric.video.Video;
import com.neocinema.fabric.video.playback.VideoLanPlayback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import uk.co.caprica.vlcj.media.MediaSlaveType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Screen {

    private int x;
    private int y;
    private int z;
    private String facing;
    private float width;
    private float height;
    private boolean visible;
    private boolean muted;

    private final transient List<PreviewScreen> previewScreens;
    private transient VideoLanPlayback player;
    private transient Video video;
    private transient boolean unregistered;
    private transient BlockPos blockPos; // used as a cache for performance

    public Screen(int x, int y, int z, String facing, int width, int height, boolean visible, boolean muted) {
        this();
        this.x = x;
        this.y = y;
        this.z = z;
        this.facing = facing;
        this.width = width;
        this.height = height;
        this.visible = visible;
        this.muted = muted;
    }

    public Screen() {
        previewScreens = new ArrayList<>();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public BlockPos getPos() {
        if (blockPos == null) {
            blockPos = new BlockPos(x, y, z);
        }

        return blockPos;
    }

    public String getFacing() {
        return facing;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isMuted() {
        return muted;
    }

    public List<PreviewScreen> getPreviewScreens() {
        return previewScreens;
    }

    public void addPreviewScreen(PreviewScreen previewScreen) {
        previewScreens.add(previewScreen);
    }

    public VideoLanPlayback getPlayer() {
        return player;
    }

    public boolean hasPlayer() {
        return player != null;
    }

    public void reload() {
        if (video != null) {
            loadVideo(video);
        }
    }

    public void loadVideo(Video video) {
        this.video = video;
        closeBrowser();
        player = new VideoLanPlayback();
        startVideo();
    }

    public void closeBrowser() {
        if (player != null) {
            player.close();
            player = null;
        }
    }

    public Video getVideo() {
        return video;
    }

    public void setVideoVolume(float volume) {
        if (player != null && video != null) {
            player.mediaPlayer().audio().setVolume((int) (volume * 100));
        }
    }

    public void startVideo() {
        if (player == null || video == null) return;

        long resumeTimeMs = System.currentTimeMillis() - video.getStartedAt();
        String mediaUrl = video.getVideoInfo().getId();
        player.mediaPlayer().media().addSlave(MediaSlaveType.SUBTITLE, video.getVideoInfo().getId().replace("mp4", "srt"), true);
        player.play(mediaUrl);
    }


    public void seekVideo(int seconds) {
        player.mediaPlayer().controls().setTime(seconds * 1000L);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void register() {
        if (MinecraftClient.getInstance().world == null) {
            return;
        }

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        if (MinecraftClient.getInstance().world.isChunkLoaded(chunkX, chunkZ)) {
            MinecraftClient.getInstance().world.setBlockState(getBlockPos(), ScreenBlock.SCREEN_BLOCK.getDefaultState());
        }

        ClientChunkEvents.CHUNK_LOAD.register((clientWorld, worldChunk) -> {
            if (unregistered) {
                return;
            }

            // If the loaded chunk has this screen block in it, place it in the world
            if (worldChunk.getPos().x == chunkX && worldChunk.getPos().z == chunkZ) {
                clientWorld.setBlockState(getBlockPos(), ScreenBlock.SCREEN_BLOCK.getDefaultState());
            }
        });
    }

    public void unregister() {
        unregistered = true;

        if (MinecraftClient.getInstance().world != null) {
            MinecraftClient.getInstance().world.setBlockState(getBlockPos(), Blocks.AIR.getDefaultState());
        }
    }

    public Screen fromBytes(PacketByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        facing = buf.readString();
        width = buf.readFloat();
        height = buf.readFloat();
        visible = buf.readBoolean();
        muted = buf.readBoolean();
        return this;
    }

}
