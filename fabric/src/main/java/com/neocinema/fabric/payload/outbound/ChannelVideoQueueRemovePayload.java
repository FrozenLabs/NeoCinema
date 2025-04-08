package com.neocinema.fabric.payload.outbound;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.buffer.IdCodec;
import com.neocinema.fabric.video.VideoInfo;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChannelVideoQueueRemovePayload(VideoInfo info) implements CustomPayload {
    public static final IdCodec<ChannelVideoQueueRemovePayload> CHANNEL_VIDEO_QUEUE_REMOVE = new IdCodec<>(
            new Id<>(Identifier.of(NeoCinema.MODID, "video_queue_remove")),
            PacketCodec.ofStatic(
                    (b, p) -> p.info.toBytes(b),
                    b -> new ChannelVideoQueueRemovePayload(new VideoInfo().fromBytes(b))
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return CHANNEL_VIDEO_QUEUE_REMOVE.id();
    }
}