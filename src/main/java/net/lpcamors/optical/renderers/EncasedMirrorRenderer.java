package net.lpcamors.optical.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;

import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.lpcamors.optical.COPartialModels;
import net.lpcamors.optical.blocks.encased_mirror.EncasedMirrorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class EncasedMirrorRenderer extends ShaftRenderer<EncasedMirrorBlockEntity> {

    public EncasedMirrorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(EncasedMirrorBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource bufferSource, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, bufferSource, light, overlay);
        BlockState state = be.getBlockState();
        SuperByteBuffer mirror = CachedBuffers.partial(COPartialModels.MIRROR, state);

        var facing = state.getValue(BlockStateProperties.FACING).getOpposite();

        float angle = AngleHelper.rad(be.getIndependentAngle(partialTicks));

        mirror.center();
        mirror.rotate(angle, Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis())).rotateToFace(facing)
                .uncenter();

        mirror.light(light).renderInto(ms, bufferSource.getBuffer(RenderType.cutoutMipped()));

    }

}
