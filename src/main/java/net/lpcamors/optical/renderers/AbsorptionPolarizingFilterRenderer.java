package net.lpcamors.optical.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.lpcamors.optical.COPartialModels;
import net.lpcamors.optical.blocks.absorption_polarizing_filter.AbsorptionPolarizingFilterBlock;
import net.lpcamors.optical.blocks.absorption_polarizing_filter.AbsorptionPolarizingFilterBlockEntity;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class AbsorptionPolarizingFilterRenderer extends SafeBlockEntityRenderer<AbsorptionPolarizingFilterBlockEntity> {
    public AbsorptionPolarizingFilterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(AbsorptionPolarizingFilterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction direction = state.getValue(AbsorptionPolarizingFilterBlock.FACING);
        SuperByteBuffer filter = CachedBuffers.partial(COPartialModels.POLARIZING_FILTER, state);
        BeamHelper.BeamPolarization beamPolarization = state.getValue(AbsorptionPolarizingFilterBlock.POLARIZATION);
        if(beamPolarization != BeamHelper.BeamPolarization.RANDOM){
            rotateFilter(filter, (float) (beamPolarization.getAngle()), direction).light(light).renderInto(ms, bufferSource.getBuffer(RenderType.translucent()));
        }


    }

    private SuperByteBuffer rotateFilter(SuperByteBuffer buffer, float angleRad, Direction facing) {
        float pivotX = 8F / 16f;
        float pivotY = 8f / 16f;
        float pivotZ = 8F / 16f;
        buffer.rotateCentered((float) (AngleHelper.rad(AngleHelper.horizontalAngle(facing.getCounterClockWise())) - 1.5F * Math.PI), Direction.UP);
        buffer.translate(pivotX, pivotY, pivotZ);
        buffer.rotate(angleRad, Direction.EAST);
        buffer.translate(-pivotX, -pivotY, -pivotZ);
        return buffer;
    }
}
