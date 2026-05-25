package net.lpcamors.optical.blocks.beam_focuser;

import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.VecHelper;
import net.lpcamors.optical.CreateOptical;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.data.COLang;
import net.lpcamors.optical.recipes.FocusingRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class BeamFocuserBlockEntity extends KineticBlockEntity {

    public static final int PROCESSING_TICK = 40;

    public FilteringBehaviour filtering;
    protected BeltProcessingBehaviour beltProcessing;
    protected BlockFocusingBehaviour customProcess;
    public int processingTicks;
    public int baseProcessingDuration = PROCESSING_TICK;

    private Optional<BeamProperties> optionalBeamProperties = Optional.empty();

    public BeamFocuserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        processingTicks = -1;
    }

    public void receiveBeam(BeamHelper.BeamProperties beamProperties, boolean onUpdate) {
        if (optionalBeamProperties.isEmpty() || onUpdate) {
            this.optionalBeamProperties = Optional.of(beamProperties);
            this.update();
        }
    }

    public void removeBeam() {
        if (!optionalBeamProperties.isEmpty()) {
            this.optionalBeamProperties = Optional.empty();
            this.update();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.speed == 0 || this.optionalBeamProperties.isEmpty()) {
            processingTicks = -1;
        }
        if (processingTicks >= 0) {
            processingTicks--;
            if (processingTicks <= 8 && this.level.isClientSide) {
                this.spawnProcessingParticles();
            }
        }

    }

    public double getAngle(float partialTicks, double radius, double k, double alpha) {
        int cycles = (int) Math.ceil(Math.abs(this.getSpeed()) / 64D);
        double t = ((cycles + 1) * 2 * Math.PI) * (-partialTicks + this.processingTicks - this.getProcessDuration())
                / (k * (5 - this.getProcessDuration()));
        double angle = radius * Math.tan(alpha * Math.cos(k * t)) / Math.tan(alpha);
        return angle;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

        filtering = new FilteringBehaviour(this, new FocuserFilterSlot()).forRecipes();
        behaviours.add(filtering);

        beltProcessing = new BeltProcessingBehaviour(this).whenItemEnters(this::onItemReceived)
                .whileItemHeld(this::whenItemHeld);
        behaviours.add(beltProcessing);

        registerAwardables(behaviours, AllAdvancements.SPOUT, AllAdvancements.FOODS);
    }

    protected void spawnProcessingParticles() {
        if (this.optionalBeamProperties.isPresent()) {
            Vec3 vec = VecHelper.getCenterOf(this.worldPosition);
            vec = vec.subtract(0.0, 0.5, 0.0);
            ParticleUtils.spawnParticleOnFace(level, this.getBlockPos().relative(Axis.Y, -2), Direction.UP,
                    ParticleTypes.WHITE_SMOKE, new Vec3(0.1, 0.1, 0.1), 0.5);
        }
    }

    public void update() {
        this.setChanged();
        this.sendData();

    }

    public Optional<BeamProperties> getOptionalBeamProperties() {
        return optionalBeamProperties;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().expandTowards(0, -2, 0);
    }

    public int getProcessDuration() {
        return (int) (this.baseProcessingDuration * this.getSpeedDurationMultiplier());
    }

    public float getSpeedDurationMultiplier() {
        return (288 - Math.abs(this.getSpeed())) / 256F;
    }

    public boolean canFocus() {
        return this.speed != 0 && this.optionalBeamProperties.isPresent();
    }

    protected BeltProcessingBehaviour.ProcessingResult whenItemHeld(TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler) {
        if (this.processingTicks != -1 && this.processingTicks != 5) {
            return ProcessingResult.HOLD;
        } else if (!this.canFocus()) {
            return ProcessingResult.PASS;
        } else if (!BeamFocuserHelper.canBeProcessed(this.level, this.getWrapperFor(transported.stack),
                this.optionalBeamProperties.get().getType())) {
            return ProcessingResult.PASS;
        } else {
            BeamHelper.BeamType beamType = this.optionalBeamProperties.get().beamType();
            Optional<RecipeHolder<FocusingRecipe>> recipe = BeamFocuserHelper.getFocusingRecipe(level,
                    this.getWrapperFor(transported.stack), beamType);
            if (recipe.isEmpty())
                return PASS;

            if (processingTicks == -1) {
                this.baseProcessingDuration = recipe.get().value().getProcessingDuration();
                processingTicks = this.getProcessDuration() + 5;
                notifyUpdate();
                return HOLD;
            }

            // Process finished

            List<ItemStack> results = recipe.get().value().rollResults(level.random);
            transported.stack.shrink(1);
            ItemStack out = results.isEmpty() ? ItemStack.EMPTY : results.get(0);
            if (!out.isEmpty()) {
                List<TransportedItemStack> outList = new ArrayList<>();
                TransportedItemStack held = null;
                TransportedItemStack result = transported.copy();
                result.stack = out;
                if (!transported.stack.isEmpty())
                    held = transported.copy();
                outList.add(result);
                handler.handleProcessingOnItem(transported,
                        TransportedItemStackHandlerBehaviour.TransportedResult.convertToAndLeaveHeld(outList, held));
            }
            notifyUpdate();
            return HOLD;
        }
    }

    private RecipeWrapper getWrapperFor(ItemStack stack) {
        ItemStackHandler inv = new ItemStackHandler(2);
        inv.setStackInSlot(0, stack);
        inv.setStackInSlot(1, this.filtering.getFilter());
        return new RecipeWrapper(inv);
    }

    protected BeltProcessingBehaviour.ProcessingResult onItemReceived(TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler) {
        if (!this.canFocus()) {
            return ProcessingResult.PASS;
        } else if (!BeamFocuserHelper.canBeProcessed(level, this.getWrapperFor(transported.stack),
                this.optionalBeamProperties.get().beamType())) {
            return ProcessingResult.PASS;
        }
        return ProcessingResult.HOLD;
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider prov, boolean clientPacket) {
        super.read(compound, prov, clientPacket);
        this.optionalBeamProperties = BeamProperties.read(compound);
        this.processingTicks = compound.getInt("ProcessingTicks");
        this.baseProcessingDuration = compound.getInt("ProcessingDuration");
        if (!clientPacket)
            return;
        if (hasLevel())
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);

    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider prov, boolean clientPacket) {
        super.write(compound, prov, clientPacket);
        this.optionalBeamProperties.ifPresent(prop -> prop.write(compound));
        compound.putInt("ProcessingTicks", processingTicks);
        compound.putInt("ProcessingDuration", baseProcessingDuration);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        if (this.optionalBeamProperties.isPresent()) {
            Lang.builder("tooltip").translate(CreateOptical.ID + ".gui.goggles.beam_properties").forGoggles(tooltip);
            Lang.builder("")
                    .add(COLang.Prefixes.CREATE.translate(("gui.goggles.beam_type")).withStyle(ChatFormatting.GRAY))
                    .forGoggles(tooltip);
            Lang.builder("")
                    .add(COLang.Prefixes.CREATE
                            .translate(
                                    this.optionalBeamProperties.get().beamType().getDescriptionId())
                            .withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);
        }

        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);

    }

    public static class FocuserFilterSlot extends ValueBoxTransform.Sided {

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(BeamFocuserBlock.HORIZONTAL_FACING).getClockWise().getAxis() == direction.getAxis();
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8f, 12.5f);
        }

    }
}
