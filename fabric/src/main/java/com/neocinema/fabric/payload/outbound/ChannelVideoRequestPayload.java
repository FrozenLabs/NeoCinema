package com.neocinema.fabric.payload.outbound;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.buffer.IdCodec;
import com.neocinema.fabric.video.VideoInfo;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChannelVideoRequestPayload(VideoInfo info) implements CustomPayload {
    public static final IdCodec<ChannelVideoRequestPayload> CHANNEL_VIDEO_REQUEST = new IdCodec<>(
            new Id<>(Identifier.of(NeoCinema.MODID, "video_request")),
            PacketCodec.ofStatic(
                    (b, p) -> p.info.toBytes(b),
                    b -> new ChannelVideoRequestPayload(new VideoInfo().fromBytes(b))
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return CHANNEL_VIDEO_REQUEST.id();
    }
}