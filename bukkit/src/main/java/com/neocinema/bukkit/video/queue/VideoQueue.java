package com.neocinema.bukkit.video.queue;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.buffer.PacketByteBufReimpl;
import com.neocinema.bukkit.event.queue.*;
import com.neocinema.bukkit.video.VideoInfo;
import com.neocinema.bukkit.theater.PrivateTheater;
import com.neocinema.bukkit.theater.Theater;
import com.neocinema.bukkit.util.NetworkUtil;
import com.neocinema.bukkit.video.Video;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.PriorityQueue;

public class VideoQueue {

    private static final int MAX_QUEUE_SIZE = 32;

    private final NeoCinemaPlugin neoCinemaPlugin;
    private final Theater theater;
    private final PriorityQueue<Video> priorityQueue;
    private boolean locked;

    public VideoQueue(NeoCinemaPlugin neoCinemaPlugin, Theater theater) {
        this.neoCinemaPlugin = neoCinemaPlugin;
        this.theater = theater;
        priorityQueue = new PriorityQueue<>();
    }

    public void upvoteVideo(Player voter, Video video) {
        addVote(voter, video, QueueVoteType.UP_VOTE);
    }

    public void downvoteVideo(Player voter, Video video) {
        addVote(voter, video, QueueVoteType.DOWN_VOTE);
    }

    private void addVote(Player voter, Video video, QueueVoteType voteType) {
        priorityQueue.remove(video);
        boolean changed = video.addVote(voter, voteType);
        priorityQueue.add(video);
        if (changed) {
            callQueueChangeEvent(new TheaterQueueVoteAddEvent(theater, voter, voteType));
        }
    }

    public void removeVote(Player voter, Video video) {
        priorityQueue.remove(video);
        boolean changed = video.removeVote(voter);
        priorityQueue.add(video);
        if (changed) {
            callQueueChangeEvent(new TheaterQueueVoteRemoveEvent(theater, voter));
        }
    }

    public void processPlayerRequest(VideoInfo videoInfo, Player player) {
        neoCinemaPlugin.getVideoStorage().saveVideoInfo(videoInfo).thenRun(() -> {
            if (!player.isOnline()) return;

            Video video = new Video(videoInfo, player);
            VideoQueueResult queueResult = queueVideo(video);

            switch (queueResult) {
                case QUEUE_FULL:
                    player.sendMessage(ChatColor.RED + "This theater's queue is full.");
                    break;
                case ALREADY_IN_QUEUE:
                    player.sendMessage(ChatColor.RED + "This video is already in queue.");
                    break;
                case QUEUE_LOCKED:
                    player.sendMessage(ChatColor.RED + "The owner of this theater has locked the queue.");
                    break;
                case SUCCESSFUL:
                    player.sendMessage(ChatColor.GOLD + "Queued video: " + video.getVideoInfo().getTitle());
                    neoCinemaPlugin.getPlayerDataManager().getData(player.getUniqueId()).addHistory(videoInfo, neoCinemaPlugin);
                    break;
            }
        });
    }

    public VideoQueueResult queueVideo(Video video) {
        if (locked) {
            boolean hasPermission = false;

            if (theater instanceof PrivateTheater) {
                if (video.getRequester().equals(((PrivateTheater) theater).getOwner())) {
                    hasPermission = true;
                }
            }

            if (!hasPermission && video.getRequester().hasPermission("neocinema.admin")) {
                hasPermission = true;
            }

            if (!hasPermission) {
                return VideoQueueResult.QUEUE_LOCKED;
            }
        }

        for (Video inQueue : priorityQueue) {
            if (inQueue.equals(video)) {
                return VideoQueueResult.ALREADY_IN_QUEUE;
            }
        }

        if (priorityQueue.size() >= MAX_QUEUE_SIZE) {
            return VideoQueueResult.QUEUE_FULL;
        }

        priorityQueue.add(video);

        callQueueChangeEvent(new TheaterQueueVideoAddEvent(theater, video));

        return VideoQueueResult.SUCCESSFUL;
    }

    public void unqueueVideo(Video video) {
        boolean changed = priorityQueue.remove(video);

        if (changed) {
            callQueueChangeEvent(new TheaterQueueVideoRemoveEvent(theater, video));
        }
    }

    public boolean hasNext() {
        return !priorityQueue.isEmpty();
    }

    public Video getVideo(VideoInfo videoInfo) {
        for (Video video : priorityQueue) {
            if (video.getVideoInfo().equals(videoInfo)) {
                return video;
            }
        }
        return null;
    }

    public Video poll() {
        Video video = priorityQueue.poll();
        callQueueChangeEvent(new TheaterQueueVideoRemoveEvent(theater, video));
        return video;
    }

    public void clear() {
        priorityQueue.clear();
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    protected void callQueueChangeEvent(TheaterQueueChangeEvent event) {
        neoCinemaPlugin.getServer().getScheduler().runTask(neoCinemaPlugin, () -> {
            theater.getViewers().forEach(viewer -> NetworkUtil.sendVideoQueueStatePacket(neoCinemaPlugin, viewer, this));
            neoCinemaPlugin.getServer().getPluginManager().callEvent(event);
        });
    }

    public void toBytes(PacketByteBufReimpl buf, Player origin) {
        buf.writeInt(priorityQueue.size());
        for (Video video : priorityQueue) {
            video.getVideoInfo().toBytes(buf);
            buf.writeInt(video.getVoteScore());
            final int clientState;
            QueueVoteType currentVote = video.getCurrentVote(origin);
            if (QueueVoteType.UP_VOTE == currentVote) {
                clientState = 1;
            } else if (QueueVoteType.DOWN_VOTE == currentVote) {
                clientState = -1;
            } else {
                clientState = 0;
            }
            buf.writeInt(clientState);
            buf.writeBoolean(origin.getUniqueId().equals(video.getRequester().getUniqueId())); // is owner flag
        }
    }

}
