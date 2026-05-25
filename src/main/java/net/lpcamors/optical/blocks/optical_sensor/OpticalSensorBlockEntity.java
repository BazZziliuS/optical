package net.lpcamors.optical.blocks.optical_sensor;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllTags;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.lang.Lang;
import net.lpcamors.optical.CreateOptical;
import net.lpcamors.optical.blocks.COBlocks;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.data.COLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class OpticalSensorBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private Optional<BeamProperties> optionalBeamProperties = Optional.empty();

    public OpticalSensorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public void update() {
        if (this.level.isClientSide)
            return;
        this.setChanged();
        this.level.setBlock(this.getBlockPos(),
                this.getBlockState().setValue(BlockStateProperties.LIT, this.getSignal() > 0), 3);
        this.updateNeighbours(this.getBlockState(), this.level);
        this.sendData();
    }

    public void receiveBeam(BeamHelper.BeamProperties beamProperties, boolean onUpdate) {
        if (optionalBeamProperties.isEmpty() || onUpdate) {
            this.optionalBeamProperties = Optional.of(beamProperties);
            this.update();
        }
    }

    public ItemInteractionResult tryChangeMaterial(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        BlockState material = blockItem.getBlock()
                .defaultBlockState();
        if (!material.is(AllTags.AllBlockTags.CASING.tag))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        String name = blockItem.getDescriptionId();
        name = name.replace("block.create.", "").replace("_casing", "");
        int index = OpticalSensorBlock.CASINGS.indexOf(name);
        if (index == -1 || index >= OpticalSensorBlock.CASINGS.size())
            index = 0;

        if (index == this.getBlockState().getValue(OpticalSensorBlock.CASING))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level.isClientSide() && !isVirtual())
            return ItemInteractionResult.SUCCESS;
        this.level.setBlock(this.getBlockPos(),
                this.getBlockState().setValue(OpticalSensorBlock.CASING, index), 1);
        notifyUpdate();

        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, worldPosition, Block.getId(material));
        return ItemInteractionResult.SUCCESS;

    }

    public int getCasingIndex(BlockState material) {
        return 0;
    }

    public void removeBeam() {
        this.optionalBeamProperties = Optional.empty();
        this.update();
    }

    public Optional<BeamHelper.BeamProperties> getOptionalBeamProperties() {
        return this.optionalBeamProperties;
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider prov, boolean clientPacket) {
        super.read(compound, prov, clientPacket);
        this.optionalBeamProperties = BeamProperties.read(compound);
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider prov, boolean clientPacket) {
        super.write(compound, prov, clientPacket);
        this.optionalBeamProperties.ifPresent(prop -> prop.write(compound));
    }

    public int getSignal() {
        return this.getBlockState().getValue(OpticalSensorBlock.MODE)
                .apply(this.optionalBeamProperties);
    }

    private void updateNeighbours(BlockState p_54681_, Level p_54682_) {
        p_54682_.updateNeighborsAt(this.getBlockPos(), COBlocks.OPTICAL_SENSOR.get());
        p_54682_.updateNeighborsAt(this.getBlockPos()
                .relative(p_54681_.getValue(OpticalSensorBlock.HANGING) ? net.minecraft.core.Direction.UP
                        : net.minecraft.core.Direction.DOWN),
                COBlocks.OPTICAL_SENSOR.get());
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.builder("tooltip").translate(CreateOptical.ID + ".gui.goggles.optical_sensor").forGoggles(tooltip);

        Lang.builder("").add(
                COLang.Prefixes.CREATE.translate(("gui.goggles.optical_sensor.mode")).withStyle(ChatFormatting.GRAY))
                .forGoggles(tooltip);
        Lang.builder("")
                .add(COLang.Prefixes.CREATE
                        .translate(this.getBlockState().getValue(OpticalSensorBlock.MODE).getDescriptionId())
                        .withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);

        return true;
    }

    public void setOptionalBeamProperties(Optional<BeamProperties> optionalBeamProperties) {
        this.optionalBeamProperties = optionalBeamProperties;
    }

}
