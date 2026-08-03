package com.onehumanawa.clrcore;

import com.onehumanawa.clrcore.block.andesite_scrap_bucket.AndesiteScrapBucketBlock;
import com.onehumanawa.clrcore.block.brass_scrap_bucket.BrassScrapBucketBlock;
import com.onehumanawa.clrcore.block.labeled_redstone_link.LabeledRedstoneLinkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CLRCore.MOD_ID);

    public static final RegistryObject<Block> ANDESITE_SCRAP_BUCKET =
            BLOCKS.register("andesite_scrap_bucket",
                    () -> new AndesiteScrapBucketBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .sound(SoundType.METAL)
                                    .strength(1.5f, 6.0f)
                                    .noOcclusion()
                                    .requiresCorrectToolForDrops()
                    )
            );

    public static final RegistryObject<Block> BRASS_SCRAP_BUCKET =
            BLOCKS.register("brass_scrap_bucket",
                    () -> new BrassScrapBucketBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .sound(SoundType.METAL)
                                    .strength(2.0f, 6.0f)
                                    .noOcclusion()
                                    .requiresCorrectToolForDrops()
                    )
            );

    public static final RegistryObject<Block> LABELED_REDSTONE_LINK =
            BLOCKS.register("labeled_redstone_link",
                    () -> new LabeledRedstoneLinkBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .sound(SoundType.METAL)
                                    .strength(2.0f, 6.0f)
                                    .noOcclusion()
                                    .isRedstoneConductor((s, l, p) -> false)
                    ));
}