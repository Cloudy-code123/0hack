package com.hack.zero.event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventManager {
    private final Map<Class<? extends Event>, List<Consumer<? extends Event>>> listeners = new ConcurrentHashMap<>();

    public <T extends Event> void register(Class<T> eventClass, Consumer<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> Collections.synchronizedList(new ArrayList<>())).add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void fire(T event) {
        List<Consumer<? extends Event>> list = listeners.get(event.getClass());
        if (list != null) {
            for (Consumer<? extends Event> consumer : list) {
                ((Consumer<T>) consumer).accept(event);
            }
        }
    }
}
