package net.lpcamors.optical.blocks.optical_receptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;

import net.createmod.catnip.lang.Lang;
import net.lpcamors.optical.CreateOptical;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.data.COLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;

public class OpticalReceptorBlockEntity extends GeneratingKineticBlockEntity {

    public final ReceptorType receptorType;
    private Map<Direction, BeamProperties> beamSourceInstanceMap = new HashMap<>();
    private BeamHelper.BeamProperties initialBeamProperties = null;
    public HashMap<Direction, ItemStack> sensors = emptyMap();

    public static HashMap<Direction, ItemStack> emptyMap() {
        HashMap<Direction, ItemStack> map = new HashMap<>();
        for (Direction dir : Direction.values())
            map.put(dir, ItemStack.EMPTY);
        return map;
    }

    public static OpticalReceptorBlockEntity speed(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new OpticalReceptorBlockEntity(type, pos, state, ReceptorType.SPEED);
    }

    public static OpticalReceptorBlockEntity capacity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new OpticalReceptorBlockEntity(type, pos, state, ReceptorType.CAPACITY);
    }

    public OpticalReceptorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            ReceptorType receptorType) {
        super(type, pos, state);
        this.receptorType = receptorType;

    }

    public void update() {
        if (this.level.isClientSide)
            return;
        this.updateBeamProperties();
        this.setChanged();
        this.sendData();
        updateGeneratedRotation();

    }

    public void updateBeamProperties() {
        ArrayList<BeamProperties> props = new ArrayList<>();
        this.beamSourceInstanceMap.forEach((direction, prop) -> {
            if (!this.sensors.get(direction).isEmpty()) {
                props.add(this.beamSourceInstanceMap.get(direction));
            }
        });
        if (!props.isEmpty()) {
            this.initialBeamProperties = new BeamProperties.Builder(
                    this.getBlockState().getValue(OpticalReceptorBlock.FACING),
                    props).build();
        } else {
            this.initialBeamProperties = null;
        }
    }

    public void receiveBeam(BeamHelper.BeamProperties beamProperties, boolean force) {
        if (!this.beamSourceInstanceMap.containsKey(beamProperties.direction().getOpposite()) || force) {
            this.beamSourceInstanceMap.put(beamProperties.direction().getOpposite(), beamProperties);
            this.update();
        }
    }

    public void removeBeam(BeamHelper.BeamProperties beamProperties) {
        this.beamSourceInstanceMap.remove(beamProperties.direction().getOpposite());
        this.update();
    }

    public OpticalReceptorBlock.OpticalReceptorGearHeaviness getGearHeaviness() {
        return ((OpticalReceptorBlock) this.getBlockState().getBlock()).heaviness;
    }

    @Override
    public float getGeneratedSpeed() {
        Float f = this.receptorType.getSpeed(this);
        return f == null ? super.getGeneratedSpeed() : f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        Float f = this.receptorType.getCapacity(this);
        return f == null ? super.calculateAddedStressCapacity() : f;
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider prov, boolean clientPacket) {
        super.read(compound, prov, clientPacket);
        if (compound.contains("IBeamSourceMap")) {
            ListTag listTag = (ListTag) compound.get("IBeamSourceMap");
            if (listTag != null) {
                for (int i = 0; i < listTag.size(); i++) {
                    Optional<BeamProperties> optional = BeamProperties.read((CompoundTag) listTag.get(i));
                    if (optional.isPresent()) {
                        this.beamSourceInstanceMap.put(Direction.values()[i], optional.get());
                    }
                }
            }
        }
        this.sensors = emptyMap();
        if (compound.contains("SensorMap")) {
            ListTag list = (ListTag) compound.get("SensorMap");
            Arrays.stream(Direction.values()).forEach(direction -> {
                ItemStack stack = direction.ordinal() < list.size()
                        ? ItemStack.parseOptional(prov, (CompoundTag) list.get(direction.ordinal()))
                        : ItemStack.EMPTY;
                this.sensors.put(direction, stack);
            });
        }
        this.updateBeamProperties();
        this.setChanged();
        this.sendData();
        updateGeneratedRotation();

        if (hasLevel())
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider prov, boolean clientPacket) {

        super.write(compound, prov, clientPacket);
        ListTag listTag1 = new ListTag();
        Arrays.stream(Direction.values()).forEach(direction -> {
            CompoundTag tag = new CompoundTag();
            if (this.beamSourceInstanceMap.containsKey(direction)) {
                if (this.beamSourceInstanceMap.get(direction) != null) {
                    this.beamSourceInstanceMap.get(direction).write(tag);
                }
            }
            listTag1.add(tag);
        });
        compound.put("IBeamSourceMap", listTag1);
        writeSensors(compound, prov, this.sensors);

    }

    public static void writeSensors(CompoundTag compound, HolderLookup.Provider prov, Map<Direction, ItemStack> map) {

        ListTag list = new ListTag(Direction.values().length);
        Arrays.stream(Direction.values()).forEachOrdered(direction -> {
            list.add(direction.ordinal(), map.get(direction).saveOptional(prov));
        });
        compound.put("SensorMap", list);
    }

    public enum ReceptorType implements IExtensibleEnum {
        SPEED("speed",
                be -> {
                    return be.initialBeamProperties != null ? be.initialBeamProperties.getEffectiveSpeed()
                            : 0F;
                },
                be -> 8f),

        CAPACITY("capacity",
                be -> {
                    return be.initialBeamProperties != null ? 32F : 0F;
                },
                be -> {
                    if (be.initialBeamProperties != null) {
                        return Math.abs(be.initialBeamProperties.getEffectiveSpeed()) * 8f / 32f;
                    }
                    return 0F;
                }),
                ;

        private final String nameId;
        private final Function<OpticalReceptorBlockEntity, Float> speed;
        private final Function<OpticalReceptorBlockEntity, Float> capacity;

        ReceptorType(String nameId, Function<OpticalReceptorBlockEntity, Float> speed,
                Function<OpticalReceptorBlockEntity, Float> capacity) {
            this.nameId = nameId;
            this.speed = speed;
            this.capacity = capacity;
        }

        public @Nullable Float getSpeed(OpticalReceptorBlockEntity be) {
            return this.speed.apply(be);
        }

        public @Nullable Float getCapacity(OpticalReceptorBlockEntity be) {
            return this.capacity.apply(be);
        }

        public String getNameId() {
            return nameId;
        }

        public Function<OpticalReceptorBlockEntity, Float> getSpeed() {
            return speed;
        }

        public Function<OpticalReceptorBlockEntity, Float> getCapacity() {
            return capacity;
        }

        public static ReceptorType create(String name, String nameId, Function<OpticalReceptorBlockEntity, Float> speed,
                Function<OpticalReceptorBlockEntity, Float> capacity) {
            throw new IllegalStateException("Enum not extended");
        }
    }

    public List<Direction> getForbiddenSensorDirection() {
        List<Direction> dirs = new ArrayList<>();
        for (Direction values : Direction.values()) {
            if (((OpticalReceptorBlock) this.getBlockState().getBlock()).hasShaftTowards(this.level, this.getBlockPos(),
                    this.getBlockState(), values))
                dirs.add(values);

        }
        return dirs;
    }

    public boolean addSensor(@Nonnull ItemStack itemStack, @Nonnull Direction direction) {
        if (this.isVirtual())
            return false;
        if (!this.sensors.get(direction).isEmpty())
            return false;
        if (this.getForbiddenSensorDirection().contains(direction))
            return false;
        this.sensors.put(direction, itemStack);
        this.update();
        return true;
    }

    public boolean removeSensor(@Nonnull Direction direction, Optional<Player> player) {
        if (this.isVirtual())
            return false;
        if (this.sensors.get(direction).isEmpty())
            return false;
        ItemStack stack = this.sensors.get(direction).copy();
        player.ifPresent(p -> {
            if (!p.addItem(stack))
                p.drop(stack, false);
        });
        this.sensors.put(direction, ItemStack.EMPTY);
        this.update();
        return true;

    }

    public void updateSensorsMap() {
        List<Direction> dirs = this.getForbiddenSensorDirection();
        for (Direction direction : dirs) {
            ItemStack stack = this.sensors.get(direction);
            if (!stack.isEmpty()) {
                for (Direction freeDir : Direction.values()) {
                    if (this.sensors.get(freeDir).isEmpty() && !dirs.contains(freeDir)) {
                        this.sensors.put(freeDir, stack);
                        this.sensors.put(direction, ItemStack.EMPTY);
                        break;
                    }
                }
                if (!this.sensors.get(direction).isEmpty()) {
                    Block.popResource(this.level, this.getBlockPos(), stack);// drop
                }
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.builder("tooltip").translate(CreateOptical.ID + ".gui.goggles.receptor_properties").forGoggles(tooltip);

        Lang.builder("")
                .add(COLang.Prefixes.CREATE.translate(("gui.goggles.sensor_count")).withStyle(ChatFormatting.GRAY))
                .forGoggles(tooltip);
        int i = 6 - this.sensors.values().stream().filter(ItemStack::isEmpty).toList().size();
        MutableComponent component = Component.empty();
        for (int j = 0; j < 4; j++) {
            if (j < i) {
                component.append(Component.literal("■ ").withStyle(ChatFormatting.GREEN));
            } else {
                component.append(Component.literal("□ ").withStyle(ChatFormatting.GRAY));
            }
        }
        if (i == 0) {
            component.append(
                    COLang.Prefixes.CREATE.translate("gui.goggles.sensor_count.empty").withStyle(ChatFormatting.BLACK));
        } else if (i == 4) {
            component.append(
                    COLang.Prefixes.CREATE.translate("gui.goggles.sensor_count.full").withStyle(ChatFormatting.BLACK));
        }
        Lang.builder("")
                .add(component)
                .forGoggles(tooltip, 1);

        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return true;
    }

}
