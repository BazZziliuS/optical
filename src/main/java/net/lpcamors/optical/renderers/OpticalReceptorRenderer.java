package net.lpcamors.optical.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.lpcamors.optical.COPartialModels;
import net.lpcamors.optical.blocks.optical_receptor.OpticalReceptorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class OpticalReceptorRenderer extends ShaftRenderer<OpticalReceptorBlockEntity> {
    public OpticalReceptorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(OpticalReceptorBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource bufferSource, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, bufferSource, light, overlay);
        BlockState state = be.getBlockState();
        ms.translate(0, 0, 0);
        be.sensors.forEach((dirs, stack) -> {
            if (!stack.isEmpty()) {
                SuperByteBuffer cube = CachedBuffers.partial(COPartialModels.OPTICAL_DEVICE_HORIZONTAL, state).center()
                        .light(light);
                cube.rotateToFace(dirs);
                cube.renderInto(ms, bufferSource.getBuffer(RenderType.solid()));
            }
        });
    }

}
