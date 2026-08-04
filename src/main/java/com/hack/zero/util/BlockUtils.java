package com.hack.zero.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class BlockUtils {
    private static final ThreadLocal<BlockPos.Mutable> EXPOSED_POS = ThreadLocal.withInitial(BlockPos.Mutable::new);

    private BlockUtils() {
    }

    public static boolean isExposed(BlockPos blockPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return false;

        for (Direction direction : Direction.values()) {
            if (!mc.world.getBlockState(EXPOSED_POS.get().set(blockPos, direction)).isOpaque()) return true;
        }

        return false;
    }
}
