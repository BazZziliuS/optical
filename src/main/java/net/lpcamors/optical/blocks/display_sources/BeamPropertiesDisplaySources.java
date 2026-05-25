package net.lpcamors.optical.blocks.display_sources;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;

import net.lpcamors.optical.blocks.beam_reader.BeamReaderBlockEntity;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamSignal;
import net.lpcamors.optical.data.COLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class BeamPropertiesDisplaySources {

    public static class Intensity extends NumericSingleLineDisplaySource {

        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            if (!(context.getSourceBlockEntity() instanceof BeamReaderBlockEntity reader))
                return ZERO.copy();

            if (reader.getOptionalBeamProperties().isPresent()) {

                BeamProperties prop = reader.getOptionalBeamProperties().get();
                return COLang.Prefixes.CREATE.translate("gui.goggles.intensity_value", prop.intensity());
            }
            return ZERO.copy();

        }

        @Override
        protected String getTranslationKey() {
            return "beam_intensity";
        }

        @Override
        protected boolean allowsLabeling(DisplayLinkContext context) {
            return true;
        }

    }

    public static class BeamDamage extends NumericSingleLineDisplaySource {

        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            if (!(context.getSourceBlockEntity() instanceof BeamReaderBlockEntity reader))
                return ZERO.copy();

            if (reader.getOptionalBeamProperties().isPresent()) {

                BeamProperties prop = reader.getOptionalBeamProperties().get();
                return COLang.Prefixes.CREATE.translate("gui.goggles.beam_damage_value", prop.getDamage());
            }
            return ZERO.copy();

        }

        @Override
        protected String getTranslationKey() {
            return "beam_damage";
        }

        @Override
        protected boolean allowsLabeling(DisplayLinkContext context) {
            return true;
        }

    }

    public static class BeamType extends SingleLineDisplaySource {

        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            if (!(context.getSourceBlockEntity() instanceof BeamReaderBlockEntity reader))
                return EMPTY_LINE.copy();

            if (reader.getOptionalBeamProperties().isPresent()) {

                BeamProperties prop = reader.getOptionalBeamProperties().get();
                return COLang.Prefixes.CREATE.translate(prop.getType().getDescriptionId());
            }
            return EMPTY_LINE.copy();

        }

        @Override
        protected String getTranslationKey() {
            return "beam_type";
        }

        @Override
        protected boolean allowsLabeling(DisplayLinkContext context) {
            return true;
        }

    }

    public static class Signal extends DisplaySource {

        public Signal() {
        }

        @Override
        protected String getTranslationKey() {
            return "beam_signal";
        }

        @Override
        public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
            if (!(context.getSourceBlockEntity() instanceof BeamReaderBlockEntity reader))
                return List.of();
            ArrayList<MutableComponent> s = new ArrayList<>();
            if (reader.getOptionalBeamProperties().isPresent()) {

                BeamProperties prop = reader.getOptionalBeamProperties().get();
                List<BeamSignal> signal = prop.signal().stream()
                        .filter(sig -> sig.freq() == reader.frequency.getValue())
                        .toList();
                if (!signal.isEmpty()) {
                    for (int i = 0; i < signal.get(0).message().size() && i < 5; i++) {
                        s.add(Component.literal(signal.get(0).message().get(i)));
                    }
                }
            }
            return s;
        }

    }

}
