package com.neocinema.bukkit.storage;

import com.neocinema.bukkit.theater.Theater;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface TheaterStorage {

    CompletableFuture<Void> saveTheater(Theater theater);

    CompletableFuture<Set<Theater>> loadTheaters();

}
