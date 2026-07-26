package com.hack.zero.module.combat;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.ModeSetting;
import com.hack.zero.module.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {

    private final NumberSetting range = new NumberSetting("Range", 4.5, 1.0, 6.0, 0.1);
    private final ModeSetting mode = new ModeSetting("Mode", "Single", "Single", "Switch", "Multi");
    private final BooleanSetting targetPlayers = new BooleanSetting("Players", true);
    private final BooleanSetting targetMobs = new BooleanSetting("Mobs", true);
    private final BooleanSetting targetAnimals = new BooleanSetting("Animals", true);
    private final BooleanSetting throughWalls = new BooleanSetting("ThroughWalls", false);
    private final BooleanSetting forceCriticals = new BooleanSetting("ForceCriticals", true);

    private int switchIndex = 0;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby targets", Category.COMBAT, 0);
        addSetting(range);
        addSetting(mode);
        addSetting(targetPlayers);
        addSetting(targetMobs);
        addSetting(targetAnimals);
        addSetting(throughWalls);
        addSetting(forceCriticals);
    }

    @Override
    protected void onEnable() {
        switchIndex = 0;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        List<LivingEntity> targets = collectTargets();
        if (targets.isEmpty()) return;

        if ("Multi".equals(mode.getValue())) {
            for (LivingEntity target : targets) {
                attack(target);
            }
        } else {
            LivingEntity target = selectTarget(targets);
            if (target != null) {
                attack(target);
                if ("Switch".equals(mode.getValue()) && !targets.isEmpty()) {
                    switchIndex = (switchIndex + 1) % targets.size();
                }
            }
        }
    }

    private void attack(LivingEntity target) {
        if (mc.player.getAttackCooldownProgress(0.0f) < 0.9f) return;

        if (forceCriticals.isOn() && mc.player.isAlive() && mc.player.isOnGround()) {
            doPacketCrit();
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void doPacketCrit() {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        boolean hCol = mc.player.horizontalCollision;

        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false, hCol));
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, hCol));
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.1e-5, z, false, hCol));
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, hCol));
    }

    private List<LivingEntity> collectTargets() {
        List<LivingEntity> result = new ArrayList<>();
        double rangeSq = range.getValue() * range.getValue();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player) continue;
            if (!living.isAlive()) continue;
            if (mc.player.squaredDistanceTo(living) > rangeSq) continue;
            if (!throughWalls.isOn() && !mc.player.canSee(living)) continue;
            if (!isAllowed(living)) continue;

            result.add(living);
        }

        result.sort(Comparator.comparingDouble(mc.player::squaredDistanceTo));
        return result;
    }

    private LivingEntity selectTarget(List<LivingEntity> targets) {
        if (targets.isEmpty()) return null;
        if ("Switch".equals(mode.getValue())) {
            if (switchIndex >= targets.size()) switchIndex = 0;
            return targets.get(switchIndex);
        }
        return targets.getFirst();
    }

    private boolean isAllowed(LivingEntity entity) {
        if (entity instanceof PlayerEntity) return targetPlayers.isOn();
        if (entity instanceof Monster) return targetMobs.isOn();
        if (entity instanceof AnimalEntity || entity instanceof PassiveEntity) return targetAnimals.isOn();
        return false;
    }
}
