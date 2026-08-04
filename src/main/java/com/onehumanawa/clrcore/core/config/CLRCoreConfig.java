package com.onehumanawa.clrcore.core.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;

public class CLRCoreConfig {
    private CLRCoreConfig() {}

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ServerConfig SERVER;

    static {
        var builder = new ForgeConfigSpec.Builder();
        SERVER = new ServerConfig(builder);
        SERVER_SPEC = builder.build();
    }

    public static class ServerConfig {
        // ===== 黄铜废料桶配置 =====
        public final ForgeConfigSpec.ConfigValue<Integer> itemsPerNugget;
        public final ForgeConfigSpec.ConfigValue<Integer> mbPerNugget;
        public final ForgeConfigSpec.ConfigValue<Boolean> generateExperienceNuggets;
        public final ForgeConfigSpec.ConfigValue<String> brassScrapBucketProduceItem;
        public final ForgeConfigSpec.ConfigValue<Integer> itemTransferAmount;
        public final ForgeConfigSpec.ConfigValue<Integer> itemTransferInterval;
        public final ForgeConfigSpec.ConfigValue<Integer> fluidTransferAmount;
        public final ForgeConfigSpec.ConfigValue<Integer> fluidTransferInterval;
        // ===== 超级冷却剂 =====
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> superCoolantFluids;
        public final ForgeConfigSpec.ConfigValue<Double> superCoolantBasicDamage;
        public final ForgeConfigSpec.ConfigValue<Double> superCoolantIncrease;
        public final ForgeConfigSpec.ConfigValue<Double> superCoolantPlayerDamage;

        ServerConfig(ForgeConfigSpec.Builder builder) {
            // ===== 黄铜废料桶配置 =====
            builder.push("brass_scrap_bucket");

            itemsPerNugget = builder
                    .comment("Number of items needed to produce one experience nugget",
                            "Default: 64")
                    .defineInRange("itemsPerNugget", 64, 1, Integer.MAX_VALUE);

            mbPerNugget = builder
                    .comment("Number of fluid mb needed to produce one experience nugget",
                            "Default: 2000 (2 buckets)")
                    .defineInRange("mbPerNugget", 2000, 1, Integer.MAX_VALUE);

            generateExperienceNuggets = builder
                    .comment("Whether to generate experience nuggets from destroyed items/fluids",
                            "Default: true")
                    .define("generateExperienceNuggets", true);

            brassScrapBucketProduceItem = builder
                    .comment("Item produced by the brass scrap bucket",
                            "Default: create:experience_nugget")
                    .define("brassScrapBucketProduceItem", "create:experience_nugget");

            itemTransferAmount = builder
                    .comment("Number of items to transfer per operation when draining from above container",
                            "Default: 64")
                    .defineInRange("itemTransferAmount", 64, 1, Integer.MAX_VALUE);

            itemTransferInterval = builder
                    .comment("Ticks between item transfer operations when draining from above container",
                            "Default: 10 ticks (0.5 seconds)")
                    .defineInRange("itemTransferInterval", 10, 1, Integer.MAX_VALUE);

            fluidTransferAmount = builder
                    .comment("Amount of fluid (mb) to transfer per operation when draining from above container",
                            "Default: 1024 mb (1 bucket)")
                    .defineInRange("fluidTransferAmount", 1024, 1, Integer.MAX_VALUE);

            fluidTransferInterval = builder
                    .comment("Ticks between fluid transfer operations when draining from above container",
                            "Default: 10 ticks (0.5 seconds)")
                    .defineInRange("fluidTransferInterval", 10, 1, Integer.MAX_VALUE);

            builder.pop();

            builder.push("super_coolant");

            superCoolantFluids = builder
                    .comment(
                            "List of fluid registry names that act as super coolant.",
                            "Entities entering these fluids will experience freezing effects (like powdered snow) ",
                            "and take exponentially increasing damage over time.",
                            "Players take a fixed (non‑exponential) amount of damage instead.",
                            "Format: each entry is a resource location, e.g. \"minecraft:water\"",
                            "Default: empty list")
                    .defineList("fluids", Collections.emptyList(), o -> o instanceof String);
            superCoolantBasicDamage = builder
                    .comment("Base damage applied each tick to non‑player entities in the fluid (before exponential term).",
                            "Default: 1.0")
                    .defineInRange("basicDamage", 1.0, 0.0, Double.MAX_VALUE);

            superCoolantIncrease = builder
                    .comment("Base factor for exponential growth per tick (damage = basicDamage + increase^tickCount).",
                            "Default: 1.1")
                    .defineInRange("increaseFactor", 1.1, 1.0, Double.MAX_VALUE);

            superCoolantPlayerDamage = builder
                    .comment("Fixed damage applied to players per tick while in the fluid (no exponential component).",
                            "Default: 1.0")
                    .defineInRange("playerDamage", 1.0, 0.0, Double.MAX_VALUE);

            builder.pop();
        }
    }
}