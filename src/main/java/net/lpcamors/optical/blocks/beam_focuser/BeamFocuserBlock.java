package net.lpcamors.optical.blocks.beam_focuser;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.lpcamors.optical.COShapes;
import net.lpcamors.optical.blocks.COBlockEntities;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeamFocuserBlock extends HorizontalKineticBlock
        implements GenericOpticalSourceBlockEntity.IBeamActivator, IBE<BeamFocuserBlockEntity> {

    public BeamFocuserBlock(Properties p_54120_) {
        super(p_54120_);
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_,
            CollisionContext p_60558_) {
        return COShapes.FOCUSER.get(p_60555_.getValue(HORIZONTAL_FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredHorizontalFacing(context);
        if (preferred != null)
            return defaultBlockState().setValue(HORIZONTAL_FACING, preferred.getOpposite());
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public Class<BeamFocuserBlockEntity> getBlockEntityClass() {
        return BeamFocuserBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BeamFocuserBlockEntity> getBlockEntityType() {
        return COBlockEntities.BEAM_FOCUSER.get();
    }

    @Override
    public boolean canReceive(Level level, BlockState state, BlockPos pos, BeamProperties prop) {

        return prop.direction().equals(Direction.DOWN)
                && level.getBlockEntity(pos, this.getBlockEntityType()).isPresent();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public BeamProperties transformProperties(Level level, BlockState state, BlockPos pos, BeamProperties prop,
            GenericOpticalSourceBlockEntity source, int range) {
        return null;
    }

    @Override
    public void onRemoveBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(be -> {
            be.removeBeam();
        });

    }

    @Override
    public void onReceiveBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        if (!this.canReceive(level, state, pos, prop))
            return;
        level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(be -> {
            be.receiveBeam(prop, false);
        });
    }

    @Override
    public AABB getNonVisibleAABB(Level level, BlockState state, BlockPos pos) {
        return null;
    }

    @Override
    public void onUpdateBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        if (!this.canReceive(level, state, pos, prop))
            return;
        level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(be -> {
            be.receiveBeam(prop, true);
        });
    }

}
