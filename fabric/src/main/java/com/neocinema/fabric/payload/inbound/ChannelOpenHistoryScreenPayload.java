package com.neocinema.fabric.payload.inbound;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.buffer.IdCodec;
import com.neocinema.fabric.gui.VideoHistoryScreen;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChannelOpenHistoryScreenPayload(VideoHistoryScreen screen) implements CustomPayload {
    public static final IdCodec<ChannelOpenHistoryScreenPayload> CHANNEL_OPEN_HISTORY_SCREEN = new IdCodec<>(
            new Id<>(Identifier.of(NeoCinema.MODID, "open_history_screen")),
            PacketCodec.ofStatic(
                    (b, p) -> {},
                    b -> new ChannelOpenHistoryScreenPayload(new VideoHistoryScreen())
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return CHANNEL_OPEN_HISTORY_SCREEN.id();
    }
}