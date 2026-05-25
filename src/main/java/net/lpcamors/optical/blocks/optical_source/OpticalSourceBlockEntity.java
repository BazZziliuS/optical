package net.lpcamors.optical.blocks.optical_source;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;

import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.VecHelper;
import net.lpcamors.optical.COUtils;
import net.lpcamors.optical.CreateOptical;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class OpticalSourceBlockEntity extends GenericOpticalSourceBlockEntity {

    private ScrollOptionBehaviour<BeamHelper.BeamPolarization> polarization;

    public OpticalSourceBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        this.polarization = new ScrollOptionBehaviour<>(BeamHelper.BeamPolarization.class,
                Lang.builder("tooltip").translate(CreateOptical.ID + ".gui.behaviour.optical_source").component(), this,
                new PolarizationValueBoxTransform());
        behaviours.add(polarization);
    }

    @Override
    public BeamHelper.BeamProperties getInitialBeamProperties() {
        return new BeamHelper.BeamProperties(
                this.getIntensity(),
                this.getBlockState().getValue(OpticalSourceBlock.HORIZONTAL_FACING),
                BeamHelper.BeamType.getTypeBySpeed(this.speed),
                this.polarization.get(),
                COUtils.getColor(DyeColor.GRAY), List.of(),
                BeamHelper.spinBySpeed(this.getSpeed()), false, false, false);
    }

    public ScrollOptionBehaviour<BeamHelper.BeamPolarization> getPolarization() {
        return polarization;
    }

    public float getIntensity() {
        return BeamHelper.intensityBySpeed(this.getSpeed());
    }

    public boolean shouldRendererLaserBeam() {
        return this.getSpeed() != 0
                && (this.getInitialBeamProperties() != null && this.getInitialBeamProperties().isVisible());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return AABB.INFINITE;
    }

    @Override
    public boolean isActive() {
        return this.getSpeed() != 0;
    }

    private static class PolarizationValueBoxTransform extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 6f, 15.5f);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            if (direction.getAxis().isVertical())
                return false;
            return state.getValue(OpticalSourceBlock.HORIZONTAL_FACING).getClockWise().getAxis()
                    .equals(direction.getAxis());
        }

        @Override
        public float getScale() {
            return 0.5f;
        }

    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        GenericOpticalSourceBlockEntity.goggleTooltip(tooltip, isPlayerSneaking, this);
        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);

    }

}
