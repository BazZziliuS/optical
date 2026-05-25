package net.lpcamors.optical.blocks.polarizing_beam_splitter_block;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import org.jetbrains.annotations.NotNull;

import net.lpcamors.optical.COShapes;
import net.lpcamors.optical.blocks.COBlockEntities;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamPolarization;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PolarizingBeamSplitterBlock extends HorizontalDirectionalBlock
        implements IWrenchable, GenericOpticalSourceBlockEntity.IBeamActivator, IBE<PolarizingBeamSplitterBlockEntity> {

    public static final MapCodec<PolarizingBeamSplitterBlock> CODEC = simpleCodec(PolarizingBeamSplitterBlock::new);

    public PolarizingBeamSplitterBlock(Properties p_54120_) {
        super(p_54120_);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_49915_) {
        super.createBlockStateDefinition(p_49915_);
        p_49915_.add(FACING);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState p_60555_, @NotNull BlockGetter p_60556_, @NotNull BlockPos p_60557_,
            @NotNull CollisionContext p_60558_) {
        return COShapes.POLARIZING_BEAM_SPLITTER_CUBE.get(p_60555_.getValue(FACING));
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getClockWise());
    }

    @Override
    public Class<PolarizingBeamSplitterBlockEntity> getBlockEntityClass() {
        return PolarizingBeamSplitterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PolarizingBeamSplitterBlockEntity> getBlockEntityType() {
        return COBlockEntities.POLARIZING_BEAM_SPLITTER.get();
    }

    @Override
    public BeamProperties transformProperties(Level level, BlockState state, BlockPos pos, BeamProperties prop,
            GenericOpticalSourceBlockEntity source, int range) {
        float strtIntesity = prop.polarization().getRemainingIntensity(prop.intensity(),
                BeamHelper.BeamPolarization.VERTICAL);
        float perpIntesity = prop.polarization().getRemainingIntensity(prop.intensity(),
                BeamHelper.BeamPolarization.HORIZONTAL);

        if (prop.direction().getAxis().isVertical())
            return null;

        if (perpIntesity > 0) {
            Direction dir = prop.direction().getAxis().equals(state.getValue(FACING).getAxis())
                    ? prop.direction().getClockWise()
                    : prop.direction().getCounterClockWise();

            BeamProperties prop1 = new BeamHelper.BeamProperties.Builder(prop).intensity(perpIntesity)
                    .polarization(BeamPolarization.HORIZONTAL).direction(dir).build();
            source.propagate(prop1, pos, range);
        }

        if (strtIntesity > 0) {
            return new BeamHelper.BeamProperties.Builder(prop).intensity(strtIntesity)
                    .polarization(BeamPolarization.VERTICAL).build();
        } else {
            return null;
        }
    }

    @Override
    public boolean canReceive(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        return !prop.direction().getAxis().isVertical();
    }

    @Override
    public void onRemoveBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
    }

    @Override
    public void onReceiveBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
    }

    @Override
    public void onUpdateBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
    }

    @Override
    public AABB getNonVisibleAABB(Level level, BlockState state, BlockPos pos) {
        return null;
    }

}
