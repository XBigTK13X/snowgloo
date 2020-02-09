package com.simplepathstudios.snowgloo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Taken from https://stackoverflow.com/questions/4742210/implementing-debounce-in-java

public class Debouncer {
    private static Debouncer __instance;
    public static Debouncer getInstance(){
        if(__instance == null){
            __instance = new Debouncer();
        }
        return __instance;
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<Object, Future<?>> delayedMap = new ConcurrentHashMap<>();

    private Debouncer(){

    }

    /**
     * Debounces {@code callable} by {@code delay}, i.e., schedules it to be executed after {@code delay},
     * or cancels its execution if the method is called with the same key within the {@code delay} again.
     */
    public void debounce(final Object key, final Runnable runnable, long delayMilliseconds) {
        final Future<?> prev = delayedMap.put(key, scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                    delayedMap.remove(key);
                }
            }
        }, delayMilliseconds, TimeUnit.MILLISECONDS));
        if (prev != null) {
            prev.cancel(true);
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}