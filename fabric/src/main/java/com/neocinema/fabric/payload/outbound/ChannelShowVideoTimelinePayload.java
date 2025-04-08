package com.neocinema.fabric.payload.outbound;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.buffer.IdCodec;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class ChannelShowVideoTimelinePayload implements CustomPayload {
    public static final IdCodec<ChannelShowVideoTimelinePayload> CHANNEL_SHOW_VIDEO_TIMELINE = new IdCodec<>(
            new Id<>(Identifier.of(NeoCinema.MODID, "show_video_timeline")),
            PacketCodec.ofStatic(
                    (b, p) -> {},
                    b -> new ChannelShowVideoTimelinePayload()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return CHANNEL_SHOW_VIDEO_TIMELINE.id();
    }
}