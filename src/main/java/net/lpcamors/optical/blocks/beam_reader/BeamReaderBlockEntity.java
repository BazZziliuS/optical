package net.lpcamors.optical.blocks.beam_reader;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;

import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.VecHelper;
import net.lpcamors.optical.CreateOptical;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.data.COLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BeamReaderBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private Optional<BeamProperties> optionalBeamProperties = Optional.empty();

    public ScrollValueBehaviour frequency;

    public BeamReaderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void removeBeam() {
        this.optionalBeamProperties = Optional.empty();
        this.update();
    }

    public void receiveBeam(BeamProperties prop, boolean force) {
        if (this.optionalBeamProperties.isEmpty() || force) {
            this.optionalBeamProperties = Optional.of(prop);
        }
        this.update();
    }

    public Optional<BeamHelper.BeamProperties> getOptionalBeamProperties() {
        return this.optionalBeamProperties;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        this.frequency = new ScrollValueBehaviour(Lang.builder("tooltip")
                .translate(CreateOptical.ID + ".gui.behaviour.beam_reader_frequency").component(), this,
                new ReaderFrequencyValueBoxTransform().fromSide(Direction.UP)).between(0, 200).requiresWrench();
        behaviours.add(this.frequency);
    }

    public void update() {
        if (this.level.isClientSide)
            return;
        this.setChanged();
        this.sendData();
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

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.builder("tooltip").translate(CreateOptical.ID + ".gui.goggles.beam_reader").forGoggles(tooltip);

        if (this.optionalBeamProperties.isPresent()) {
            BeamProperties prop = this.optionalBeamProperties.get();
            Lang.builder("")
                    .add(COLang.Prefixes.CREATE.translate(("gui.goggles.beam_type")).withStyle(ChatFormatting.GRAY))
                    .forGoggles(tooltip, 1);
            Lang.builder("")
                    .add(COLang.Prefixes.CREATE.translate(prop.getType().getDescriptionId())
                            .withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);
            Lang.builder("")
                    .add(COLang.Prefixes.CREATE.translate(("gui.goggles.intensity")).withStyle(ChatFormatting.GRAY))
                    .forGoggles(tooltip);
            Lang.builder("")
                    .add(COLang.Prefixes.CREATE.translate("gui.goggles.intensity_value", prop.intensity())
                            .withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);
            if (prop.beamType().canPassThroughEntities()) {
                Lang.builder("")
                        .add(COLang.Prefixes.CREATE.translate(("gui.goggles.beam_damage"))
                                .withStyle(ChatFormatting.GRAY))
                        .forGoggles(tooltip, 1);
                Lang.builder("")
                        .add(COLang.Prefixes.CREATE.translate("gui.goggles.beam_damage_value", (int) prop.getDamage())
                                .withStyle(ChatFormatting.RED))
                        .forGoggles(tooltip, 1);

            }
            Lang.builder("")
                    .add(COLang.Prefixes.CREATE.translate(("gui.goggles.color")).withStyle(ChatFormatting.GRAY))
                    .forGoggles(tooltip, 1);
            Lang.builder("")
                    .add(COLang.Prefixes.CREATE.translate("gui.goggles.color_value", prop.color().getX(),
                            prop.color().getY(), prop.color().getZ()).withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);

        } else {

            Lang.builder("")
                    .add(COLang.Prefixes.CREATE.translate(("gui.goggles.no_beam")).withStyle(ChatFormatting.GRAY))
                    .forGoggles(tooltip,1 );
        }
        return true;
    }

    public void setOptionalBeamProperties(Optional<BeamProperties> optionalBeamProperties) {
        this.optionalBeamProperties = optionalBeamProperties;
    }

    private static class ReaderFrequencyValueBoxTransform extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8f, 10.5f);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction == Direction.UP;
        }

        @Override
        public float getScale() {
            return 0.5f;
        }

    }

}
