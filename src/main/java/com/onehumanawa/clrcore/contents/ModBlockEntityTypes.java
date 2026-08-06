package com.onehumanawa.clrcore.contents;

import com.onehumanawa.clrcore.contents.registry.block.brass_scrap_bucket.BrassScrapBucketRenderer;
import com.onehumanawa.clrcore.contents.registry.block.labeled_redstone_link.LabeledRedstoneLinkRenderer;
import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.registry.block.andesite_scrap_bucket.AndesiteScrapBucketBlockEntity;
import com.onehumanawa.clrcore.contents.registry.block.brass_scrap_bucket.BrassScrapBucketBlockEntity;
import com.onehumanawa.clrcore.contents.registry.block.labeled_redstone_link.LabeledRedstoneLinkBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class ModBlockEntityTypes {
    public static void register() {}

    public static final BlockEntityEntry<AndesiteScrapBucketBlockEntity> ANDESITE_SCRAP_BUCKET =
            CLRCore.REGISTRATE.blockEntity("andesite_scrap_bucket", AndesiteScrapBucketBlockEntity::new)
                    .validBlocks(ModBlocks.ANDESITE_SCRAP_BUCKET)
                    .register();

    public static final BlockEntityEntry<BrassScrapBucketBlockEntity> BRASS_SCRAP_BUCKET =
            CLRCore.REGISTRATE.blockEntity("brass_scrap_bucket", BrassScrapBucketBlockEntity::new)
                    .renderer(() -> BrassScrapBucketRenderer::new)
                    .validBlocks(ModBlocks.BRASS_SCRAP_BUCKET)
                    .register();

    public static final BlockEntityEntry<LabeledRedstoneLinkBlockEntity> LABELED_REDSTONE_LINK =
            CLRCore.REGISTRATE.blockEntity("labeled_redstone_link", LabeledRedstoneLinkBlockEntity::new)
                    .renderer(() -> LabeledRedstoneLinkRenderer::new)
                    .validBlocks(ModBlocks.LABELED_REDSTONE_LINK)
                    .register();
}