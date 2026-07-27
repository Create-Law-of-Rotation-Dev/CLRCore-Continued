package com.onehumanawa.clrcore.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
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
        // 安山岩废料桶黑名单
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> scrapBucketBlacklistedItems;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> scrapBucketBlacklistedFluids;

        // 黄铜废料桶配置
        public final ForgeConfigSpec.ConfigValue<Integer> itemsPerNugget;
        public final ForgeConfigSpec.ConfigValue<Integer> mbPerNugget;
        public final ForgeConfigSpec.ConfigValue<Boolean> generateExperienceNuggets;
        public final ForgeConfigSpec.ConfigValue<String> brassScrapBucketProduceItem;

        ServerConfig(ForgeConfigSpec.Builder builder) {
            // 安山岩废料桶黑名单
            builder.push("scrap_bucket");

            scrapBucketBlacklistedItems = builder
                    .comment("Items that cannot be destroyed by the scrap bucket (format: modid:item)")
                    .defineList("blacklisted_items",
                            Arrays.asList(
                                    "minecraft:nether_star",
                                    "minecraft:dragon_egg",
                                    "minecraft:elytra",
                                    "minecraft:netherite_ingot",
                                    "minecraft:netherite_block"
                            ),
                            obj -> obj instanceof String && ((String) obj).contains(":"));

            scrapBucketBlacklistedFluids = builder
                    .comment("Fluids that cannot be destroyed by the scrap bucket (format: modid:fluid)")
                    .defineList("blacklisted_fluids",
                            Arrays.asList("minecraft:lava", "minecraft:water"),
                            obj -> obj instanceof String && ((String) obj).contains(":"));

            builder.pop();

            // 黄铜废料桶配置
            builder.push("brass_scrap_bucket");

            itemsPerNugget = builder
                    .comment("Number of items needed to produce one experience nugget")
                    .defineInRange("itemsPerNugget", 64, 1, Integer.MAX_VALUE);

            mbPerNugget = builder
                    .comment("Number of fluid mb needed to produce one experience nugget")
                    .defineInRange("mbPerNugget", 2000, 1, Integer.MAX_VALUE);

            generateExperienceNuggets = builder
                    .comment("Whether to generate experience nuggets from destroyed items/fluids")
                    .define("generateExperienceNuggets", true);

            brassScrapBucketProduceItem = builder
                    .comment("Item produced by the brass scrap bucket (default: create:experience_nugget)")
                    .define("brassScrapBucketProduceItem", "create:experience_nugget");

            builder.pop();
        }
    }
}