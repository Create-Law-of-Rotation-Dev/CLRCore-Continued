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
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> scrapBucketBlacklistedItems;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> scrapBucketBlacklistedFluids;

        ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.push("scrap_bucket");

            builder.comment("=========================================",
                    "  Scrap Bucket Blacklist Configuration",
                    "=========================================",
                    "Add items and fluids that should NOT be destroyed",
                    "by the Andesite Scrap Bucket.",
                    "",
                    "Format: modid:item_id",
                    "Example: minecraft:diamond");

            scrapBucketBlacklistedItems = builder
                    .comment("Items that cannot be destroyed by the scrap bucket")
                    .defineList("blacklisted_items",
                            Arrays.asList(
                                    "minecraft:nether_star",
                                    "minecraft:dragon_egg",
                                    "minecraft:elytra",
                                    "minecraft:netherite_ingot",
                                    "minecraft:netherite_block",
                                    "minecraft:enchanting_table",
                                    "minecraft:beacon"
                            ),
                            obj -> obj instanceof String && ((String) obj).contains(":"));

            scrapBucketBlacklistedFluids = builder
                    .comment("Fluids that cannot be destroyed by the scrap bucket")
                    .defineList("blacklisted_fluids",
                            Arrays.asList(
                                    "minecraft:lava",
                                    "minecraft:water"
                            ),
                            obj -> obj instanceof String && ((String) obj).contains(":"));

            builder.pop();
        }
    }
}