package net.lpcamors.optical.blocks.beam_condenser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BeamCondenserBlockEntity extends GenericOpticalSourceBlockEntity {

    protected BeamProperties beamProperties = null;

    protected final Map<Direction, Optional<BeamProperties>> beams = Arrays.stream(Direction.values())
            .collect(Collectors.toMap(d -> d, d -> Optional.ofNullable((BeamProperties) null)));

    public BeamCondenserBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public void removeBeam(BeamProperties prop) {
        if (beams.get(prop.direction()).isPresent()) {
            beams.put(prop.direction(), Optional.empty());
        }
        this.update();
    }

    public void receiveBeam(BeamProperties prop, boolean force) {
        if (beams.get(prop.direction()).isEmpty() || force) {
            beams.put(prop.direction(), Optional.of(prop));
        }
        this.update();
    }

    public void update() {
        if (this.level.isClientSide)
            return;
        this.updateBeamProperties();
        this.setChanged();
        this.sendData();
    }

    public void updateBeamProperties() {
        Direction blockFacing = this.getBlockState().getValue(BeamCondenserBlock.FACING);

        ArrayList<BeamProperties> prop = new ArrayList<>();
        this.beams.forEach((direction, opt) -> {
            if (opt.isPresent()) {
                if (direction.getAxis().isHorizontal()
                        && !direction.equals(blockFacing.getOpposite())) {
                    prop.add(opt.get());
                }
            }
        });

        if (!prop.isEmpty()) {
            this.beamProperties = new BeamProperties.Builder(blockFacing, prop).isDirty(true).build();
        } else {
            this.beamProperties = null;
        }
    }

    @Override
    public @Nullable BeamHelper.BeamProperties getInitialBeamProperties() {
        return this.beamProperties;
    }

    @Override
    public boolean isActive() {
        return this.beamProperties != null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return AABB.INFINITE;
    }

    @Override
    protected void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        ListTag tag = new ListTag(Direction.values().length);
        for (Direction values : Direction.values()) {
            this.beams.get(values).ifPresentOrElse(
                    a -> {
                        CompoundTag compoundTag = new CompoundTag();
                        a.write(compoundTag);
                        tag.add(values.ordinal(), compoundTag);
                    }, () -> {

                        tag.add(values.ordinal(), new CompoundTag());
                    });
        }
        compound.put("Beams", tag);
        if (this.beamProperties != null) {
            this.beamProperties.write(compound);
        }
    }

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("Beams")) {
            ListTag list = compound.getList("Beams", 0);
            for (Direction direction : Direction.values()) {
                if (direction.ordinal() < list.size()) {
                    this.beams.put(direction, BeamProperties.read((CompoundTag) list.get(direction.ordinal())));
                } else {
                    this.beams.put(direction, Optional.empty());
                }
            }
        }
        BeamProperties.read(compound).ifPresent(a -> this.beamProperties = a);
    }

}
