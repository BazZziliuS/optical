package net.lpcamors.optical.blocks.optical_source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.platform.CatnipServices;
import net.lpcamors.optical.COUtils;
import net.lpcamors.optical.CreateOptical;
import net.lpcamors.optical.blocks.hologram_source.HologramSourceBlock;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamType;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BlockContext;
import net.lpcamors.optical.data.COLang;
import net.lpcamors.optical.data.COTags;
import net.lpcamors.optical.network.ConfigureOpticalSourcePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class GenericOpticalSourceBlockEntity extends KineticBlockEntity {

    public final List<BeamSection> sections = new ArrayList<>();
    public List<BeamSection> oldSections = new ArrayList<>();
    public final Map<BlockPos, BeamProperties> activators = new HashMap<>();
    public boolean shouldSendClient = false;

    private int chargingTick = 0;

    public GenericOpticalSourceBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isActive()) {
            if (this.level instanceof ServerLevel) {
                this.sections.forEach(section -> {
                    section.beamProperties
                            .incideOnBlock(new BlockContext(level, level.getBlockState(new BlockPos(section.toPos)),
                                    new BlockPos(section.toPos)));
                });
                this.oldSections = new ArrayList<>(this.sections);
                this.computeSections();
                this.updateSections();
            }
            if (chargingTick < 40)
                chargingTick++;

        } else {
            if (this.level instanceof ServerLevel) {
                this.sections.clear();
                this.updateSections();
                this.shouldSendClient = true;
            }
            if (chargingTick > 0)
                chargingTick--;

        }
        if (this.level instanceof ServerLevel serverLevel) {
            if (shouldSendClient) {

                CatnipServices.NETWORK.sendToClientsTrackingChunk(serverLevel,
                        new ChunkPos(this.getBlockPos()),
                        new ConfigureOpticalSourcePacket(this));
                this.shouldSendClient = false;
            }
        }
    }

    public final void updateSections() {
        List<BlockPos> toRemove = new ArrayList<>();
        List<BlockPos> list = this.sections.stream().map(a -> new BlockPos(a.toPos())).toList();
        this.activators.forEach((pos, prop) -> {
            if (!list.contains(pos))
                toRemove.add(pos);
        });

        toRemove.forEach(pos -> {
            BlockState state = this.level.getBlockState(pos);
            if (state.getBlock() instanceof IBeamActivator iActivator) {
                iActivator.onRemoveBeam(level, state, pos, this.activators.get(pos));
            }
            this.activators.remove(pos);

        });

    }

    @Override
    public void remove() {
        super.remove();
        this.onRemove();
    }

    final void onRemove() {
        this.sections.forEach(section -> {
            BlockPos pos = new BlockPos(section.toPos());
            if (this.activators.get(pos) == null)
                return;
            BlockState state = this.level.getBlockState(pos);
            if (state.getBlock() instanceof IBeamActivator iActivator) {
                iActivator.onRemoveBeam(level, state, pos, this.activators.get(pos));
            }
        });
    }

    public final void propagate(BeamProperties beamProperties, BlockPos initialPos, int range) {
        BlockPos fromPos = initialPos, toPos = initialPos;
        boolean shouldStopOnTouch = !beamProperties.canPassThroughEntities();
        while (range > 0) {
            range--;
            if (beamProperties == null)
                break;
            toPos = toPos.relative(beamProperties.direction());
            BlockState state = this.level.getBlockState(toPos);
            if (state.getBlock() instanceof BeaconBeamBlock beaconBlock) {
                if (this.addSection(fromPos, toPos, beamProperties)) {
                    beamProperties = COLOR_GLASS_FUNCTION.apply(beaconBlock).apply(beamProperties);
                    fromPos = toPos;
                    continue;
                } else if (shouldStopOnTouch) {
                    break;
                }
            }
            if (state.is(COTags.Blocks.IMPENETRABLE)) {
                this.addSection(fromPos, toPos, beamProperties);
                break;
            }
            if (state.is(COTags.Blocks.PENETRABLE) && range != 0)
                continue;
            if (this.addSection(fromPos, toPos, beamProperties)) {
                if (state.getBlock() instanceof IBeamActivator iBeamActivator) {
                    if (!this.activators.containsKey(toPos)) {
                        this.activators.put(toPos, beamProperties);
                        if (iBeamActivator.canReceive(this.level, state, toPos, beamProperties)) {
                            iBeamActivator.onReceiveBeam(level, state, toPos, beamProperties);
                        }
                    } else if (!this.activators.get(toPos).equals(beamProperties)) {
                        this.activators.remove(toPos);
                        this.activators.put(toPos, beamProperties);
                        iBeamActivator.onUpdateBeam(this.level, state, toPos, beamProperties);
                    }
                    beamProperties = iBeamActivator.transformProperties(this.level, state, toPos, beamProperties, this,
                            range);

                    fromPos = toPos;
                    continue;
                }
            } else if (shouldStopOnTouch) {
                break;
            }
            fromPos = toPos;
            beamProperties = null;
            continue;

        }

    }

    private final void computeSections() {
        BeamProperties initialBeamProperties = getInitialBeamProperties();
        BeamProperties beamProperties = initialBeamProperties;
        this.sections.clear();
        if (initialBeamProperties == null)
            return;
        int range = initialBeamProperties.beamType().getRange();
        this.propagate(beamProperties, this.getBlockPos(), range);
    }

    public final boolean shouldRenderBeam() {
        return this.isActive() && this.getInitialBeamProperties() != null
                && this.getInitialBeamProperties().isVisible();
    }

    public final int getBeamRadiusCount() {
        return Math.min(10,
                Math.min(this.getChargingTick(),
                        3 + (int) Math.floor(this.getInitialBeamProperties().intensity() / 32)));
    }

    public final int getChargingTick() {
        return 1 + (this.chargingTick / 4);
    }

    public abstract boolean isActive();

    public abstract @Nullable BeamProperties getInitialBeamProperties();

    private final boolean addSection(Vec3i fromPos, Vec3i toPos, BeamProperties beamProperties) {
        BeamSection section = new BeamSection(fromPos, toPos, beamProperties), section1 = section;
        List<Entity> entities = this.level.getEntities(null, section.getAABB());

        if (!entities.isEmpty()) {
            if (!beamProperties.beamType().equals(BeamType.GAMMA)) {
                Vec3i n = beamProperties.direction().getNormal()
                        .multiply(beamProperties.direction().getAxisDirection().getStep());
                Vec3i m = new Vec3i((int) Math.pow(0, n.getX()), (int) Math.pow(0, n.getY()),
                        (int) Math.pow(0, n.getZ()));
                Vec3i fromPosNormal = COUtils.multiplyVec3i(fromPos, n);
                Vec3i toPosVar = toPos;
                boolean f = false;
                Entity irradiated = entities.get(0);
                for (Entity entity : entities) {
                    double d1 = fromPosNormal.distSqr(COUtils.multiplyVec3i(toPosVar, n));
                    double d2 = fromPosNormal.distSqr(COUtils.multiplyVec3i(entity.blockPosition(), n));
                    // LITERALLY JUST TO FIX ODD VISUAL BEHAVIOUR
                    boolean f1 = entity.getBlockStateOn()
                            .getBlock() instanceof HologramSourceBlock;
                    if (d1 > d2 && !f1) {
                        toPosVar = COUtils.multiplyVec3i(fromPos, m)
                                .offset(COUtils.multiplyVec3i(entity.blockPosition(), n));
                        irradiated = entity;
                        f = true;
                    }
                }
                if (f) {
                    section = new BeamSection(fromPos, toPosVar, beamProperties);
                    beamProperties.incideOnEntity(irradiated);
                }
            } else {
                entities.forEach(beamProperties::incideOnEntity);
            }
        }
        int i1 = this.sections.size(), i2 = this.oldSections.indexOf(section);
        this.sections.add(section);
        this.shouldSendClient = i1 != i2;
        if (!this.shouldSendClient) {
            this.shouldSendClient = section.beamProperties.equals(this.oldSections.get(i2).beamProperties);
        }
        if (section != section1) {

        }
        return section == section1;
    }

    public static record BeamSection(Vec3i fromPos, Vec3i toPos, BeamProperties beamProperties) {

        public AABB getAABB() {
            Vec3 off = new Vec3(0.07, 0.07, 0.07);
            Vec3 lowerCorner = Vec3.atCenterOf(fromPos).add(off.multiply(-1, -1, -1));
            Vec3 upperCorner = Vec3.atCenterOf(toPos).add(off);
            return new AABB(lowerCorner, upperCorner);
        }

        @Override
        public final String toString() {
            return "BeamSection: from: " + fromPos.toString() + "; to: " + toPos.toString() + "; with prop: "
                    + this.beamProperties.toString();
        }

        public static Optional<BeamSection> read(CompoundTag tag) {
            if (tag.contains("BeamSection")) {
                CompoundTag compound = tag.getCompound("BeamSection");
                try {
                    return Optional.of(new BeamSection(NbtUtils.readBlockPos(compound, "FromPos").get(),
                            NbtUtils.readBlockPos(compound, "ToPos").get(),
                            BeamProperties.read(compound).get()));
                } catch (Exception ex) {

                }
            }
            return Optional.empty();
        }

        public static void write(CompoundTag tag, BeamSection sec) {
            CompoundTag compound = new CompoundTag();
            compound.put("FromPos", NbtUtils.writeBlockPos(new BlockPos(sec.fromPos)));
            compound.put("ToPos", NbtUtils.writeBlockPos(new BlockPos(sec.toPos)));
            sec.beamProperties.write(compound);
            tag.put("BeamSection", compound);

        }

        public static StreamCodec<ByteBuf, BeamSection> CODEC = new StreamCodec<ByteBuf, BeamSection>() {

            public BeamSection decode(ByteBuf buffer) {
                return new BeamSection(FriendlyByteBuf.readBlockPos(buffer), FriendlyByteBuf.readBlockPos(buffer),
                        BeamHelper.PROPERTIES_CODEC.decode(buffer));
            }

            public void encode(ByteBuf buffer, BeamSection section) {
                FriendlyByteBuf.writeBlockPos(buffer, new BlockPos(section.fromPos));
                FriendlyByteBuf.writeBlockPos(buffer, new BlockPos(section.toPos));
                BeamHelper.PROPERTIES_CODEC.encode(buffer, section.beamProperties);
            }
        };
    }

    private static final Function<BeaconBeamBlock, Function<BeamProperties, BeamProperties>> COLOR_GLASS_FUNCTION = beacon -> {
        return prop -> {
            return new BeamProperties.Builder(prop)
                    .color(COUtils.meanPoint(COUtils.getColor(beacon.getColor()), prop.color())).build();
        };
    };

    public static void goggleTooltip(List<Component> tooltip, boolean isPlayerSneaking,
            OpticalSourceBlockEntity be) {

        Lang.builder("tooltip").translate(CreateOptical.ID + ".gui.goggles.beam_properties").forGoggles(tooltip);

        if (Math.abs(be.getSpeed()) > 0) {
            BeamHelper.BeamType beamType = be.getInitialBeamProperties().getType();
            Lang.builder("")
                    .add(COLang.Prefixes.CREATE.translate(("gui.goggles.beam_type")).withStyle(ChatFormatting.GRAY))
                    .forGoggles(tooltip);
            Lang.builder("")
                    .add(COLang.Prefixes.CREATE.translate(beamType.getDescriptionId()).withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);
            Lang.builder("").add(
                    COLang.Prefixes.CREATE.translate(("gui.goggles.propagation_range")).withStyle(ChatFormatting.GRAY))
                    .forGoggles(tooltip);
            Lang.builder("")
                    .add(Component.literal(" " + beamType.getRange() + " blocks").withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);
        }

        BeamHelper.BeamPolarization beamPolarization = be.getPolarization().get();

        Lang.builder("")
                .add(COLang.Prefixes.CREATE.translate(("gui.goggles.polarization")).withStyle(ChatFormatting.GRAY))
                .forGoggles(tooltip);
        Lang.builder("")
                .add(Component.translatable(beamPolarization.getTranslationKey())
                        .append(" " + beamPolarization.getsIcon()).withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);

    }

    public static interface IBeamActivator {

        public static final AABB CENTERED_OFFSET = new AABB(0.5d, 0.5d, 0.5d, 0.5d, 0.5d, 0.5d);

        /*
         * * Transforms the current beamProperties, can change direction, polarization
         * etc
         */
        public @Nullable BeamProperties transformProperties(Level level, BlockState state, BlockPos pos,
                BeamProperties prop, GenericOpticalSourceBlockEntity source, int range);

        /*
         * Checks if the beam can receive beam to receiveTick() and onReceiveBeam()
         */
        public boolean canReceive(Level level, BlockState state, BlockPos pos, BeamProperties prop);

        /* Triggers when no longer receive a beam with that specific beamProperties */
        public void onRemoveBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop);

        /* Triggers when first receive a beam with that specific beamProperties */
        public void onReceiveBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop);

        /* Triggers when first receive a beam with that specific beamProperties */
        public void onUpdateBeam(Level level, BlockState state, BlockPos pos, BeamProperties prop);

        public AABB getNonVisibleAABB(Level level, BlockState state, BlockPos pos);
    }
}
