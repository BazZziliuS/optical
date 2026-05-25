package net.lpcamors.optical.blocks.optical_source;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.nbt.NBTHelper;
import net.lpcamors.optical.CODamageSources;
import net.lpcamors.optical.COIcons;
import net.lpcamors.optical.COUtils;
import net.lpcamors.optical.CreateOptical;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public class BeamHelper {

    public static StreamCodec<ByteBuf, List<BeamSignal>> SIGNALS_CODEC = new StreamCodec<ByteBuf, List<BeamSignal>>() {
        public void encode(ByteBuf buffer, List<BeamSignal> value) {
            buffer.writeInt(value.size());
            value.forEach(a -> BeamSignal.CODEC.encode(buffer, a));
        };

        public List<BeamSignal> decode(ByteBuf buffer) {
            int size = buffer.readInt();
            ArrayList<BeamSignal> signals = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                signals.add(BeamSignal.CODEC.decode(buffer));
            }
            return signals;
        };
    };

    public static StreamCodec<ByteBuf, BeamProperties> PROPERTIES_CODEC = new StreamCodec<ByteBuf, BeamHelper.BeamProperties>() {

        public BeamProperties decode(ByteBuf buffer) {
            return new BeamProperties(
                    buffer.readFloat(),
                    Direction.values()[buffer.readInt()],
                    BeamType.values()[buffer.readInt()],
                    BeamPolarization.values()[buffer.readInt()],
                    FriendlyByteBuf.readBlockPos(buffer),
                    SIGNALS_CODEC.decode(buffer),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean());
        }

        public void encode(ByteBuf buffer, BeamProperties prop) {
            buffer.writeFloat(prop.intensity);
            buffer.writeInt(prop.direction.ordinal());
            buffer.writeInt(prop.beamType.ordinal());
            buffer.writeInt(prop.polarization().ordinal());
            FriendlyByteBuf.writeBlockPos(buffer, new BlockPos(prop.color));
            SIGNALS_CODEC.encode(buffer, prop.signal());
            buffer.writeBoolean(prop.spin());
            buffer.writeBoolean(prop.forceVisibility());
            buffer.writeBoolean(prop.forcePenetration());
            buffer.writeBoolean(prop.isDirty());

        }
    };
    public static final float SPEED_CONSTANT = 4F;

    public static float intensityBySpeed(float speed) {
        return Math.abs(speed / SPEED_CONSTANT);
    }

    public static boolean spinBySpeed(float speed) {
        return speed > 0;
    }

    public static record BeamProperties(float intensity, Direction direction, BeamType beamType,
            BeamPolarization polarization, Vec3i color, List<BeamSignal> signal, boolean spin, boolean forceVisibility,
            boolean forcePenetration, boolean isDirty) {

        public static BeamProperties BASE = new BeamProperties(1, Direction.UP, BeamType.VISIBLE,
                BeamPolarization.RANDOM,
                COUtils.getColor(DyeColor.LIGHT_GRAY), List.of(), false, false, false, false);

        public float getEffectiveSpeed() {
            return SPEED_CONSTANT * this.intensity * (this.spin ? 1 : -1);
        }

        public float getDamage() {
            return (float) Math.floor(intensity / 32);
        }

        public BeamType getType() {
            return this.beamType;
        }

        public boolean isVisible() {
            return this.beamType.visible() || this.forceVisibility;
        }

        public boolean canPassThroughEntities() {
            return this.beamType.canPassThroughEntities() || this.forcePenetration;
        }

        public void incideOnEntity(Entity entity) {
            this.beamType.livingEntityBiConsumer.accept(entity, this);
        }

        public void incideOnBlock(BlockContext context) {
            this.beamType.blockStateBiConsumer.accept(context, this);
        }

        public void write(CompoundTag compoundTag) {
            ListTag listTag = new ListTag(), tagFloats = new ListTag(), tagInts = new ListTag(),
                    color = NBTHelper.writeVec3i(this.color);
            tagFloats.add(FloatTag.valueOf(this.intensity));
            tagInts.add(IntTag.valueOf(this.polarization().ordinal()));
            tagInts.add(IntTag.valueOf(this.direction == null ? 0 : direction.ordinal()));
            tagInts.add(IntTag.valueOf(this.forceVisibility ? 1 : 0));
            tagInts.add(IntTag.valueOf(this.forcePenetration ? 1 : 0));
            tagInts.add(IntTag.valueOf(this.beamType.ordinal()));
            tagInts.add(IntTag.valueOf(this.spin ? 1 : 0));
            tagInts.add(IntTag.valueOf(this.isDirty ? 1 : 0));

            listTag.add(tagFloats);
            listTag.add(tagInts);
            listTag.add(color);

            listTag.add(NBTHelper.writeCompoundList(this.signal, sig -> {
                CompoundTag tag = new CompoundTag();
                if (sig == null)
                    return tag;
                sig.write(tag);
                return tag;
            }));

            compoundTag.put("BeamProperties", listTag);
        }

        public static Optional<BeamProperties> read(CompoundTag compoundTag) {
            try {
                if (!compoundTag.contains("BeamProperties"))
                    return Optional.empty();
                ListTag listTag = (ListTag) compoundTag.get("BeamProperties");
                ListTag tagFloats = listTag.getList(0);
                ListTag tagInts = listTag.getList(1);
                ListTag tagColor = listTag.getList(2);
                ListTag tagSignals = listTag.getList(3);

                float intensity = tagFloats.getFloat(0);
                Vec3i color = NBTHelper.readVec3i(tagColor);
                BeamPolarization polarization = BeamPolarization.values()[tagInts.getInt(0)];
                Direction direction = Direction.values()[tagInts.getInt(1)];
                boolean forceVisibility = tagInts.getInt(2) == 1;
                boolean forcePenetration = tagInts.getInt(3) == 1;
                BeamType beamType = BeamType.values()[tagInts.getInt(4)];
                boolean spin = tagInts.getInt(5) != 0;
                boolean isDirty = tagInts.getInt(6) != 0;
                List<BeamSignal> signal = List.of();
                if (tagSignals != null) {
                    signal = NBTHelper.readCompoundList(tagSignals, BeamSignal::read).stream()
                            .filter(a -> a != null).toList();

                }
                return Optional.of(new BeamProperties(intensity, direction, beamType, polarization, color, signal, spin,
                        forceVisibility, forcePenetration, isDirty));

            } catch (Exception e) {
                CreateOptical.LOGGER
                        .info("Trying to load an older version of the world and catch some irregularities, but it's all good :D");
                CreateOptical.LOGGER.error(e.getMessage());
            }
            return Optional.empty();
        }

        public static class Builder {

            private float intensity;
            private Direction direction;
            private BeamType beamType;
            private BeamPolarization polarization;
            private Vec3i color;
            private ArrayList<BeamSignal> signal;
            private boolean spin;
            private boolean dirty = false;
            private boolean forceVisibility;
            private boolean forcePenetration;

            public Builder(BeamProperties prop) {
                this.intensity = prop.intensity();
                this.direction = prop.direction();
                this.beamType = prop.beamType();
                this.polarization = prop.polarization();
                this.color = prop.color();
                this.signal = new ArrayList<>(prop.signal());
                this.spin = prop.spin();
                this.forceVisibility = prop.forceVisibility();
                this.dirty = prop.isDirty();
            }

            public Builder(Direction direction, List<BeamProperties> beamProperties) {
                this.direction = direction;
                this.intensity = 0;
                this.forceVisibility = false;
                this.color = null;
                this.signal = new ArrayList<>();
                for (BeamProperties beamProperties1 : beamProperties) {
                    if (this.intensity < beamProperties1.intensity) {
                        this.beamType = beamProperties1.beamType;
                        this.spin = beamProperties1.spin;
                    }
                    this.intensity += beamProperties1.intensity;

                    if (this.polarization != BeamPolarization.RANDOM) {
                        if (this.polarization != null && this.polarization != beamProperties1.polarization()) {
                            this.polarization = BeamPolarization.RANDOM;
                        } else {
                            this.polarization = beamProperties1.polarization();
                        }
                    }

                    if (beamProperties1.beamType.visible()) {
                        this.forceVisibility = true;
                        if (color == null) {
                            color = beamProperties1.color;
                        } else {
                            color = COUtils.meanPoint(color, beamProperties1.color);
                        }
                    }
                    beamProperties1.signal().forEach(this::addSignal);
                }
                this.color = this.color == null ? COUtils.getColor(DyeColor.GRAY) : this.color;
                this.forcePenetration = this.beamType.canPassThroughEntities();
            }

            public Builder intensity(float intensity) {
                this.intensity = intensity;
                return this;
            }

            public Builder color(Vec3i color) {
                this.color = color;
                return this;
            }

            public Builder direction(Direction direction) {
                this.direction = direction;
                return this;
            }

            public Builder polarization(BeamPolarization pol) {
                this.polarization = pol;
                return this;
            }

            public Builder addSignal(BeamSignal signal1) {
                this.signal.removeIf(sig -> sig.freq() == signal1.freq());
                this.signal.add(signal1);
                return this;
            }

            public Builder isDirty(boolean isDirty) {
                this.dirty = isDirty;
                return this;
            }

            public BeamProperties build() {
                return new BeamProperties(this.intensity, this.direction, this.beamType, this.polarization, this.color,
                        this.signal, this.spin, this.forceVisibility, this.forcePenetration, this.dirty);
            }

        }
    }

    public enum BeamPolarization implements StringRepresentable, INamedIconOptions {

        RANDOM("", COIcons.POL_RANDOM),
        HORIZONTAL("⬅➡", COIcons.POL_HORIZONTAL),
        DIAGONAL_POSITIVE("⬋⬈", COIcons.POL_DIAGONAL_POSITIVE),
        VERTICAL("⬇⬆", COIcons.POL_VERTICAL),
        DIAGONAL_NEGATIVE("⬊⬉", COIcons.POL_DIAGONAL_NEGATIVE),;

        final @Nullable Integer angleIndex;
        final String sIcon;
        final COIcons coIcons;

        BeamPolarization(String sIcon, COIcons coIcons) {
            this.angleIndex = this.ordinal() == 0 ? null : this.ordinal() - 1;
            this.sIcon = sIcon;
            this.coIcons = coIcons;
        }

        public BeamPolarization getNextRotated(int n) {
            BeamPolarization pol = this;
            for (int i = 0; i < n; i++) {
                pol = pol.getNextRotated();
            }
            return pol;
        }

        public BeamPolarization getNextRotated() {
            if (this.ordinal() == 0)
                return this;
            return BeamPolarization.values()[1 + (this.ordinal() % 4)];
        }

        public String getsIcon() {
            return sIcon;
        }

        public boolean isDiagonal() {
            return this.equals(DIAGONAL_NEGATIVE) || this.equals(DIAGONAL_POSITIVE);
        }

        public @Nullable double getAngle() {
            return this.ordinal() == 0 ? null : (this.ordinal() - 1) * Math.PI * 0.25D;
        }

        public float getRemainingIntensity(float intensity, BeamPolarization beamPolarization) {
            float f;
            int oThis = this.ordinal(), oPol = beamPolarization.ordinal();
            if (oThis == oPol || oPol == 0) {
                f = 1F;
            } else if (oThis == 0) {
                f = 0.5F;
            } else {
                f = Math.abs(oThis - oPol) % 2 == 0 ? 0.0f : 0.5f;
            }
            return f * intensity;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public String getDescriptionId() {
            return getTranslationKey();
        }

        @Override
        public AllIcons getIcon() {
            return this.coIcons;
        }

        @Override
        public String getTranslationKey() {
            return "create.create_optical.polarization." + this.getSerializedName();
        }
    }

    public static BiConsumer<Entity, BeamProperties> livingEntityNothing() {
        return (livingEntity, beamProperties) -> {
        };
    }

    public static BiConsumer<BlockContext, BeamProperties> blockStateNothing() {
        return (a, b) -> {
        };
    }

    public static BiConsumer<BlockContext, BeamProperties> evaporatesWaterBasedBlocks() {
        return (context, beamProperties) -> {
            float f = context.state.is(BlockTags.SNOW) ? 0.01f : context.state.is(BlockTags.ICE) ? 0.001f : 0.0f;
            if (f > 0 && !context.level.isClientSide) {
                if (context.level().getGameTime() % 20 == 0) {
                    if (f >= context.level().random.nextFloat())
                        return;
                    context.level.levelEvent(LevelEvent.PARTICLES_WATER_EVAPORATING,
                            context.pos.atY(context.pos().getY() - 1),
                            Block.getId(context.state));
                    context.level.playSound(null, context.pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                            2.6F + (context.level.random.nextFloat() - context.level.random.nextFloat()) * 0.8F);
                    context.level.removeBlock(context.pos, false);

                }
            }
        };
    }

    public static BiConsumer<Entity, BeamProperties> dealDamage() {
        return (livingEntity, beamProperties) -> {
            float damage = beamProperties.getDamage();
            if (damage > 0)

                livingEntity.hurt(CODamageSources.gammaRay(livingEntity.level()), damage);
        };
    }

    public record BlockContext(Level level, BlockState state, BlockPos pos) {
    }

    public enum BeamType {

        RADIO(
                livingEntityNothing(), blockStateNothing()),
        MICROWAVE(
                livingEntityNothing(), evaporatesWaterBasedBlocks()),
        VISIBLE(
                livingEntityNothing(), blockStateNothing()),
        GAMMA(
                dealDamage(), blockStateNothing());
        ;

        protected final BiConsumer<Entity, BeamProperties> livingEntityBiConsumer;
        protected final BiConsumer<BlockContext, BeamProperties> blockStateBiConsumer;

        BeamType(BiConsumer<Entity, BeamProperties> livingEntityBiConsumer,
                BiConsumer<BlockContext, BeamProperties> blockStateBiConsumer) {
            this.livingEntityBiConsumer = livingEntityBiConsumer;
            this.blockStateBiConsumer = blockStateBiConsumer;
        }

        public int getRange() {
            return (4 - this.ordinal()) * 32;
        }

        public static BeamType getTypeBySpeed(float speed) {
            BeamType beamType = RADIO;
            for (BeamType beamType1 : BeamType.values()) {
                beamType = beamType1;
                if (Math.abs(speed) <= Math.pow(2, 2 * beamType1.ordinal() + 3)) {
                    break;
                }
            }
            return beamType;
        }

        public boolean visible() {
            return this.equals(VISIBLE);
        }

        public boolean canPassThroughEntities() {
            return this.equals(GAMMA);
        }

        public String getDescriptionId() {
            return "beam_type.type." + (this.name().toLowerCase(Locale.ROOT));
        }

    }

    public record BeamSignal(int freq, List<String> message) {

        public @Nullable List<String> test(int givFreq) {
            if (freq == givFreq)
                return this.message();
            return null;
        }

        public static final StreamCodec<ByteBuf, BeamSignal> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                BeamSignal::freq,
                ByteBufCodecs.stringUtf8(8192).apply(ByteBufCodecs.list(5)),
                BeamSignal::message,
                BeamSignal::new);

        public void write(CompoundTag tag) {
            CompoundTag compound = new CompoundTag();
            compound.putInt("Frequency", this.freq());

            compound.put("Messages", NBTHelper.writeCompoundList(this.message(), a -> {
                CompoundTag tag1 = new CompoundTag();
                tag1.putString("String", a);
                return tag1;
            }));
            tag.put("BeamSignal", compound);
        }

        public static @Nullable BeamSignal read(CompoundTag tag) {
            if (tag.contains("BeamSignal")) {
                CompoundTag compound = tag.getCompound("BeamSignal");
                if (compound.contains("Frequency") && compound.contains("Messages")) {
                    List<String> list = tag.get("Messages") == null ? List.of()
                            : NBTHelper.readCompoundList((ListTag) tag.get("Messages"), tag1 -> {
                                return tag1.getString("String");
                            });
                    return new BeamSignal(compound.getInt("Frequency"), list);
                }
            }
            return null;
        }

    }

}
