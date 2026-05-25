package net.lpcamors.optical.blocks.optical_sensor;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import org.jetbrains.annotations.NotNull;

import net.lpcamors.optical.COShapes;
import net.lpcamors.optical.COUtils;
import net.lpcamors.optical.blocks.COBlockEntities;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity.IBeamActivator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class OpticalSensorBlock extends Block implements IWrenchable, IBeamActivator, IBE<OpticalSensorBlockEntity> {
    public static final List<String> CASINGS = List.of(
            "andesite",
            "brass",
            "copper",
            "railway",
            "refined_radiance",
            "shadow_steel");
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final MapCodec<OpticalSensorBlock> CODEC = simpleCodec(OpticalSensorBlock::new);
    public static final EnumProperty<Mode> MODE = EnumProperty.create("sensor_mode", Mode.class);
    public static final IntegerProperty CASING = IntegerProperty.create("casing", 0, CASINGS.size() - 1);

    public OpticalSensorBlock(Properties p_54120_) {
        super(p_54120_);
    }

    public static NonNullBiConsumer<DataGenContext<Block, OpticalSensorBlock>, RegistrateBlockstateProvider> stateProvider() {
        return (a, b) -> {
            b.getVariantBuilder(a.get())
                    .forAllStates(state -> ConfiguredModel.builder()
                            .modelFile(getBlockModel(a, b).apply(state))
                            .rotationX((state.getValue(HANGING) ? 180 : 0))
                            .build());
            ;
        };
    }

    public static Function<BlockState, ModelFile> getBlockModel(DataGenContext<Block, OpticalSensorBlock> c,
            RegistrateBlockstateProvider p) {
        return state -> {
            return AssetLookup.partialBaseModel(c, p, CASINGS.get(state.getValue(CASING)));
        };
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public Class<OpticalSensorBlockEntity> getBlockEntityClass() {
        return OpticalSensorBlockEntity.class;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos,
            @NotNull CollisionContext context) {
        return state.getValue(HANGING) ? COShapes.SENSOR_DOWN : COShapes.SENSOR_UP;
    }

    @Override
    public BlockEntityType<? extends OpticalSensorBlockEntity> getBlockEntityType() {
        return COBlockEntities.OPTICAL_SENSOR.get();
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (!context.getPlayer().isShiftKeyDown()) {
            if (context.getLevel().isClientSide)
                return InteractionResult.SUCCESS;
            context.getLevel().setBlock(context.getClickedPos(), state.setValue(MODE, state.getValue(MODE).getNext()),
                    3);
            context.getLevel().updateNeighborsAt(context.getClickedPos(), this);
            return InteractionResult.SUCCESS;

        }
        return IWrenchable.super.onWrenched(state, context);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, @NotNull BlockState state, @NotNull Level level,
            @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
            BlockHitResult hitResult) {
        if (!player.isShiftKeyDown()) {
            if (!stack.isEmpty() && !stack.getItem().equals(AllItems.WRENCH.get())) {
                ItemInteractionResult[] result = { ItemInteractionResult.FAIL };
                level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(be -> {
                    result[0] = be.tryChangeMaterial(stack);
                });
                return result[0];
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public boolean canReceive(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        return !prop.direction().getAxis().isVertical()
                && level.getBlockEntity(pos, this.getBlockEntityType()).isPresent();
    }

    @Override
    public void onRemoveBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(OpticalSensorBlockEntity::removeBeam);
    }

    @Override
    public void onReceiveBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(be -> {
            be.receiveBeam(prop, false);
        });
    }

    @Override
    public void onUpdateBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        level.getBlockEntity(pos, this.getBlockEntityType()).ifPresent(be -> {
            be.receiveBeam(prop, true);
        });
    }

    @Override
    public BeamProperties transformProperties(Level level, BlockState state, BlockPos pos, BeamProperties prop,
            GenericOpticalSourceBlockEntity source, int range) {
        return prop;
    }

    @Override
    public AABB getNonVisibleAABB(Level level, BlockState state, BlockPos pos) {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> p_49915_) {
        super.createBlockStateDefinition(p_49915_);
        p_49915_.add(HANGING).add(MODE).add(BlockStateProperties.LIT).add(CASING);
    }

    public static int getLight(BlockState state) {
        return (state.getValue(BlockStateProperties.LIT) ? 1 : 0)
                * (state.getValue(MODE).equals(Mode.DIGITAL) ? 15 : 10);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        for (Direction direction : context.getNearestLookingDirections()) {
            BlockState blockstate = this.defaultBlockState().setValue(HANGING, direction.equals(Direction.UP));
            if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
                return blockstate.setValue(MODE, Mode.DIGITAL).setValue(BlockStateProperties.LIT, Boolean.FALSE)
                        .setValue(CASING, 0);
            }
        }
        return null;
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        Direction direction = getConnectedDirection(state).getOpposite();
        return Block.canSupportCenter(level, pos.relative(direction), direction.getOpposite());
    }

    protected static Direction getConnectedDirection(BlockState state) {
        return state.getValue(HANGING) ? Direction.DOWN : Direction.UP;
    }

    @Override
    public boolean isSignalSource(@NotNull BlockState p_60571_) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        boolean f1 = blockState.getValue(HANGING);
        if (f1) {
            if (side != Direction.UP)
                return 0;
        } else {
            if (side != Direction.DOWN)
                return 0;
        }
        return getSignal(blockState, blockAccess, pos, side);
    }

    @Override
    public int getSignal(@NotNull BlockState state, @NotNull BlockGetter blockGetter, @NotNull BlockPos pos,
            @NotNull Direction direction) {
        return getBlockEntityOptional(blockGetter, pos).map(OpticalSensorBlockEntity::getSignal)
                .orElse(0);
    }

    public enum Mode implements StringRepresentable {

        INTENSITY(
                new Vec3i(67, 144, 141),
                optionalBeamProperties -> optionalBeamProperties
                        .map(beamProperties -> (int) Math.min(16, beamProperties.intensity() * 16) - 1).orElse(0)),
        COLOR(
                new Vec3i(136, 162, 255),
                optionalBeamProperties -> optionalBeamProperties
                        .map(beamProperties -> 15
                                - (int) (COUtils.getPseudoLengthVec(Vec3.atLowerCornerOf(beamProperties.color()))
                                        * (5F / (3 * 255))))
                        .orElse(0)),
        DIGITAL(
                optionalBeamProperties -> optionalBeamProperties
                        .map(beamProperties -> beamProperties.color()).orElse(new Vec3i(255, 255, 255)),
                optionalBeamProperties -> optionalBeamProperties.isPresent() ? 15 : 0);

        private final Function<Optional<BeamHelper.BeamProperties>, Vec3i> color;
        private final Function<Optional<BeamHelper.BeamProperties>, Integer> function;

        Mode(Vec3i color, Function<Optional<BeamHelper.BeamProperties>, Integer> function) {
            this(optionalBeamProperties -> color, function);
        }

        Mode(Function<Optional<BeamHelper.BeamProperties>, Vec3i> color,
                Function<Optional<BeamHelper.BeamProperties>, Integer> function) {
            this.color = color;
            this.function = function;
        }

        public Integer apply(Optional<BeamHelper.BeamProperties> optionalBeamProperties) {
            return this.function.apply(optionalBeamProperties);
        }

        public Mode getNext() {
            return Mode.values()[this.ordinal() + 1 >= Mode.values().length ? 0 : this.ordinal() + 1];
        }

        public Vec3i getColor(Optional<BeamHelper.BeamProperties> optionalBeamProperties) {
            return color.apply(optionalBeamProperties);
        }

        public String getDescriptionId() {
            return "gui.goggles.optical_sensor.mode." + this.getSerializedName();
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

}
