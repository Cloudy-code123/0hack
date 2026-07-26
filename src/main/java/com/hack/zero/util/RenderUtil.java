package com.hack.zero.util;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.OptionalDouble;

public class RenderUtil {

    // Modern 1.21.7 pipeline – NO depth test → sees through walls
    private static final RenderPipeline ESP_LINES_PIPELINE = RenderPipeline.builder(
            RenderPipelines.RENDERTYPE_LINES_SNIPPET
    )
    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
    .withDepthWrite(false)
    .withLocation("zerohack/esp_lines")
    .build();

    public static final RenderLayer ESP_LINES = RenderLayer.of(
            "zerohack_esp_lines",
            1536,
            false,
            true,
            ESP_LINES_PIPELINE,
            RenderLayer.MultiPhaseParameters.builder()
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(2.5)))
                    .build(false)
    );

    public static RenderLayer getEspLines() {
        return ESP_LINES;
    }

    public static void setupRenderState() {
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GlStateManager._enableBlend();
        GlStateManager._disableCull();
    }

    public static void restoreRenderState() {
        GlStateManager._enableDepthTest();
        GlStateManager._depthMask(true);
        GlStateManager._enableCull();
        GlStateManager._disableBlend();
    }

    public static void drawBoxLines(VertexConsumer buffer, Matrix4f matrix, Box box,
                                     float r, float g, float b, float a) {
        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;

        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(1, 0, 0).light(15728880);
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).normal(1, 0, 0).light(15728880);
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).normal(0, 0, 1).light(15728880);
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).normal(0, 0, 1).light(15728880);
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).normal(-1, 0, 0).light(15728880);
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).normal(-1, 0, 0).light(15728880);
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).normal(0, 0, -1).light(15728880);
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(0, 0, -1).light(15728880);

        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).normal(1, 0, 0).light(15728880);
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).normal(1, 0, 0).light(15728880);
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).normal(0, 0, 1).light(15728880);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(0, 0, 1).light(15728880);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(-1, 0, 0).light(15728880);
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).normal(-1, 0, 0).light(15728880);
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).normal(0, 0, -1).light(15728880);
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).normal(0, 0, -1).light(15728880);

        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(0, 1, 0).light(15728880);
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).normal(0, 1, 0).light(15728880);
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).normal(0, 1, 0).light(15728880);
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).normal(0, 1, 0).light(15728880);
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).normal(0, 1, 0).light(15728880);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(0, 1, 0).light(15728880);
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).normal(0, 1, 0).light(15728880);
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).normal(0, 1, 0).light(15728880);
    }

    public static void drawLine(MatrixStack matrices, VertexConsumerProvider consumers,
                                 Vec3d from, Vec3d to, int color) {
        float a = (float) (color >> 24 & 0xFF) / 255.0f;
        float r = (float) (color >> 16 & 0xFF) / 255.0f;
        float g = (float) (color >> 8 & 0xFF) / 255.0f;
        float b = (float) (color & 0xFF) / 255.0f;
        if (a == 0.0f) a = 1.0f;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());
        buffer.vertex(matrix, (float) from.x, (float) from.y, (float) from.z)
              .color(r, g, b, a).normal(0, 1, 0).light(15728880);
        buffer.vertex(matrix, (float) to.x, (float) to.y, (float) to.z)
              .color(r, g, b, a).normal(0, 1, 0).light(15728880);
    }
}
