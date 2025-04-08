package com.neocinema.fabric.payload.outbound;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.buffer.IdCodec;
import com.neocinema.fabric.video.VideoInfo;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChannelVideoQueueVotePayload(VideoInfo videoInfo, int voteType) implements CustomPayload {
    public static final IdCodec<ChannelVideoQueueVotePayload> CHANNEL_VIDEO_QUEUE_VOTE = new IdCodec<>(
            new Id<>(Identifier.of(NeoCinema.MODID, "video_queue_vote")),
            PacketCodec.ofStatic(
                    (b, p) -> {
                        p.videoInfo.toBytes(b);
                        b.writeInt(p.voteType);
                    },
                    b -> new ChannelVideoQueueVotePayload(new VideoInfo().fromBytes(b), b.readInt())
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return CHANNEL_VIDEO_QUEUE_VOTE.id();
    }
}