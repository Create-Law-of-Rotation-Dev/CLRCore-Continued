package com.onehumanawa.clrcore.contents;

import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.registry.block.andesite_scrap_bucket.AndesiteScrapBucketBlockEntity;
import com.onehumanawa.clrcore.contents.registry.block.brass_scrap_bucket.BrassScrapBucketBlockEntity;
import com.onehumanawa.clrcore.contents.registry.block.labeled_redstone_link.LabeledRedstoneLinkBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CLRCore.MOD_ID);

    public static final RegistryObject<BlockEntityType<AndesiteScrapBucketBlockEntity>> ANDESITE_SCRAP_BUCKET =
            BLOCK_ENTITY_TYPES.register("andesite_scrap_bucket",
                    () -> BlockEntityType.Builder.of(
                            AndesiteScrapBucketBlockEntity::new,
                            ModBlocks.ANDESITE_SCRAP_BUCKET.get()
                    ).build(null)
            );

    public static final RegistryObject<BlockEntityType<BrassScrapBucketBlockEntity>> BRASS_SCRAP_BUCKET =
            BLOCK_ENTITY_TYPES.register("brass_scrap_bucket",
                    () -> BlockEntityType.Builder.of(
                            BrassScrapBucketBlockEntity::new,
                            ModBlocks.BRASS_SCRAP_BUCKET.get()
                    ).build(null)
            );

    public static final RegistryObject<BlockEntityType<LabeledRedstoneLinkBlockEntity>> LABELED_REDSTONE_LINK =
            BLOCK_ENTITY_TYPES.register("labeled_redstone_link",
                    () -> BlockEntityType.Builder.of(
                            LabeledRedstoneLinkBlockEntity::new,
                            ModBlocks.LABELED_REDSTONE_LINK.get()
                    ).build(null)
            );
}