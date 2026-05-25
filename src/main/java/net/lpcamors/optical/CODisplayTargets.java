package net.lpcamors.optical;

import java.util.function.Supplier;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.lpcamors.optical.blocks.hologram_source.HologramSourceDisplayTarget;

public class CODisplayTargets {

    public static void initiate() {
    }

    public static final RegistryEntry<DisplayTarget, HologramSourceDisplayTarget> HOLOGRAM_SOURCE = simple(
            "hologram_source",
            HologramSourceDisplayTarget::new);

    private static <T extends DisplayTarget> RegistryEntry<DisplayTarget, T> simple(String name, Supplier<T> supplier) {
        return CreateOptical.REGISTRATE.displayTarget(name, supplier).register();
    }
}
