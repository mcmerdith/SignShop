package org.wargamer2010.signshop.configuration.storage.database.util;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.SignShopLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class ShopValidator implements Listener {
    private static ShopValidator _instance;

    public static ShopValidator instance() {
        if (_instance == null) _instance = new ShopValidator();

        return _instance;
    }

    private static SignShopLogger logger = SignShopLogger.getLogger("Shop Validator");

    private static class ChunkCoordinate {
        final int x;
        final int z;
        final String world;
        final Chunk c;

        ChunkCoordinate(int x, int z, String world, Chunk c) {
            this.x = x;
            this.z = z;
            this.world = world;
            this.c = c;
        }
    }

    private final Queue<ChunkCoordinate> chunkQueue = new ConcurrentLinkedQueue<>();

    private ShopValidator() {
//        Bukkit.getScheduler().runTaskTimerAsynchronously(
//                SignShop.getInstance(),
//                () -> {
//                    Map<String, List<ChunkCoordinate>> loaded = new HashMap<>();
//
//                    // Aggregate
//                    while (!chunkQueue.isEmpty()) {
//                        ChunkCoordinate cc = chunkQueue.poll();
//
//                        String chunkKey = cc.world + "/" + cc.x;
//
//                        List<ChunkCoordinate> chunks;
//
//                        if (loaded.containsKey(chunkKey)) {
//                            chunks = loaded.get(chunkKey);
//                        } else {
//                            chunks = new ArrayList<>();
//                        }
//
//                        chunks.add(cc);
//                        loaded.put(chunkKey, chunks);
//                    }
//
//                    loaded.forEach((k, lc) -> {
//                        String z = lc.stream().map(c -> c.z).sorted().map(String::valueOf).collect(Collectors.joining(","));
//
//                        logger.info(String.format("Loaded Chunks at %s: %s", k, z));
//                    });
//                },
//                20, 20
//        );
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent chunkEvent) {
        Chunk c = chunkEvent.getChunk();

        chunkQueue.add(new ChunkCoordinate(c.getX(), c.getZ(), c.getWorld().getName(), c));
    }
}
