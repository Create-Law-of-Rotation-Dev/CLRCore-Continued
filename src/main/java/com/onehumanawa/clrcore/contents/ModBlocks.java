package com.onehumanawa.clrcore.contents;

import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.registry.block.andesite_scrap_bucket.AndesiteScrapBucketBlock;
import com.onehumanawa.clrcore.contents.registry.block.brass_scrap_bucket.BrassScrapBucketBlock;
import com.onehumanawa.clrcore.contents.registry.block.labeled_redstone_link.LabeledRedstoneLinkBlock;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class ModBlocks {
    public static final BlockEntry<AndesiteScrapBucketBlock> ANDESITE_SCRAP_BUCKET = CLRCore.REGISTRATE
            .block("andesite_scrap_bucket", AndesiteScrapBucketBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.METAL))
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<BrassScrapBucketBlock> BRASS_SCRAP_BUCKET = CLRCore.REGISTRATE
            .block("brass_scrap_bucket", BrassScrapBucketBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.METAL))
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<LabeledRedstoneLinkBlock> LABELED_REDSTONE_LINK = CLRCore.REGISTRATE
            .block("labeled_redstone_link", LabeledRedstoneLinkBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.METAL))
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static void register() {}
}