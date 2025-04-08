package com.neocinema.fabric.payload.inbound;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.buffer.IdCodec;
import com.neocinema.fabric.screen.PreviewScreen;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.NotImplementedException;

public record UpdatePreviewScreenPayload(PreviewScreen screen) implements CustomPayload {
    public static final IdCodec<UpdatePreviewScreenPayload> CHANNEL_UPDATE_PREVIEW_SCREEN = new IdCodec<>(
            new Id<>(Identifier.of(NeoCinema.MODID, "update_preview_screen")),
            PacketCodec.ofStatic(
                    (b, p) -> {
                        throw new NotImplementedException();
                    },
                    b -> {
                        PreviewScreen previewScreen = new PreviewScreen().fromBytes(b);
                        b.clear();
                        return new UpdatePreviewScreenPayload(previewScreen);
                    }
            )
    );
    @Override
    public Id<? extends CustomPayload> getId() {
        return CHANNEL_UPDATE_PREVIEW_SCREEN.id();
    }
}