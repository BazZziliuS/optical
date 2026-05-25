package net.lpcamors.optical.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.lpcamors.optical.COPartialModels;
import net.lpcamors.optical.CORenderTypes;
import net.lpcamors.optical.blocks.optical_sensor.OpticalSensorBlock;
import net.lpcamors.optical.blocks.optical_sensor.OpticalSensorBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

public class OpticalSensorRenderer extends SafeBlockEntityRenderer<OpticalSensorBlockEntity> {

    public OpticalSensorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(OpticalSensorBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource bufferSource, int light, int overlay) {
        BlockState blockState = be.getBlockState();

        boolean f = be.getSignal() > 0;
        Vec3i color = blockState.getValue(OpticalSensorBlock.MODE).getColor(be.getOptionalBeamProperties());
        if (color.equals(Vec3i.ZERO))
            return;
        float rotation = blockState.getValue(OpticalSensorBlock.HANGING) ? (float) Math.PI : 0.0F;
        ms.pushPose();

        CachedBuffers.partial(COPartialModels.OPTICAL_SENSOR_LAMP_GLOW, blockState)
                .light(f ? LightTexture.FULL_BRIGHT : light)
                .color(color.getX(), color.getY(), color.getZ(), 255)
                .disableDiffuse()
                .center().rotateX(rotation).uncenter()
                .renderInto(ms, bufferSource
                        .getBuffer(f ? CORenderTypes.TRANSPARENT_ADDITIVE : CORenderTypes.TRANSLUCENT_NO_CULL));

        if (f) {
            CachedBuffers.partial(COPartialModels.OPTICAL_SENSOR_LAMP, blockState)
                    .light(LightTexture.FULL_BRIGHT / 2)
                    .color(color.getX(), color.getY(), color.getZ(), 255)
                    .disableDiffuse()
                    .center().rotateX(rotation).uncenter()
                    .renderInto(ms, bufferSource.getBuffer(CORenderTypes.TRANSPARENT_ADDITIVE));
        }
        ms.popPose();
    }
}
