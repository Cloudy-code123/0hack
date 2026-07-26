package com.hack.zero.event.events;

import com.hack.zero.event.Event;

public class PacketEvent extends Event {
    private final Object packet;
    private final Direction direction;

    public enum Direction { SEND, RECEIVE }

    public PacketEvent(Object packet, Direction direction) {
        this.packet = packet;
        this.direction = direction;
    }

    public Object getPacket() {
        return packet;
    }

    public Direction getDirection() {
        return direction;
    }
}
