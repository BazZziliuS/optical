package net.lpcamors.optical.blocks.encased_mirror;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import org.jetbrains.annotations.NotNull;

import net.lpcamors.optical.COShapes;
import net.lpcamors.optical.COUtils;
import net.lpcamors.optical.blocks.COBlockEntities;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class EncasedMirrorBlock extends DirectionalKineticBlock
        implements IBE<EncasedMirrorBlockEntity>, GenericOpticalSourceBlockEntity.IBeamActivator {

    public static BooleanProperty ENCASED = BooleanProperty.create("encased");

    public static NonNullBiConsumer<DataGenContext<Block, EncasedMirrorBlock>, RegistrateBlockstateProvider> stateProvider() {
        return (a, b) -> {
            b.directionalBlock(a.get(), getBlockModel(a, b));
            ;
        };
    }

    public static Function<BlockState, ModelFile> getBlockModel(DataGenContext<Block, EncasedMirrorBlock> c,
            RegistrateBlockstateProvider p) {
        return state -> {
            return AssetLookup.partialBaseModel(c, p,
                    state.getValue(ENCASED).booleanValue() ? "encased" : "");
        };
    }

    public EncasedMirrorBlock(Properties p_54120_) {
        super(p_54120_);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return state.getValue(ENCASED).booleanValue() ? COShapes.ENCASED_MIRROR.get(state.getValue(FACING))
                : COShapes.ENCASED_MIRROR_SHAFT.get(state.getValue(FACING));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis().equals(state.getValue(FACING).getAxis());
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> p_49915_) {
        super.createBlockStateDefinition(p_49915_);
        p_49915_.add(ENCASED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(ENCASED, true);
    }

    @Override
    public Class<EncasedMirrorBlockEntity> getBlockEntityClass() {
        return EncasedMirrorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EncasedMirrorBlockEntity> getBlockEntityType() {
        return COBlockEntities.ENCASED_MIRROR.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (AllBlocks.ANDESITE_CASING.is(stack)) {
            if (level.isClientSide()) {
                return ItemInteractionResult.SUCCESS;
            }
            level.setBlock(pos, state.setValue(ENCASED, true), 3);
            level.updateNeighborsAt(pos, this);
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if (state.getValue(ENCASED)) {
            if (context.getLevel().isClientSide)
                return InteractionResult.SUCCESS;
            context.getLevel().setBlock(context.getClickedPos(), state.setValue(ENCASED, false), 3);
            context.getLevel().updateNeighborsAt(context.getClickedPos(), this);
            return InteractionResult.SUCCESS;
        }
        return super.onSneakWrenched(state, context);
    }

    public boolean useCenteredIncidence() {
        return true;
    }

    public boolean canReceive(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        if (hasShaftTowards(level, pos, state, prop.direction()))
            return false;
        return level.getBlockEntity(pos, this.getBlockEntityType()).isPresent();
    }

    @Override
    public @Nullable BeamProperties transformProperties(Level level, BlockState state, BlockPos pos,
            BeamProperties prop,
            GenericOpticalSourceBlockEntity source, int range) {
        if (!this.canReceive(level, state, pos, prop))
            return null;
        Direction[] direction = { null };
        level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(a -> {
            if (a.getState() == null)
                return;

            direction[0] = COUtils
                    .getDirectionByNormal(prop.direction().getNormal().cross(state.getValue(FACING).getNormal()));
            if (a.getState().isParallel()) {
                direction[0] = direction[0].getOpposite();
            }
            if (state.getValue(FACING).getAxis().isVertical() && prop.direction().getAxis().equals(Axis.Z)) {
                direction[0] = direction[0].getOpposite();
            }
            if (prop.direction().getAxis().isVertical()) {

                direction[0] = direction[0].getOpposite();
            }
        });
        if (direction[0] == null)
            return null;
        return new BeamProperties.Builder(prop).direction(direction[0]).build();
    }

    public boolean shouldTick(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        return false;
    };

    public void receiveTick(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
    }

    @Override
    public void onReceiveBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {

    }

    @Override
    public void onRemoveBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {

    }

    @Override
    public void onUpdateBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
    }

    @Override
    public AABB getNonVisibleAABB(Level level, BlockState state, BlockPos pos) {
        return CENTERED_OFFSET.inflate(0.25d);
    }
}
