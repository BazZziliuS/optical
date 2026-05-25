package net.lpcamors.optical.blocks.beam_modulator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;

import org.jetbrains.annotations.NotNull;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.lpcamors.optical.CreateOptical;
import net.lpcamors.optical.blocks.COBlocks;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamSignal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BeamModulatorBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    ScrollValueBehaviour frequency;

    private final ArrayList<String> signal = new ArrayList<>();

    public BeamModulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        this.frequency = new ModulatorBehaviour(Lang.builder("tooltip")
                .translate(CreateOptical.ID + ".gui.behaviour.beam_reader_frequency").component(), this,
                new ReaderFrequencyValueBoxTransform()).between(0, 200);
        behaviours.add(this.frequency);

    }

    public BeamHelper.BeamSignal getSignal(BeamProperties prop) {
        return new BeamSignal(this.frequency.getValue(), generateSignalString(prop));
    }

    private List<String> generateSignalString(BeamProperties prop) {
        ArrayList<String> s_arr = new ArrayList<>();
        for (int k = 0; k < this.signal.size() && k < 5; k++) {
            String s = this.signal.get(k);
            float f = 0.3f * prop.getType().ordinal();
            String finalS = "";
            String[] sarr = s.split(" ");
            int i = 0;
            for (String substring : sarr) {
                finalS += randomFromSubstring(substring, f);
                i++;
                if (i != sarr.length) {
                    finalS += " ";
                }
            }
            s_arr.add(finalS);
        }
        return s_arr;
    }

    public void addMessages(List<String> signals) {
        this.signal.clear();
        this.signal.addAll(signals);
        this.sendData();
        this.setChanged();
    }

    @Override
    protected void read(CompoundTag compoundTag, HolderLookup.Provider prov, boolean clientPacket) {
        super.read(compoundTag, prov, clientPacket);
        this.signal.clear();
        if (compoundTag.contains("Signal")) {
            this.signal
                    .addAll(NBTHelper.readCompoundList(compoundTag.getList("Signal", CompoundTag.TAG_COMPOUND), tag -> {
                        return tag.getString("String");
                    }));
        }

    };

    @Override
    protected void write(CompoundTag compoundTag, HolderLookup.Provider prov, boolean clientPacket) {
        super.write(compoundTag, prov, clientPacket);

        compoundTag.put("Signal", NBTHelper.writeCompoundList(this.signal, a -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("String", a);
            return tag;
        }));
    };

    private static class ReaderFrequencyValueBoxTransform extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(0, 0, 0);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction == Direction.UP;
        }

        @Override
        public float getScale() {
            return 0.5f;
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return new Vec3(.5, 2f / 16f, .5)
                    .add(VecHelper.rotate(VecHelper.voxelSpace(0, 0, -3), angle(state), Axis.Y));
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            TransformStack.of(ms)
                    .rotateYDegrees(angle(state))
                    .rotateXDegrees(90);
        }

        protected float angle(BlockState state) {
            float horizontalAngle = COBlocks.BEAM_MODULATOR.has(state)
                    ? AngleHelper.horizontalAngle(state.getValue(BeamModulatorBlock.FACING))
                    : 0;
            return horizontalAngle;
        }

    }

    private static String randomFromSubstring(String substring, float chance) {
        if (substring == null) {
            throw new IllegalArgumentException("Substring can't be null");
        }

        long seed = 1125899906842597L;
        for (int i = 0; i < substring.length(); i++) {
            seed = 31 * seed + substring.charAt(i);
        }

        Random r = new Random(seed);
        String finalS = "";
        char[] chars = substring.toCharArray();
        for (int i = 0; i < substring.length(); i++) {
            char c = r.nextFloat() > chance ? chars[i] : '❚';
            finalS += c;
        }
        return finalS;
    }

    private static class ModulatorBehaviour extends ScrollValueBehaviour {

        public ModulatorBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
            super(label, be, slot);
        }

        @Override
        public boolean readFromClipboard(@NotNull Provider registries, CompoundTag tag, Player player, Direction side,
                boolean simulate) {

            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (AllBlocks.CLIPBOARD.isIn(stack)) {
                List<ClipboardEntry> entries = ClipboardEntry.getLastViewedEntries(stack);
                Iterator<ClipboardEntry> var18 = entries.iterator();
                ArrayList<String> strings = new ArrayList<>();
                while (var18.hasNext()) {
                    ClipboardEntry entry = var18.next();
                    String[] var20 = entry.text.getString().split("\n");
                    int var21 = var20.length;

                    for (int var22 = 0; var22 < var21; ++var22) {
                        String string = var20[var22];
                        strings.add(string);
                    }
                    if (strings.size() == 5)
                        break;
                }
                if (this.blockEntity instanceof BeamModulatorBlockEntity be) {
                    be.addMessages(strings);
                }
            }
            return super.readFromClipboard(registries, tag, player, side, simulate);
        }

    }
}
