package net.lpcamors.optical.blocks.hologram_source;

import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.placement.PoleHelper;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import org.jetbrains.annotations.NotNull;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.createmod.catnip.platform.CatnipServices;
import net.lpcamors.optical.COShapes;
import net.lpcamors.optical.blocks.COBlockEntities;
import net.lpcamors.optical.blocks.COBlocks;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity.IBeamActivator;
import net.lpcamors.optical.gui.HologramSourceScreen;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class HologramSourceBlock extends Block
        implements IBeamActivator, IWrenchable, IBE<HologramSourceBlockEntity> {

    public static final MapCodec<HologramSourceBlock> CODEC = simpleCodec(HologramSourceBlock::new);

    public static final Property<Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty RIGHT = BooleanProperty.create("positive_connection");
    public static final BooleanProperty LEFT = BooleanProperty.create("negative_connection");

    public HologramSourceBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(RIGHT, false)
                .setValue(LEFT, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(RIGHT, LEFT, HORIZONTAL_AXIS));
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState p_60555_, @NotNull BlockGetter p_60556_, @NotNull BlockPos p_60557_,
            @NotNull CollisionContext p_60558_) {
        return COShapes.HOLOGRAM_SOURCE.get(p_60555_.getValue(HORIZONTAL_AXIS));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateLine(super.getStateForPlacement(context).setValue(HORIZONTAL_AXIS,
                context.getHorizontalDirection().getAxis()), context.getClickedPos(),
                context.getLevel());
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
        if (pOldState.getBlock() == this)
            return;
        LevelTickAccess<Block> blockTicks = pLevel.getBlockTicks();
        if (!blockTicks.hasScheduledTick(pPos, this)) {
            pLevel.scheduleTick(pPos, this, 1);
        }

        BlockPos adjacentPos = pPos.relative(getConnectionAxis(pState), 1);
        BlockState state = pLevel.getBlockState(adjacentPos);
        if (state.getBlock() instanceof HologramSourceBlock b) {
            b.update(state, pLevel, adjacentPos);
        }

        adjacentPos = pPos.relative(getConnectionAxis(pState), -1);
        state = pLevel.getBlockState(adjacentPos);
        if (state.getBlock() instanceof HologramSourceBlock b) {
            b.update(state, pLevel, adjacentPos);
        }
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState newState,
            boolean movedByPiston) {
        super.onRemove(pState, pLevel, pPos, newState, movedByPiston);
        LevelTickAccess<Block> blockTicks = pLevel.getBlockTicks();
        if (!blockTicks.hasScheduledTick(pPos, this)) {
            pLevel.scheduleTick(pPos, this, 1);
        }

        BlockPos adjacentPos = pPos.relative(getConnectionAxis(pState), 1);
        BlockState state = pLevel.getBlockState(adjacentPos);
        if (state.getBlock() instanceof HologramSourceBlock b) {
            b.update(state, pLevel, adjacentPos);
        }

        adjacentPos = pPos.relative(getConnectionAxis(pState), -1);
        state = pLevel.getBlockState(adjacentPos);
        if (state.getBlock() instanceof HologramSourceBlock b) {
            b.update(state, pLevel, adjacentPos);
        }
    }

    protected boolean canConnect(BlockState state, BlockState other) {
        return other.getBlock() == this && state.getValue(HORIZONTAL_AXIS).equals(other.getValue(HORIZONTAL_AXIS));
    }

    public static Axis getConnectionAxis(BlockState state) {
        return Direction.fromAxisAndDirection(state.getValue(HORIZONTAL_AXIS), AxisDirection.POSITIVE).getClockWise()
                .getAxis();
    }

    public static BlockState setConnection(BlockState state, boolean connectRight, boolean connectLeft) {
        return state.setValue(RIGHT, connectRight).setValue(LEFT, connectLeft);
    }

    public static boolean getConnection(BlockState state, boolean left) {
        if (!(state.getBlock() instanceof HologramSourceBlock))
            return false;
        return state.getValue(left ? LEFT : RIGHT);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource pRandom) {
        super.tick(state, level, pos, pRandom);
        if (state.getBlock() != this)
            return;
        update(state, level, pos);
        level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(be -> {
            be.findController().updateChain();
        });

    }

    private void update(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide())
            return;
        boolean f1 = getConnection(state, false) != canConnect(state,
                level.getBlockState(pos.relative(getConnectionAxis(state), 1)));
        boolean f2 = getConnection(state, true) != canConnect(state,
                level.getBlockState(pos.relative(getConnectionAxis(state), -1)));
        if (f1 || f2) {

            KineticBlockEntity.switchToBlockState(level, pos, updateLine(state, pos,
                    level));
        }
    }

    private BlockState updateLine(BlockState state, BlockPos pos, Level level) {
        Axis axis = getConnectionAxis(state);

        if (!level.isLoaded(pos))
            return state;
        Direction direction = axis.equals(Axis.Z) ? Direction.SOUTH : Direction.EAST;
        BlockState stateL = level.getBlockState(pos.relative(direction));
        BlockState stateR = level.getBlockState(pos.relative(direction.getOpposite()));

        boolean canConnectRight = canConnect(state, stateR),
                canConnectLeft = canConnect(state, stateL);

        state = setConnection(state, canConnectRight, canConnectLeft);

        return state;
    }

    @Override
    public Class<HologramSourceBlockEntity> getBlockEntityClass() {
        return HologramSourceBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HologramSourceBlockEntity> getBlockEntityType() {
        return COBlockEntities.HOLOGRAM_SOURCE.get();
    }

    @Override
    public BeamProperties transformProperties(Level level, BlockState state, BlockPos pos, BeamProperties prop,
            GenericOpticalSourceBlockEntity source, int range) {
        if (prop.direction().getAxis() == getConnectionAxis(state)) {
            return prop;
        }
        return null;
    }

    @Override
    public boolean canReceive(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        return prop.direction().getAxis() == getConnectionAxis(state);
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
        return null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand interactionHand, BlockHitResult hitResult) {
        if (!player.mayBuild())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        boolean f = player.isShiftKeyDown();
        IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
        if (helper.matchesItem(stack) && !f)
            return helper.getOffset(player, level, state, pos, hitResult)
                    .placeInWorld(level, (BlockItem) stack.getItem(), player, interactionHand, hitResult);

        if (!f && stack.isEmpty()) {
            if (player.level().isClientSide) {
                level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(be -> {
                    CompoundTag tag = new CompoundTag();
                    be.write(tag, level.registryAccess(), true);
                });
                CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> {
                    this.displayScreen(player, level, pos);
                });

                return ItemInteractionResult.SUCCESS;
            }
        }

        HologramSourceBlockEntity be = getBlockEntity(level, pos);
        if (be == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        be = be.getController();
        if (be == null || be.getProfile() == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!level.isClientSide) {
            if (!stack.isEmpty()) {
                if (!stack.is(AllItems.WRENCH)) {
                    be.getProfile().setItemStack(stack.copy());
                    be.update();
                }
            } else if (f) {
                be.getProfile().setItemStack(ItemStack.EMPTY);
                be.update();
            } 
        }

        return ItemInteractionResult.SUCCESS;
    }

    @OnlyIn(value = Dist.CLIENT)
    protected void displayScreen(Player player, Level level, BlockPos pos) {
        if (player instanceof LocalPlayer) {
            HologramSourceBlockEntity be = this.getBlockEntity(level, pos);
            if (be.getController() != null && be.getController().getProfile() != null)
                ScreenOpener.open(new HologramSourceScreen(be.getController()));
        }
    }

    public static <T extends Block> Function<BlockState, net.neoforged.neoforge.client.model.generators.ModelFile> getBlockModel(
            DataGenContext<Block, T> c, RegistrateBlockstateProvider p) {
        return state -> AssetLookup.partialBaseModel(c, p, getNameForState(state));
    }

    private static String getNameForState(BlockState state) {
        boolean f1 = state.getValue(RIGHT),
                f2 = state.getValue(LEFT);
        return f1 && f2 ? "connected" : f1 ? "connected_positive" : f2 ? "connected_negative" : "not_connected";
    }

    public static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    @MethodsReturnNonnullByDefault
    private static class PlacementHelper extends PoleHelper<Direction.Axis> {

        public PlacementHelper() {
            super(state -> state.getBlock() instanceof HologramSourceBlock,
                    state -> Direction.fromAxisAndDirection(state.getValue(HORIZONTAL_AXIS), AxisDirection.POSITIVE)
                            .getClockWise().getAxis(),
                    HORIZONTAL_AXIS);
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return COBlocks.HOLOGRAM_SOURCE::isIn;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return COBlocks.HOLOGRAM_SOURCE::has;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
                BlockHitResult ray) {
            PlacementOffset offset = super.getOffset(player, world, state, pos, ray);
            if (offset.isSuccessful())
                offset.withTransform(offset.getTransform());
            return offset;
        }
    }

}
