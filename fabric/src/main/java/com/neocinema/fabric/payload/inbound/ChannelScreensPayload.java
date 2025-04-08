package com.neocinema.fabric.payload.inbound;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.buffer.IdCodec;
import com.neocinema.fabric.screen.Screen;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public record ChannelScreensPayload(List<Screen> screens) implements CustomPayload {
    public static final IdCodec<ChannelScreensPayload> CHANNEL_SCREENS = new IdCodec<>(
            new Id<>(Identifier.of(NeoCinema.MODID, "screens")),
            PacketCodec.ofStatic(
                    (b, p) -> {
                        throw new NotImplementedException();
                    },
                    b -> {
                        List<Screen> screens = new ArrayList<>();
                        int length = b.readInt();
                        for (int i = 0; i < length; i++) screens.add(new Screen().fromBytes(b));
                        b.clear();
                        return new ChannelScreensPayload(screens);
                    }
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return CHANNEL_SCREENS.id();
    }
}