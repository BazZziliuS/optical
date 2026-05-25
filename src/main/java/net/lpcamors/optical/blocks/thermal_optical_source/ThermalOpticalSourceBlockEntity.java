package net.lpcamors.optical.blocks.thermal_optical_source;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.lpcamors.optical.blocks.COBlockEntities;
import net.lpcamors.optical.blocks.optical_source.OpticalSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class ThermalOpticalSourceBlockEntity extends OpticalSourceBlockEntity {

    public SmartFluidTankBehaviour internalTank;
    protected IFluidHandler fluidCapability;
    protected int tick = 0;

    public ThermalOpticalSourceBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        this.tick++;
        if (this.tick % 24 == 0) {
            if (this.isActive()) {
                this.internalTank.getPrimaryHandler().drain(
                        Math.min(5, this.internalTank.getPrimaryHandler().getFluidAmount()), FluidAction.EXECUTE);
            }
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(internalTank = SmartFluidTankBehaviour.single(this, 1000)
                .allowExtraction()
                .allowInsertion());
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.internalTank.getPrimaryHandler().getFluidAmount() > 0;
    }

    @Override
    public float getIntensity() {
        Fluid fluid = this.internalTank.getPrimaryHandler().getFluid().getFluid();
        return super.getIntensity()
                * (Fluids.WATER.getSource().isSame(fluid) ? 2F : Fluids.LAVA.getSource().isSame(fluid) ? 4F : 1F);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                COBlockEntities.THERMAL_OPTICAL_SOURCE.get(),
                (be, dir) -> {
                    if (ThermalOpticalSourceBlock.hasPipeTowards(be.getBlockState(), dir))
                        return be.internalTank.getCapability();
                    return null;
                });
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        containedFluidTooltip(tooltip, isPlayerSneaking,
                level.getCapability(Capabilities.FluidHandler.BLOCK, this.getBlockPos(), null));
        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

}
