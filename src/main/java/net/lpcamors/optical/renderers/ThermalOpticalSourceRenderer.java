package net.lpcamors.optical.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.lpcamors.optical.blocks.thermal_optical_source.ThermalOpticalSourceBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ThermalOpticalSourceRenderer extends OpticalSourceRenderer {

    public ThermalOpticalSourceRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(GenericOpticalSourceBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        if (be instanceof ThermalOpticalSourceBlockEntity thermalOpticalSourceBlockEntity)
            this.renderFluid(thermalOpticalSourceBlockEntity, partialTicks, ms, buffer, light);
    }

    protected void renderFluid(ThermalOpticalSourceBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource buffer,
            int light) {
        SmartFluidTankBehaviour tank = be.internalTank;
        if (tank == null)
            return;

        SmartFluidTankBehaviour.TankSegment primaryTank = tank.getPrimaryTank();
        net.neoforged.neoforge.fluids.FluidStack fluidStack = primaryTank.getRenderedFluid();
        float level = primaryTank.getFluidLevel()
                .getValue(partialTicks);

        if (!fluidStack.isEmpty() && level != 0) {
            float yMin = 1.01f / 16f;
            float min = 1.01f / 16f;
            float max = 14.99F / 16f;
            float yOffset = yMin + (8.97F / 16f) * level;
            ms.pushPose();
            ms.translate(0, yOffset, 0);
            NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluidStack, min, yMin - yOffset, min, max, yMin,
                    max, buffer, ms, light, false, false);
            ms.popPose();
        }
    }
}
