package com.neocinema.fabric.payload.inbound;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.buffer.IdCodec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.NotImplementedException;

public class UnloadScreenPayload implements CustomPayload {
    public static final IdCodec<UnloadScreenPayload> UNLOAD_SCREEN = new IdCodec<>(
            new Id<>(Identifier.of(NeoCinema.MODID, "unload_screen")),
            PacketCodec.ofStatic(
                    (b, p) -> {
                        throw new NotImplementedException();
                    },
                    b -> {
                        var payload = new UnloadScreenPayload().fromBytes(b);
                        b.clear();
                        return payload;
                    }
            )
    );

    private int x;
    private int y;
    private int z;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return UNLOAD_SCREEN.id();
    }

    public UnloadScreenPayload fromBytes(PacketByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        return this;
    }
}