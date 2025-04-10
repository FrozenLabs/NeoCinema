package com.neocinema.fabric.util;

import com.neocinema.fabric.NeoCinemaClient;
import com.neocinema.fabric.buffer.IdCodec;
import com.neocinema.fabric.gui.VideoQueueScreen;
import com.neocinema.fabric.payload.inbound.*;
import com.neocinema.fabric.payload.outbound.*;
import com.neocinema.fabric.screen.PreviewScreen;
import com.neocinema.fabric.screen.PreviewScreenManager;
import com.neocinema.fabric.screen.Screen;
import com.neocinema.fabric.service.VideoService;
import com.neocinema.fabric.video.Video;
import com.neocinema.fabric.video.VideoInfo;
import com.neocinema.fabric.video.list.VideoList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public final class NetworkUtil {

    private static final NeoCinemaClient CD = NeoCinemaClient.getInstance();

    public static void registerReceivers() {
        registerInbound(ChannelServicesPayload.CHANNEL_SERVICES, ((payload, context) -> {
            for (VideoService element : payload.service()) {
                CD.getVideoServiceManager().register(element);
            }
        }));
        registerInbound(ChannelScreensPayload.CHANNEL_SCREENS, (payload, context) -> {
            for (Screen element : payload.screens()) {
                CD.getScreenManager().registerScreen(element);
            }
        });
        registerInbound(LoadScreenPayload.LOAD_SCREEN, (payload, context) -> {
            Screen screen = CD.getScreenManager().getScreen(new BlockPos(payload.getX(), payload.getY(), payload.getZ()));
            if (screen == null) return;
            Video video = payload.getVideo();
            System.out.println(video.getVideoInfo().getTitle());
            context.client().submit(() -> screen.loadVideo(video));
        });
        registerInbound(UnloadScreenPayload.UNLOAD_SCREEN, ((payload, context) -> {
            Screen screen = CD.getScreenManager().getScreen(new BlockPos(payload.getX(), payload.getY(), payload.getZ()));
            if (screen == null) return;
            context.client().submit(screen::closeBrowser);
        }));
        registerInbound(UpdatePreviewScreenPayload.CHANNEL_UPDATE_PREVIEW_SCREEN, ((payload, context) -> {
            PreviewScreen previewScreen = payload.screen();
            PreviewScreenManager manager = CD.getPreviewScreenManager();
            VideoInfo videoInfo = previewScreen.getVideoInfo();
            if (manager.getPreviewScreen(previewScreen.getBlockPos()) == null)
                manager.addPreviewScreen(previewScreen);
            else
                manager.getPreviewScreen(previewScreen.getBlockPos()).setVideoInfo(videoInfo);
        }));
        registerInbound(ChannelOpenSettingsScreenPayload.CHANNEL_OPEN_SETTINGS_SCREEN, ((payload, context) -> {
            context.client().submit(() -> context.client().setScreen(payload.screen()));
        }));
        registerInbound(ChannelOpenHistoryScreenPayload.CHANNEL_OPEN_HISTORY_SCREEN, ((payload, context) -> {
            context.client().submit(() -> context.client().setScreen(payload.screen()));
        }));
        registerInbound(ChannelVideoListHistorySplit.CHANNEL_VIDEO_LIST_HISTORY_SPLIT, ((payload, context) -> {
            CD.getVideoListManager().getHistory().merge(new VideoList(payload.entries()));
        }));
        registerInbound(ChannelVideoQueueStatePayload.CHANNEL_VIDEO_QUEUE_STATE, ((payload, context) -> {
            CD.getVideoQueue().setVideos(payload.queue());
            context.client().submit(() -> {
                if (context.client().currentScreen instanceof VideoQueueScreen) {
                    VideoQueueScreen videoQueueScreen = (VideoQueueScreen) context.client().currentScreen;
                    videoQueueScreen.videoQueueWidget.update();
                }
            });
        }));

        registerOutbound(ChannelVideoRequestPayload.CHANNEL_VIDEO_REQUEST);
        registerOutbound(ChannelVideoHistoryRemovePayload.CHANNEL_VIDEO_HISTORY_REMOVE);
        registerOutbound(ChannelVideoQueueVotePayload.CHANNEL_VIDEO_QUEUE_VOTE);
        registerOutbound(ChannelVideoQueueRemovePayload.CHANNEL_VIDEO_QUEUE_REMOVE);
        registerOutbound(ChannelShowVideoTimelinePayload.CHANNEL_SHOW_VIDEO_TIMELINE);
//        ClientPlayNetworking.registerGlobalReceiver(CHANNEL_VIDEO_LIST_PLAYLIST_SPLIT, (client, handler, buf, responseSender) -> {
//
//        });
    }

    private static <T extends CustomPayload> void registerOutbound(IdCodec<T> codec) {
        PayloadTypeRegistry.playC2S().register(codec.id(), codec.codec());
    }
    private static <T extends CustomPayload> void registerInbound(IdCodec<T> codec, ClientPlayNetworking.PlayPayloadHandler<T> handler) {
        PayloadTypeRegistry.playS2C().register(codec.id(), codec.codec());
        ClientPlayNetworking.registerGlobalReceiver(codec.id(), handler);
    }

    public static void sendVideoRequestPacket(VideoInfo videoInfo) {
        ClientPlayNetworking.send(new ChannelVideoRequestPayload(videoInfo));
    }

    public static void sendDeleteHistoryPacket(VideoInfo videoInfo) {
        ClientPlayNetworking.send(new ChannelVideoHistoryRemovePayload(videoInfo));
    }

    public static void sendVideoQueueVotePacket(VideoInfo videoInfo, int voteType) {
        ClientPlayNetworking.send(new ChannelVideoQueueVotePayload(videoInfo, voteType));
    }

    public static void sendVideoQueueRemovePacket(VideoInfo videoInfo) {
        ClientPlayNetworking.send(new ChannelVideoQueueRemovePayload(videoInfo));
    }

    public static void sendShowVideoTimelinePacket() {
        ClientPlayNetworking.send(new ChannelShowVideoTimelinePayload());
    }

}
