package com.neocinema.fabric.payload.inbound;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.buffer.IdCodec;
import com.neocinema.fabric.video.Video;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.NotImplementedException;

public class LoadScreenPayload implements CustomPayload {
    public static final IdCodec<LoadScreenPayload> LOAD_SCREEN = new IdCodec<>(
            new Id<>(Identifier.of(NeoCinema.MODID, "load_screen")),
            PacketCodec.ofStatic(
                    (b, p) -> {
                        throw new NotImplementedException();
                    },
                    b -> {
                        var payload = new LoadScreenPayload().fromBytes(b);
                        b.clear();
                        return payload;
                    }
            )
    );

    private int x;
    private int y;
    private int z;

    private Video video;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Video getVideo() {
        return video;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return LOAD_SCREEN.id();
    }

    public LoadScreenPayload fromBytes(PacketByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        video = new Video().fromBytes(buf);
        return this;
    }
}