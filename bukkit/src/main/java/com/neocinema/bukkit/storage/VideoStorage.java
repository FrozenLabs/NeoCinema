package com.neocinema.bukkit.storage;

import com.neocinema.bukkit.service.VideoServiceType;
import com.neocinema.bukkit.video.VideoInfo;
import com.neocinema.bukkit.video.VideoRequest;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface VideoStorage {

    CompletableFuture<Void> saveVideoInfo(VideoInfo videoInfo);

    CompletableFuture<VideoInfo> searchVideoInfo(VideoServiceType videoService, String id);

    CompletableFuture<Void> saveVideoRequest(VideoRequest request);

    CompletableFuture<Set<VideoRequest>> loadVideoRequests(UUID requester);

}
