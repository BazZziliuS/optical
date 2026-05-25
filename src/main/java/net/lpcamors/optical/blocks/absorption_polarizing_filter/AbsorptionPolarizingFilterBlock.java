package net.lpcamors.optical.blocks.absorption_polarizing_filter;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import org.jetbrains.annotations.NotNull;

import net.lpcamors.optical.COShapes;
import net.lpcamors.optical.blocks.COBlockEntities;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class AbsorptionPolarizingFilterBlock extends HorizontalDirectionalBlock implements IWrenchable,
        GenericOpticalSourceBlockEntity.IBeamActivator, IBE<AbsorptionPolarizingFilterBlockEntity> {

    public static NonNullBiConsumer<DataGenContext<Block, AbsorptionPolarizingFilterBlock>, RegistrateBlockstateProvider> stateProvider() {
        return (a, b) -> {
            b.horizontalBlock(a.get(), getBlockModel(a, b));
        };

    }

    public static Function<BlockState, ModelFile> getBlockModel(
            DataGenContext<Block, AbsorptionPolarizingFilterBlock> c,
            RegistrateBlockstateProvider p) {
        return state -> {
            return AssetLookup.partialBaseModel(c, p, state.getValue(POLARIZATION).name().toLowerCase());
        };
    }

    public static final MapCodec<AbsorptionPolarizingFilterBlock> CODEC = simpleCodec(
            AbsorptionPolarizingFilterBlock::new);

    public static final EnumProperty<BeamHelper.BeamPolarization> POLARIZATION = EnumProperty.create("polarization",
            BeamHelper.BeamPolarization.class);

    public AbsorptionPolarizingFilterBlock(Properties p_54120_) {
        super(p_54120_);
        registerDefaultState(defaultBlockState().setValue(POLARIZATION, BeamHelper.BeamPolarization.HORIZONTAL));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {

        return CODEC;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState p_60555_, @NotNull BlockGetter p_60556_, @NotNull BlockPos p_60557_,
            @NotNull CollisionContext p_60558_) {
        return (COShapes.ABSORPTION_POLARIZING_FILTER).get(p_60555_.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_49915_) {
        super.createBlockStateDefinition(p_49915_.add(FACING, POLARIZATION));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection().getClockWise().getOpposite())
                .setValue(POLARIZATION, BeamHelper.BeamPolarization.VERTICAL);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getClickedFace().getAxis().isHorizontal()) {
            BlockState state1 = state.setValue(POLARIZATION, state.getValue(POLARIZATION).getNextRotated(1));
            context.getLevel().setBlock(context.getClickedPos(), state1, 3);
            return InteractionResult.SUCCESS;
        }
        return IWrenchable.super.onWrenched(state, context);
    }

    public boolean canReceive(Level level, BlockState state, BlockPos pos, BeamProperties prop) {
        return false;
    }

    @Override
    public @Nullable BeamProperties transformProperties(Level level, BlockState state, BlockPos pos,
            BeamProperties prop, GenericOpticalSourceBlockEntity source, int range) {
        if (!state.getValue(FACING).getClockWise().getAxis().equals(prop.direction().getAxis()))
            return null;
        Direction direction = prop.direction();
        BeamHelper.BeamPolarization beamPolarization = state.getValue(POLARIZATION);
        if (beamPolarization.isDiagonal()) {
            if (!direction.equals(state.getValue(FACING).getCounterClockWise())) {
                beamPolarization = beamPolarization.getNextRotated(2);
            }
        }
        float intensity = prop.polarization().getRemainingIntensity(prop.intensity(),
                beamPolarization);
        if (intensity > 0) {
            return new BeamHelper.BeamProperties.Builder(prop).polarization(beamPolarization).intensity(
                    intensity).build();
        }
        return null;
    };

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
        return CENTERED_OFFSET;
    }

    @Override
    public Class<AbsorptionPolarizingFilterBlockEntity> getBlockEntityClass() {
        return AbsorptionPolarizingFilterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AbsorptionPolarizingFilterBlockEntity> getBlockEntityType() {
        return COBlockEntities.ABSORPTION_POLARIZING_FILTER.get();
    }
}
