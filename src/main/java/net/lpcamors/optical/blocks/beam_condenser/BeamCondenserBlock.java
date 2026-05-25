package net.lpcamors.optical.blocks.beam_condenser;

import java.util.Arrays;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;

import org.jetbrains.annotations.NotNull;

import net.createmod.catnip.data.Iterate;
import net.lpcamors.optical.COShapes;
import net.lpcamors.optical.blocks.COBlockEntities;
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

public class BeamCondenserBlock extends HorizontalDirectionalBlock
        implements GenericOpticalSourceBlockEntity.IBeamActivator, IBE<BeamCondenserBlockEntity>, IWrenchable {
    public static final MapCodec<BeamCondenserBlock> CODEC = simpleCodec(BeamCondenserBlock::new);

    public BeamCondenserBlock(Properties p_54120_) {
        super(p_54120_);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState p_60555_, @NotNull BlockGetter p_60556_, @NotNull BlockPos p_60557_,
            @NotNull CollisionContext p_60558_) {
        return (COShapes.BEAM_CONDENSER).get(p_60555_.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_49915_) {
        super.createBlockStateDefinition(p_49915_.add(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferredFacing = getPreferredFacing(context);
        if (preferredFacing == null)
            preferredFacing = (Direction) Arrays.stream(context.getNearestLookingDirections())
                    .filter(direction -> direction.getAxis().isHorizontal()).toArray()[0];
        return defaultBlockState().setValue(FACING, context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown() ? preferredFacing : preferredFacing.getOpposite());
    }

    public Direction getPreferredFacing(BlockPlaceContext context) {
        Direction prefferedSide = null;
        for (Direction side : Iterate.directions) {
            BlockState blockState = context.getLevel()
                    .getBlockState(context.getClickedPos()
                            .relative(side));
            if (blockState.getBlock() instanceof IRotate) {
                if (((IRotate) blockState.getBlock()).hasShaftTowards(context.getLevel(), context.getClickedPos()
                        .relative(side), blockState, side.getOpposite()))
                    if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
                        prefferedSide = null;
                        break;
                    } else {
                        prefferedSide = side;
                    }
            }
        }
        return prefferedSide != null && prefferedSide.getAxis().isVertical() ? null : prefferedSide;
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
            be.removeBeam(prop);
        });
    }

    @Override
    public boolean canReceive(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        return !state.getValue(FACING).equals(prop.direction().getOpposite()) && !prop.isDirty();
    }

    @Override
    public BeamProperties transformProperties(Level level, BlockState state, BlockPos pos, BeamProperties prop,
            GenericOpticalSourceBlockEntity source, int range) {
        return null;
    }

    @Override
    public AABB getNonVisibleAABB(Level level, BlockState state, BlockPos pos) {
        return CENTERED_OFFSET;
    }

    @Override
    public Class<BeamCondenserBlockEntity> getBlockEntityClass() {
        return BeamCondenserBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BeamCondenserBlockEntity> getBlockEntityType() {
        return COBlockEntities.BEAM_CONDENSER.get();
    }

}
