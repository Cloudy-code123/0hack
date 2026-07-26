package com.hack.zero.event.events;

import com.hack.zero.event.Event;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class RenderEvent extends Event {
    private final MatrixStack matrices;
    private final VertexConsumerProvider.Immediate vertexConsumers;
    private final float tickDelta;

    public RenderEvent(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, float tickDelta) {
        this.matrices = matrices;
        this.vertexConsumers = vertexConsumers;
        this.tickDelta = tickDelta;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }

    public VertexConsumerProvider.Immediate getVertexConsumers() {
        return vertexConsumers;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
