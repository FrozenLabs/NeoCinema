package com.neocinema.fabric.video.queue;

import com.neocinema.fabric.video.VideoInfo;
import org.jetbrains.annotations.NotNull;

/**
 * @param clientState -1 = down vote, 0 = no vote, 1 = upvote
 */
public record QueuedVideo(VideoInfo videoInfo, int score, int clientState,
                          boolean owner) implements Comparable<QueuedVideo> {

    public String getScoreString() {
        if (score > 0) {
            return "+" + score;
        } else {
            return String.valueOf(score);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueuedVideo)) {
            return false;
        }
        return videoInfo.equals(((QueuedVideo) o).videoInfo);
    }

    @Override
    public int compareTo(@NotNull QueuedVideo queuedVideo) {
        return Integer.compare(queuedVideo.score, score);
    }
}
