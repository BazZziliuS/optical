package net.lpcamors.optical.blocks.hologram_source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.nbt.NBTHelper;
import net.lpcamors.optical.COUtils;
import net.lpcamors.optical.CreateOptical;
import net.lpcamors.optical.blocks.COBlockEntities;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.data.COLang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HologramSourceBlockEntity extends SmartBlockEntity
        implements IHaveGoggleInformation, IHaveHoveringInformation {

    private Optional<BeamProperties> optionalBeamProperties = Optional.empty();
    private @Nullable HologramSourceProfile profile;
    private BlockPos controllerPos;
    private int tickCount = 0;

    public HologramSourceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(10);
        this.controllerPos = pos;
        this.profile = null;
    }

    public int getTickCount() {
        return tickCount;
    }

    @Override
    public void tick() {
        this.tickCount++;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (this.isController() && !this.level.isClientSide) {
            this.updateChain();
        }
    }

    public void updateChain() {
        if (!this.shouldBeController()) {
            this.findController().updateChain();
            return;
        }

        Direction dir = Direction.fromAxisAndDirection(HologramSourceBlock.getConnectionAxis(this.getBlockState()),
                AxisDirection.POSITIVE);

        BlockPos pos = this.getBlockPos();
        HologramSourceBlockEntity be = this;
        Axis axis = HologramSourceBlock.getConnectionAxis(be.getBlockState());

        int length = 1;

        HologramSourceProfile lastProfile = null;
        while (true) {
            pos = pos.relative(dir);
            Optional<HologramSourceBlockEntity> opt = this.level.getBlockEntity(pos,
                    COBlockEntities.HOLOGRAM_SOURCE.get());
            if (!opt.isPresent())
                break;
            if (!axis.equals(HologramSourceBlock.getConnectionAxis(opt.get().getBlockState()))) {
                break;
            }

            if (opt.get().profile != null) {
                lastProfile = opt.get().profile;
            }
            length++;
            opt.get().controllerPos = this.getBlockPos();
            opt.get().profile = null;
            opt.get().update();
        }
        if (this.profile == null) {
            if (lastProfile == null) {
                this.profile = new HologramSourceProfile();
            } else {
                this.profile = lastProfile;
            }
        }
        this.profile.setConnectionLength(length);
        this.controllerPos = this.getBlockPos();
        this.update();
    }

    private boolean shouldBeController() {
        BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof HologramSourceBlock))
            return false;

        Axis axis = HologramSourceBlock.getConnectionAxis(this.getBlockState());
        Direction dir = Direction.fromAxisAndDirection(axis,
                AxisDirection.NEGATIVE);
        Optional<HologramSourceBlockEntity> opt = this.level.getBlockEntity(this.getBlockPos().relative(dir),
                COBlockEntities.HOLOGRAM_SOURCE.get());
        if (opt.isEmpty())
            return true;
        return !axis.equals(HologramSourceBlock.getConnectionAxis(opt.get().getBlockState()));
    }

    public boolean isController() {
        return this.getBlockPos().equals(this.controllerPos);
    }

    public @Nonnull HologramSourceBlockEntity findController() {

        Direction dir = Direction.fromAxisAndDirection(HologramSourceBlock.getConnectionAxis(this.getBlockState()),
                AxisDirection.NEGATIVE);

        BlockPos pos = this.getBlockPos();
        HologramSourceBlockEntity be = this;

        while (true) {
            pos = pos.relative(dir);
            Optional<HologramSourceBlockEntity> opt = this.level.getBlockEntity(pos,
                    COBlockEntities.HOLOGRAM_SOURCE.get());
            if (!opt.isPresent())
                break;
            be = opt.get();
            if (be.shouldBeController())
                break;

        }
        return be;
    }

    public @Nullable HologramSourceBlockEntity getController() {
        if (this.isController()) {
            return this;
        }
        if (this.controllerPos == null)
            return null;
        Optional<HologramSourceBlockEntity> opt = this.level.getBlockEntity(this.controllerPos,
                COBlockEntities.HOLOGRAM_SOURCE.get());
        if (opt.isPresent()) {
            return opt.get();
        } else {
            return null;
        }

    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB aabb = getProjectionBox();
        if (aabb != null)
            return aabb.inflate(0, 1, 0);
        return super.getRenderBoundingBox();
    }

    public @Nullable AABB getProjectionBox() {
        HologramSourceBlockEntity controller = this.getController();
        if (controller == null || controller.profile == null)
            return null;
        int length = controller.profile.connectionLength;
        Vec3 center = Vec3.atCenterOf(controller.getBlockPos())
                .add(
                        COUtils.getAbsVec(Vec3.atLowerCornerOf(
                                Direction.fromAxisAndDirection(
                                        HologramSourceBlock.getConnectionAxis(controller.getBlockState()),
                                        AxisDirection.POSITIVE).getNormal()))
                                .scale((length - 1) / 2D));
        center = center.add(0, 0.5 + length / 2D, 0);
        return new AABB(center, center).inflate(length / 2D);
    }

    public void update() {
        this.setChanged();
        this.sendData();
    }

    public @Nullable HologramSourceProfile getProfile() {
        return this.profile;
    }

    public void receiveBeam(BeamProperties prop, boolean onUpdate) {
        if (this.optionalBeamProperties.isEmpty() || onUpdate) {
            this.optionalBeamProperties = Optional.of(prop);
            this.update();
        }
    }

    public void removeBeam() {
        this.optionalBeamProperties = Optional.empty();
        this.update();
    }

    public Optional<BeamHelper.BeamProperties> getOptionalBeamProperties() {
        return this.isController() ? this.optionalBeamProperties : this.getController().getOptionalBeamProperties();
    }

    public boolean isActive() {
        if (this.isController()) {
            return this.getOptionalBeamProperties().isPresent();
        } else {
            return this.getController() != null && this.getController().isActive();
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider prov, boolean clientPacket) {
        super.read(tag, prov, clientPacket);
        this.optionalBeamProperties = BeamProperties.read(tag);
        NbtUtils.readBlockPos(tag, "ControllerPos").ifPresent(a -> {
            this.controllerPos = a;
        });
        this.profile = HologramSourceProfile.read(tag, prov);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider prov, boolean clientPacket) {
        super.write(tag, prov, clientPacket);
        this.optionalBeamProperties.ifPresent(prop -> {
            prop.write(tag);
        });
        if (this.controllerPos != null)
            tag.put("ControllerPos", NbtUtils.writeBlockPos(this.controllerPos));
        if (this.profile != null)
            this.getProfile().write(tag, prov);

    }

    public static class HologramSourceProfile {

        private int connectionLength = 1;
        public int fixedAngle = 0;
        public int angleVelocity = 45;
        public Mode displayMode = Mode.ROTATING_COUNTERCLOCKWISE;
        public ItemStack stack = ItemStack.EMPTY;
        private List<String> messages = new ArrayList<>();

        public HologramSourceProfile() {
            this.updateSections();
        }

        public HologramSourceProfile(int length, int angle, int mode, int angleVelocity) {
            this.connectionLength = length;
            this.fixedAngle = angle;
            this.angleVelocity = angleVelocity;
            this.displayMode = Mode.values()[mode % Mode.values().length]; // ciclic
            this.updateSections();
        }

        public int getConnectionLength() {
            return connectionLength;
        }

        public int getFixedAngle() {
            return fixedAngle;
        }

        public int getAngleVelocity() {
            return this.angleVelocity;
        }

        public Mode getDisplayMode() {
            return displayMode;
        }

        public List<String> getMessages() {
            return messages;
        }

        public boolean hasMessages() {
            return this.messages.stream()
                    .anyMatch(s -> s != null && !s.isEmpty());

        }

        public void setItemStack(ItemStack stack) {
            this.stack = stack;
        }

        public void setConnectionLength(int connectionLength) {
            this.connectionLength = connectionLength;
            this.updateSections();
        }

        public int getRowsCount() {
            return Math.min(5, 1 + connectionLength);
        }

        private void updateSections() {

            int mS = Math.min(5, this.messages.size());
            int mR = Math.min(5, this.getRowsCount());

            if (mS != mR) {
                List<String> sections = new ArrayList<>(
                        Collections.nCopies(this.getRowsCount(), new String()));
                for (int i = 0; i < Math.min(mS, mR); i++) {
                    sections.set(i, this.messages.get(i));
                }
                this.messages = sections;
            }

        }

        public void addSection(int i, String section) {
            this.updateSections();
            if (i < this.messages.size())
                this.messages.set(i, section);
        }

        public void write(CompoundTag tag, HolderLookup.Provider prov) {
            CompoundTag profile = new CompoundTag();
            profile.putInt("Length", this.connectionLength);
            profile.putInt("Angle", this.fixedAngle);
            profile.putInt("DisplayMode", this.displayMode.ordinal());
            profile.putInt("AngleVelocity", this.angleVelocity);
            profile.putInt("AngleVelocity", this.angleVelocity);
            profile.put("Messages", NBTHelper.writeCompoundList(this.messages, s -> {
                CompoundTag t = new CompoundTag();
                t.putString("String", s);
                return t;
            }));
            profile.put("Stack", this.stack.saveOptional(prov));

            tag.put("Profile", profile);
        }

        public static @Nullable HologramSourceProfile read(CompoundTag tag, HolderLookup.Provider prov) {
            HologramSourceProfile profile = null;
            if (tag.contains("Profile")) {
                CompoundTag profileTag = tag.getCompound("Profile");
                try {
                    profile = new HologramSourceProfile(
                            profileTag.getInt("Length"),
                            profileTag.getInt("Angle"),
                            profileTag.getInt("DisplayMode"),
                            profileTag.getInt("AngleVelocity"));
                    if (profileTag.contains("Messages")) {
                        List<String> sections = NBTHelper.readCompoundList(
                                profileTag.getList("Messages", Tag.TAG_COMPOUND), t -> t.getString("String"));
                        for (int i = 0; i < sections.size(); i++) {
                            profile.addSection(i, sections.get(i));
                        }
                    }
                    profile.stack = ItemStack.parseOptional(prov, profileTag.getCompound("Stack"));
                } catch (Exception ex) {
                    CreateOptical.LOGGER.info("Unable to read HologramSourceProfile");
                }
            }
            return profile;
        }
    }

    public boolean hasFixedAngle() {
        return this.profile.displayMode.shouldRenderAngle;
    }

    public enum Mode {
        ROTATING_COUNTERCLOCKWISE("counterclockwise", false),
        ROTATING_CLOCKWISE("clockwise", false),
        SPECIFIC_ANGLE("specific_angle", true);

        final String translationKey;
        final boolean shouldRenderAngle;

        Mode(String name, boolean shouldRenderAngle) {
            this.translationKey = "gui.hologram_source.mode_" + name;
            this.shouldRenderAngle = shouldRenderAngle;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }

        public boolean isShouldRenderAngle() {
            return shouldRenderAngle;
        }

        public static List<Component> getComponents() {
            List<Component> components = new ArrayList<>();
            for (Mode mode : Mode.values()) {
                components.add(COLang.Prefixes.OPTICAL.translate((mode.getTranslationKey())));
            }
            return components;
        }
    }

    public record DisplaySection(List<MutableComponent> line) {
        public DisplaySection() {
            this(new ArrayList<>());
        }

        public String getText() {
            String s = "";
            for (MutableComponent c : this.line()) {
                s = s.concat(c.getString());
            }
            return s;
        }

        public void write(CompoundTag tag, HolderLookup.Provider prov) {
            tag.put("DisplaySection",
                    NBTHelper.writeCompoundList(this.line(), component -> {
                        CompoundTag c = new CompoundTag();
                        c.putString("Element", Component.Serializer.toJson(component, prov));
                        return c;
                    }));
        }

        public static DisplaySection read(CompoundTag tag, HolderLookup.Provider prov) {
            ArrayList<MutableComponent> components = new ArrayList<>();
            if (tag.contains("DisplaySection")) {
                ListTag list = tag.getList("DisplaySection", Tag.TAG_COMPOUND);
                components.addAll(NBTHelper.readCompoundList(list, compound -> {
                    return Component.Serializer.fromJson(compound.getString("Element"), RegistryAccess.EMPTY);
                }));
            }
            return new DisplaySection(components);
        }

    }
}
