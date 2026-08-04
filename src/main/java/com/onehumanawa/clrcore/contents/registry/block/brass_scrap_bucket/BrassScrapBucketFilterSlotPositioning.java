package com.onehumanawa.clrcore.contents.registry.block.brass_scrap_bucket;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BrassScrapBucketFilterSlotPositioning extends ValueBoxTransform.Sided {

    @Override
    protected Vec3 getSouthLocation() {
        // 在方块侧面的位置，Y往上3像素
        return VecHelper.voxelSpace(8, 11, 15.5f);
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        // 所有水平方向都显示过滤器槽位
        return direction.getAxis().isHorizontal();
    }
}