package com.neocinema.bukkit.util;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.buffer.PacketByteBufReimpl;
import com.neocinema.bukkit.player.PlayerData;
import com.neocinema.bukkit.service.VideoServiceType;
import com.neocinema.bukkit.theater.StaticTheater;
import com.neocinema.bukkit.theater.Theater;
import com.neocinema.bukkit.theater.screen.PreviewScreen;
import com.neocinema.bukkit.theater.screen.Screen;
import com.neocinema.bukkit.video.Video;
import com.neocinema.bukkit.video.VideoInfo;
import com.neocinema.bukkit.video.VideoRequest;
import com.neocinema.bukkit.video.queue.QueueVoteType;
import com.neocinema.bukkit.video.queue.VideoQueue;
import io.netty.buffer.Unpooled;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class NetworkUtil {

    /* OUTGOING */
    private static final String CHANNEL_SERVICES = "neocinema:services";
    private static final String CHANNEL_SCREENS = "neocinema:screens";
    private static final String CHANNEL_LOAD_SCREEN = "neocinema:load_screen";
    private static final String CHANNEL_UNLOAD_SCREEN = "neocinema:unload_screen";
    private static final String CHANNEL_UPDATE_PREVIEW_SCREEN = "neocinema:update_preview_screen";
    private static final String CHANNEL_OPEN_SETTINGS_SCREEN = "neocinema:open_settings_screen";
    private static final String CHANNEL_OPEN_HISTORY_SCREEN = "neocinema:open_history_screen";
    private static final String CHANNEL_VIDEO_LIST_HISTORY_SPLIT = "neocinema:video_list_history_split";
    private static final String CHANNEL_VIDEO_QUEUE_STATE = "neocinema:video_queue_state";
    /* INCOMING */
    private static final String CHANNEL_VIDEO_REQUEST = "neocinema:video_request";
    private static final String CHANNEL_VIDEO_HISTORY_REMOVE = "neocinema:video_history_remove";
    private static final String CHANNEL_VIDEO_QUEUE_VOTE = "neocinema:video_queue_vote";
    private static final String CHANNEL_VIDEO_QUEUE_REMOVE = "neocinema:video_queue_remove";
    private static final String CHANNEL_SHOW_VIDEO_TIMELINE = "neocinema:show_video_timeline";

    public static void registerChannels(NeoCinemaPlugin neoCinemaPlugin) {
        Messenger m = neoCinemaPlugin.getServer().getMessenger();
        /* OUTGOING */
        m.registerOutgoingPluginChannel(neoCinemaPlugin, CHANNEL_SERVICES);
        m.registerOutgoingPluginChannel(neoCinemaPlugin, CHANNEL_SCREENS);
        m.registerOutgoingPluginChannel(neoCinemaPlugin, CHANNEL_LOAD_SCREEN);
        m.registerOutgoingPluginChannel(neoCinemaPlugin, CHANNEL_UNLOAD_SCREEN);
        m.registerOutgoingPluginChannel(neoCinemaPlugin, CHANNEL_UPDATE_PREVIEW_SCREEN);
        m.registerOutgoingPluginChannel(neoCinemaPlugin, CHANNEL_OPEN_SETTINGS_SCREEN);
        m.registerOutgoingPluginChannel(neoCinemaPlugin, CHANNEL_OPEN_HISTORY_SCREEN);
        m.registerOutgoingPluginChannel(neoCinemaPlugin, CHANNEL_VIDEO_LIST_HISTORY_SPLIT);
        m.registerOutgoingPluginChannel(neoCinemaPlugin, CHANNEL_VIDEO_QUEUE_STATE);
        /* INCOMING */
        m.registerIncomingPluginChannel(neoCinemaPlugin, CHANNEL_VIDEO_REQUEST, (s, player, bytes) -> {
            PacketByteBufReimpl buf = new PacketByteBufReimpl(Unpooled.wrappedBuffer(bytes));
            VideoInfo videoInfo = new VideoInfo().fromBytes(buf);
            neoCinemaPlugin.getVideoStorage().searchVideoInfo(videoInfo.getServiceType(), videoInfo.getId()).thenAccept(search -> {
                if (!player.isOnline() || search == null) return;
                Theater theater = neoCinemaPlugin.getTheaterManager().getCurrentTheater(player);
                if (theater == null || theater instanceof StaticTheater) return;
                theater.getVideoQueue().processPlayerRequest(search, player);
            });
        });
        m.registerIncomingPluginChannel(neoCinemaPlugin, CHANNEL_VIDEO_HISTORY_REMOVE, (s, player, bytes) -> {
            PacketByteBufReimpl buf = new PacketByteBufReimpl(Unpooled.wrappedBuffer(bytes));
            VideoInfo videoInfo = new VideoInfo().fromBytes(buf);
            PlayerData playerData = neoCinemaPlugin.getPlayerDataManager().getData(player.getUniqueId());
            playerData.deleteHistory(videoInfo, neoCinemaPlugin);
        });
        m.registerIncomingPluginChannel(neoCinemaPlugin, CHANNEL_VIDEO_QUEUE_VOTE, (s, player, bytes) -> {
            PacketByteBufReimpl buf = new PacketByteBufReimpl(Unpooled.wrappedBuffer(bytes));
            VideoInfo videoInfo = new VideoInfo().fromBytes(buf);
            int voteType = buf.readInt();
            Theater theater = neoCinemaPlugin.getTheaterManager().getCurrentTheater(player);
            if (theater != null) {
                Video video = theater.getVideoQueue().getVideo(videoInfo);
                if (video != null) {
                    QueueVoteType currentVote = video.getCurrentVote(player);

                    if (currentVote == null) {
                        if (voteType == -1) {
                            theater.getVideoQueue().downvoteVideo(player, video);
                        } else if (voteType == 1) {
                            theater.getVideoQueue().upvoteVideo(player, video);
                        }
                    } else if (currentVote.getValue() == voteType) {
                        theater.getVideoQueue().removeVote(player, video);
                    } else {
                        theater.getVideoQueue().removeVote(player, video);

                        if (voteType == -1) {
                            theater.getVideoQueue().downvoteVideo(player, video);
                        } else if (voteType == 1) {
                            theater.getVideoQueue().upvoteVideo(player, video);
                        }
                    }
                }
            }
        });
        m.registerIncomingPluginChannel(neoCinemaPlugin, CHANNEL_VIDEO_QUEUE_REMOVE, (s, player, bytes) -> {
            PacketByteBufReimpl buf = new PacketByteBufReimpl(Unpooled.wrappedBuffer(bytes));
            VideoInfo videoInfo = new VideoInfo().fromBytes(buf);
            Theater theater = neoCinemaPlugin.getTheaterManager().getCurrentTheater(player);
            if (theater == null || theater instanceof StaticTheater) return;
            Video queuedVideo = theater.getVideoQueue().getVideo(videoInfo);
            if (queuedVideo == null || !queuedVideo.getRequester().equals(player) || !player.hasPermission("neocinema.admin"))
                return;
            theater.getVideoQueue().unqueueVideo(queuedVideo);
        });
        m.registerIncomingPluginChannel(neoCinemaPlugin, CHANNEL_SHOW_VIDEO_TIMELINE, (s, player, bytes) -> {
            Theater theater = neoCinemaPlugin.getTheaterManager().getCurrentTheater(player);
            if (theater == null || theater instanceof StaticTheater) return;
            theater.showBossBars(neoCinemaPlugin, player);
        });
    }

    public static void sendRegisterServicesPacket(JavaPlugin plugin, Player player) {
        PacketByteBufReimpl buf = new PacketByteBufReimpl(Unpooled.buffer());
        buf.writeInt(VideoServiceType.values().length);
        for (VideoServiceType type : VideoServiceType.values())
            type.toBytes(buf);
        player.sendPluginMessage(plugin, CHANNEL_SERVICES, buf.array());
    }

    public static void sendScreensPacket(JavaPlugin plugin, Player player, List<Screen> screens) {
        PacketByteBufReimpl buf = new PacketByteBufReimpl(Unpooled.buffer());
        buf.writeInt(screens.size());
        for (Screen screen : screens)
            screen.toBytes(buf);
        player.sendPluginMessage(plugin, CHANNEL_SCREENS, buf.array());
    }

    public static void sendLoadScreenPacket(JavaPlugin plugin, Player player, Screen screen, Video video) {
        PacketByteBufReimpl buf = new PacketByteBufReimpl(Unpooled.buffer());
        buf.writeInt(screen.getX());
        buf.writeInt(screen.getY());
        buf.writeInt(screen.getZ());
        video.toBytes(buf);
        player.sendPluginMessage(plugin, CHANNEL_LOAD_SCREEN, buf.array());
    }

    public static void sendUnloadScreenPacket(JavaPlugin plugin, Player player, Screen screen) {
        PacketByteBufReimpl buf = new PacketByteBufReimpl(Unpooled.buffer());
        buf.writeInt(screen.getX());
        buf.writeInt(screen.getY());
        buf.writeInt(screen.getZ());
        player.sendPluginMessage(plugin, CHANNEL_UNLOAD_SCREEN, buf.array());
    }

    public static void sendUpdatePreviewScreenPacket(JavaPlugin plugin, Player player, PreviewScreen previewScreen, @Nullable VideoInfo videoInfo) {
        PacketByteBufReimpl buf = new PacketByteBufReimpl(Unpooled.buffer());
        previewScreen.toBytes(buf);
        buf.writeBoolean(videoInfo != null);
        if (videoInfo != null) videoInfo.toBytes(buf);
        player.sendPluginMessage(plugin, CHANNEL_UPDATE_PREVIEW_SCREEN, buf.array());
    }

    public static void sendOpenSettingsScreenPacket(JavaPlugin plugin, Player player) {
        player.sendPluginMessage(plugin, CHANNEL_OPEN_SETTINGS_SCREEN, new byte[0]);
    }

    public static void sendOpenHistoryScreenPacket(JavaPlugin plugin, Player player) {
        player.sendPluginMessage(plugin, CHANNEL_OPEN_HISTORY_SCREEN, new byte[0]);
    }

    public static void sendVideoListHistorySplitPacket(JavaPlugin plugin, Player player, List<VideoRequest> history) {
        for (List<VideoRequest> splitList : splitVideoRequests(history, 50)) {
            PacketByteBufReimpl buf = new PacketByteBufReimpl(Unpooled.buffer());
            buf.writeInt(splitList.size());
            for (VideoRequest request : splitList)
                request.toBytes(buf);
            player.sendPluginMessage(plugin, CHANNEL_VIDEO_LIST_HISTORY_SPLIT, buf.array());
        }
    }

    public static void sendVideoQueueStatePacket(JavaPlugin plugin, Player player, VideoQueue queue) {
        PacketByteBufReimpl buf = new PacketByteBufReimpl(Unpooled.buffer());
        queue.toBytes(buf, player);
        player.sendPluginMessage(plugin, CHANNEL_VIDEO_QUEUE_STATE, buf.array());
    }

    public static List<List<VideoRequest>> splitVideoRequests(List<VideoRequest> entries, int listSize) {
        List<List<VideoRequest>> splitEntries = new ArrayList<>();
        for (int i = 0; i < entries.size(); i = i + listSize) {
            if (i + listSize < entries.size())
                splitEntries.add(entries.subList(i, i + listSize));
            else
                splitEntries.add(entries.subList(i, entries.size()));
        }
        return splitEntries;
    }

}
