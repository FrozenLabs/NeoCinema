package com.neocinema.fabric.payload.inbound;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.buffer.IdCodec;
import com.neocinema.fabric.gui.VideoSettingsScreen;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChannelOpenSettingsScreenPayload(VideoSettingsScreen screen) implements CustomPayload {
    public static final IdCodec<ChannelOpenSettingsScreenPayload> CHANNEL_OPEN_SETTINGS_SCREEN = new IdCodec<>(
            new CustomPayload.Id<>(Identifier.of(NeoCinema.MODID, "open_settings_screen")),
            PacketCodec.ofStatic(
                    (b, p) -> {},
                    b -> new ChannelOpenSettingsScreenPayload(new VideoSettingsScreen())
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return CHANNEL_OPEN_SETTINGS_SCREEN.id();
    }
}
