package net.lpcamors.optical;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.lpcamors.optical.blocks.display_sources.BeamPropertiesDisplaySources;

public class CODisplaySources {

    public static void initiate() {
    }

    public static final RegistryEntry<DisplaySource, BeamPropertiesDisplaySources.Intensity> BEAM_INTENSITY = CreateOptical.REGISTRATE
            .displaySource("beam_intensity", BeamPropertiesDisplaySources.Intensity::new).register();

    public static final RegistryEntry<DisplaySource, BeamPropertiesDisplaySources.BeamDamage> BEAM_DAMAGE = CreateOptical.REGISTRATE
            .displaySource("beam_damage", BeamPropertiesDisplaySources.BeamDamage::new).register();

    public static final RegistryEntry<DisplaySource, BeamPropertiesDisplaySources.BeamType> BEAM_TYPE = CreateOptical.REGISTRATE
            .displaySource("beam_type", BeamPropertiesDisplaySources.BeamType::new).register();

    public static final RegistryEntry<DisplaySource, BeamPropertiesDisplaySources.Signal> SIGNAL = CreateOptical.REGISTRATE
            .displaySource("signal", () -> new BeamPropertiesDisplaySources.Signal()).register();
}
