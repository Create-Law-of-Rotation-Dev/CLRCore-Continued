package com.onehumanawa.clrcore.contents.datagen.tags.block;

import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CLRCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(BlockTags.create(CLRCore.rl("create:wrench_pickup")))
                .add(ModBlocks.ANDESITE_SCRAP_BUCKET.get())
                .add(ModBlocks.BRASS_SCRAP_BUCKET.get());
    }
}