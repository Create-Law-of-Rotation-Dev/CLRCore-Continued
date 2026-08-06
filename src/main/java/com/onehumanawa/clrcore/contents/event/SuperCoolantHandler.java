package com.onehumanawa.clrcore.contents.event;

import com.onehumanawa.clrcore.core.config.CLRCoreConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SuperCoolantHandler {
    private static final Map<UUID, Long> ENTER_TICK_MAP = new HashMap<>();
    private static final double SLOW_FACTOR = 0.2;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new SuperCoolantHandler());
    }

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (level.isClientSide()) return;

        BlockPos pos = entity.blockPosition();
        FluidState fluidState = level.getFluidState(pos);
        if (fluidState.isEmpty()) {
            ENTER_TICK_MAP.remove(entity.getUUID());
            return;
        }

        Fluid fluid = fluidState.getType();
        ResourceLocation fluidKey = ForgeRegistries.FLUIDS.getKey(fluid);
        if (fluidKey == null) return;

        // 获取配置列表（安全类型转换）
        @SuppressWarnings("unchecked")
        List<String> coolantList = (List<String>) (List<?>) CLRCoreConfig.SERVER.superCoolantFluids.get();
        if (!coolantList.contains(fluidKey.toString())) {
            ENTER_TICK_MAP.remove(entity.getUUID());
            return;
        }

        // 记录进入时间
        long currentTick = level.getGameTime();
        long enterTick = ENTER_TICK_MAP.getOrDefault(entity.getUUID(), currentTick);
        ENTER_TICK_MAP.put(entity.getUUID(), enterTick);
        long stayTicks = currentTick - enterTick + 1;

        // 1. 减速效果（类似细雪）
        applySlowingEffect(entity);

        // 2. 伤害
        DamageSource freezeDamage = level.damageSources().freeze();  // 冻伤，忽略护甲
        if (entity instanceof Player) {
            float playerDamage = CLRCoreConfig.SERVER.superCoolantPlayerDamage.get().floatValue();
            entity.hurt(freezeDamage, playerDamage);
        } else {
            double basic = CLRCoreConfig.SERVER.superCoolantBasicDamage.get();
            double increase = CLRCoreConfig.SERVER.superCoolantIncrease.get();
            double damage = basic + Math.pow(increase, stayTicks);
            entity.hurt(freezeDamage, (float) damage);
        }
    }

    private void applySlowingEffect(LivingEntity entity) {
        Vec3 motion = entity.getDeltaMovement();
        // 水平减速
        entity.setDeltaMovement(motion.x * SLOW_FACTOR, motion.y, motion.z * SLOW_FACTOR);
        // 垂直速度微降，模拟下沉（类似细雪）
        if (motion.y < 0) {
            entity.setDeltaMovement(entity.getDeltaMovement().x, motion.y * 0.8, entity.getDeltaMovement().z);
        }
        // 如果垂直速度为正且实体在地面上，限制跳跃高度（但我们不依赖isOnGround，仅靠垂直速度处理）
        // 这里可以根据需要添加额外逻辑
    }
}