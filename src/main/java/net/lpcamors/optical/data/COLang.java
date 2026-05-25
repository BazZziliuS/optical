package net.lpcamors.optical.data;

import javax.annotation.Nullable;

import net.createmod.catnip.lang.LangBuilder;
import net.lpcamors.optical.CreateOptical;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class COLang {

    public static void initiate() {
    }

    static {

        Prefixes.OPTICAL.register("gui.hologram_source.title", "Hologram Source");
        Prefixes.OPTICAL.register("gui.hologram_source.mode", "Rotation Mode");
        Prefixes.OPTICAL.register("gui.hologram_source.mode_clockwise", "Clockwise");
        Prefixes.OPTICAL.register("gui.hologram_source.mode_counterclockwise", "Counterclockwise");
        Prefixes.OPTICAL.register("gui.hologram_source.mode_specific_angle", "At angle");
        
        Prefixes.OPTICAL.register("display_source.beam_damage", "Beam Damage");
        Prefixes.OPTICAL.register("display_source.beam_intensity", "Beam Intensity");
        Prefixes.OPTICAL.register("display_source.beam_type", "Beam Type");
        Prefixes.OPTICAL.register("display_source.beam_signal", "Signal Information");
 

        Prefixes.TOOLTIP.register("gui.goggles.absorption_polarizing_filter", "Polarizing Filter Stats:");
        Prefixes.TOOLTIP.register("gui.behaviour.optical_source", "Propagated Beam Polarization");
        Prefixes.TOOLTIP.register("gui.behaviour.beam_reader_frequency", "Signal Frequency");
        Prefixes.TOOLTIP.register("gui.goggles.beam_properties", "Beam Properties:");
        Prefixes.TOOLTIP.register("gui.goggles.optical_sensor", "Sensor Properties:");
        Prefixes.TOOLTIP.register("gui.goggles.receptor_properties", "Receptor Properties:");
        Prefixes.TOOLTIP.register("gui.goggles.beam_reader", "Beam Properties:");

        Prefixes.CREATE.register("gui.goggles.beam_type", "Beam Type:");
        Prefixes.CREATE.register("gui.goggles.propagation_range", "Propagation Range:");
        Prefixes.CREATE.register("gui.goggles.polarization", "Polarization:");
        Prefixes.CREATE.register("gui.goggles.optical_sensor.mode", "Mode");
        Prefixes.CREATE.register("gui.goggles.optical_sensor.mode.intensity", "Intensity");
        Prefixes.CREATE.register("gui.goggles.optical_sensor.mode.color", "Color");
        Prefixes.CREATE.register("gui.goggles.optical_sensor.mode.digital", "Digital");
        Prefixes.CREATE.register("gui.goggles.sensor_count", "Sensors attached:");
        Prefixes.CREATE.register("gui.goggles.sensor_count.full", "full");
        Prefixes.CREATE.register("gui.goggles.sensor_count.empty", "empty");
        Prefixes.CREATE.register("gui.goggles.intensity", "Intensity: ");
        Prefixes.CREATE.register("gui.goggles.intensity_value", "%.2fiu");
        Prefixes.CREATE.register("gui.goggles.color", "Color: ");
        Prefixes.CREATE.register("gui.goggles.color_value", " (%d, %d, %d)");
        Prefixes.CREATE.register("gui.goggles.beam_damage", "Beam Damage: ");
        Prefixes.CREATE.register("gui.goggles.beam_damage_value", "%d");
        Prefixes.CREATE.register("gui.goggles.no_beam", "No beam received...");

        Prefixes.JEI.register("focusing.sequence.0", "Sequenced Radio Focusing");
        Prefixes.JEI.register("focusing.sequence.1", "Sequenced Microwave Focusing");
        Prefixes.JEI.register("focusing.sequence.2", "Sequenced Visible Focusing");
        Prefixes.JEI.register("focusing.sequence.3", "Sequenced Gamma Focusing");
        Prefixes.JEI.register("focusing.sequence.4", "Sequenced Focusing");
        Prefixes.JEI.register("focusing", "Focusing");


        Prefixes.JEI.register("required_beam_type.radio", "Radio waves required");
        Prefixes.JEI.register("required_beam_type.microwave", "Microwaves required");
        Prefixes.JEI.register("required_beam_type.visible", "Visible light required");
        Prefixes.JEI.register("required_beam_type.gamma", "Gamma rays required");
        Prefixes.JEI.register("required_beam_type.none", "No beam type required");

        Prefixes.CREATE.register("beam_type.type.radio", "Radio Waves");
        Prefixes.CREATE.register("beam_type.type.microwave", "Microwaves");
        Prefixes.CREATE.register("beam_type.type.visible", "Visible Light");
        Prefixes.CREATE.register("beam_type.type.gamma", "Gamma Ray");
        Prefixes.CREATE.register("beam_type.type.range", "Block Range:");
        Prefixes.CREATE.register("polarization.random", "Random");
        Prefixes.CREATE.register("polarization.vertical", "Vertical");
        Prefixes.CREATE.register("polarization.diagonal_positive", "Positive Diagonal");
        Prefixes.CREATE.register("polarization.horizontal", "Horizontal");
        Prefixes.CREATE.register("polarization.diagonal_negative", "Negative Diagonal");

        Prefixes.CREATIVE_TAB.register("co_base", "Optical Tab");

        Prefixes.DEATH.register("gamma_ray", "%1$s received a high dose of gamma rays");
    }

    public enum Prefixes {
        OPTICAL(null),
        TOOLTIP("tooltip"),
        CREATE("create"),
        RECIPE("recipe"),
        JEI("jei"),
        CREATIVE_TAB("itemGroup"),
        DEATH("death.attack"),
        ;

        final @Nullable String pFix;

        Prefixes(@Nullable String pFix) {
            this.pFix = pFix;
        }

        public void register(String s, String value) {
            if (this.pFix == null) {
                CreateOptical.REGISTRATE.addRawLang(CreateOptical.ID + "." + s, value);
            } else {
                CreateOptical.REGISTRATE.addLang(this.pFix, ResourceLocation.fromNamespaceAndPath(CreateOptical.ID, s),
                        value);
            }
        }

        public MutableComponent translate(String key, Object... args) {
            String s = this.pFix + "." + CreateOptical.ID + "." + key;
            if (this.pFix == null) {
                s = CreateOptical.ID + "." + key;
            }
            return Component.translatable(s, resolveBuilders(args));
        }

    }

    public static Object[] resolveBuilders(Object[] args) {
        for (int i = 0; i < args.length; i++)
            if (args[i] instanceof LangBuilder cb)
                args[i] = cb.component();
        return args;
    }

}
