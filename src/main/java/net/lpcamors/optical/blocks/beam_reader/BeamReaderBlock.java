package net.lpcamors.optical.blocks.beam_reader;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import org.jetbrains.annotations.NotNull;

import net.lpcamors.optical.COShapes;
import net.lpcamors.optical.blocks.COBlockEntities;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.minecraft.core.BlockPos;
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

public class BeamReaderBlock extends HorizontalDirectionalBlock implements IWrenchable,
        GenericOpticalSourceBlockEntity.IBeamActivator, IBE<BeamReaderBlockEntity> {

    public static final MapCodec<BeamReaderBlock> CODEC = simpleCodec(
            BeamReaderBlock::new);

    public BeamReaderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState p_60555_, @NotNull BlockGetter p_60556_, @NotNull BlockPos p_60557_,
            @NotNull CollisionContext p_60558_) {
        return (COShapes.BEAM_READER).get(p_60555_.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_49915_) {
        super.createBlockStateDefinition(p_49915_.add(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection().getClockWise().getOpposite());
    }

    @Override
    public BeamProperties transformProperties(Level level, BlockState state, BlockPos pos, BeamProperties prop,
            GenericOpticalSourceBlockEntity source, int range) {
        if (this.canReceive(level, state, pos, prop))
            return prop;
        return null;
    }

    @Override
    public boolean canReceive(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        return state.getValue(FACING).getAxis().equals(prop.direction().getAxis());
    }

    @Override
    public void onReceiveBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(be -> {
            be.receiveBeam(prop, false);
        });

    }

    @Override
    public void onUpdateBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        if (!this.canReceive(level, state, pos, prop))
            return;
        level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(be -> {
            be.receiveBeam(prop, true);
        });
    }

    @Override
    public void onRemoveBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(be -> {
            be.removeBeam();
        });
    }

    @Override
    public AABB getNonVisibleAABB(Level level, BlockState state, BlockPos pos) {
        return CENTERED_OFFSET;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return BeamReaderBlock.CODEC;
    }

    @Override
    public Class<BeamReaderBlockEntity> getBlockEntityClass() {
        return BeamReaderBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BeamReaderBlockEntity> getBlockEntityType() {
        return COBlockEntities.BEAM_READER.get();
    }

}
