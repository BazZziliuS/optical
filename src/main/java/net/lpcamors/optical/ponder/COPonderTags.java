package net.lpcamors.optical.ponder;

import com.tterrag.registrate.util.entry.RegistryEntry;

import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.lpcamors.optical.CreateOptical;
import net.lpcamors.optical.blocks.COBlocks;
import net.minecraft.resources.ResourceLocation;

public class COPonderTags {

    public static final ResourceLocation OPTICALS = CreateOptical.loc("opticals");

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {

        PonderTagRegistrationHelper<RegistryEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        helper.registerTag(OPTICALS)
                .addToIndex()
                .item(COBlocks.OPTICAL_SOURCE.get(), true, false)
                .title("Optical Components")
                .description("Components which work with optical beams.")
                .register();

        HELPER.addToTag(OPTICALS)
                .add(COBlocks.OPTICAL_SOURCE)
                .add(COBlocks.THERMAL_OPTICAL_SOURCE)
                .add(COBlocks.LIGHT_OPTICAL_RECEPTOR)
                .add(COBlocks.HEAVY_OPTICAL_RECEPTOR)
                .add(COBlocks.ENCASED_MIRROR)
                .add(COBlocks.ABSORPTION_POLARIZING_FILTER)
                .add(COBlocks.POLARIZING_BEAM_SPLITTER_BLOCK)
                .add(COBlocks.OPTICAL_SENSOR)
                .add(COBlocks.BEAM_CONDENSER)
                .add(COBlocks.BEAM_FOCUSER)
                .add(COBlocks.HOLOGRAM_SOURCE)
                .add(COBlocks.BEAM_READER)
                .add(COBlocks.BEAM_MODULATOR);

    }
}
