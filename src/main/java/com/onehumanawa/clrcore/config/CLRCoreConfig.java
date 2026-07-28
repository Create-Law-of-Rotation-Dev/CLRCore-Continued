package com.onehumanawa.clrcore.config;

import net.minecraftforge.common.ForgeConfigSpec;

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

        // ===== 网络管理器配置 =====
        public final ForgeConfigSpec.ConfigValue<Integer> longPressThreshold;

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

            // ===== 网络管理器配置 =====
            builder.push("network_manager");

            longPressThreshold = builder
                    .comment("Long press threshold in ticks before opening network manager config screen",
                            "Default: 20 ticks (1 second)")
                    .defineInRange("longPressThreshold", 20, 1, 100);

            builder.pop();
        }
    }
}