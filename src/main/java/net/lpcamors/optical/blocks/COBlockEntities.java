package net.lpcamors.optical.blocks;

import static net.lpcamors.optical.CreateOptical.REGISTRATE;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import net.lpcamors.optical.blocks.absorption_polarizing_filter.AbsorptionPolarizingFilterBlockEntity;
import net.lpcamors.optical.blocks.beam_condenser.BeamCondenserBlockEntity;
import net.lpcamors.optical.blocks.beam_focuser.BeamFocuserBlockEntity;
import net.lpcamors.optical.blocks.beam_modulator.BeamModulatorBlockEntity;
import net.lpcamors.optical.blocks.beam_reader.BeamReaderBlockEntity;
import net.lpcamors.optical.blocks.encased_mirror.EncasedMirrorBlockEntity;
import net.lpcamors.optical.blocks.hologram_source.HologramSourceBlockEntity;
import net.lpcamors.optical.blocks.optical_receptor.OpticalReceptorBlockEntity;
import net.lpcamors.optical.blocks.optical_sensor.OpticalSensorBlockEntity;
import net.lpcamors.optical.blocks.optical_source.OpticalSourceBlockEntity;
import net.lpcamors.optical.blocks.polarizing_beam_splitter_block.PolarizingBeamSplitterBlockEntity;
import net.lpcamors.optical.blocks.thermal_optical_source.ThermalOpticalSourceBlockEntity;
import net.lpcamors.optical.renderers.BeamCondenserRenderer;
import net.lpcamors.optical.renderers.BeamFocuserRenderer;
import net.lpcamors.optical.renderers.EncasedMirrorRenderer;
import net.lpcamors.optical.renderers.HologramSourceRenderer;
import net.lpcamors.optical.renderers.OpticalReceptorRenderer;
import net.lpcamors.optical.renderers.OpticalSensorRenderer;
import net.lpcamors.optical.renderers.OpticalSourceRenderer;
import net.lpcamors.optical.renderers.PolarizingBeamSplitterRenderer;
import net.lpcamors.optical.renderers.ThermalOpticalSourceRenderer;
import net.lpcamors.optical.visual.MirrorVisual;
import net.lpcamors.optical.visual.ReceptorVisual;

public class COBlockEntities {

    public static void initiate() {
    }

    public static final BlockEntityEntry<OpticalSourceBlockEntity> OPTICAL_SOURCE = REGISTRATE
            .blockEntity("optical_source", OpticalSourceBlockEntity::new)
            .visual(() -> OrientedRotatingVisual.backHorizontal(AllPartialModels.SHAFT_HALF))
            .validBlocks(COBlocks.OPTICAL_SOURCE)
            .renderer(() -> OpticalSourceRenderer::new)
            .register();
    public static final BlockEntityEntry<ThermalOpticalSourceBlockEntity> THERMAL_OPTICAL_SOURCE = REGISTRATE
            .blockEntity("thermal_optical_source", ThermalOpticalSourceBlockEntity::new)
            .visual(() -> OrientedRotatingVisual.backHorizontal(AllPartialModels.SHAFT_HALF))
            .validBlocks(COBlocks.THERMAL_OPTICAL_SOURCE)
            .renderer(() -> ThermalOpticalSourceRenderer::new)
            .register();

    public static final BlockEntityEntry<OpticalReceptorBlockEntity> OPTICAL_RECEPTOR = REGISTRATE
            .blockEntity("optical_receptor", OpticalReceptorBlockEntity::speed)
            .visual(() -> ReceptorVisual::new, false)
            .validBlocks(COBlocks.LIGHT_OPTICAL_RECEPTOR)
            .renderer(() -> OpticalReceptorRenderer::new)
            .register();
    public static final BlockEntityEntry<OpticalReceptorBlockEntity> CAPACITY_OPTICAL_RECEPTOR = REGISTRATE
            .blockEntity("optical_receptor_capacity", OpticalReceptorBlockEntity::capacity)
            .visual(() -> ReceptorVisual::new, false)
            .validBlocks(COBlocks.HEAVY_OPTICAL_RECEPTOR)
            .renderer(() -> OpticalReceptorRenderer::new)
            .register();
    public static final BlockEntityEntry<EncasedMirrorBlockEntity> ENCASED_MIRROR = REGISTRATE
            .blockEntity("encased_mirror", EncasedMirrorBlockEntity::new)
            .visual(() -> MirrorVisual::new, false)
            .validBlocks(COBlocks.ENCASED_MIRROR)
            .renderer(() -> EncasedMirrorRenderer::new)
            .register();

    public static final BlockEntityEntry<AbsorptionPolarizingFilterBlockEntity> ABSORPTION_POLARIZING_FILTER = REGISTRATE
            .blockEntity("absorption_polarizing_filter", AbsorptionPolarizingFilterBlockEntity::new)
            .validBlocks(COBlocks.ABSORPTION_POLARIZING_FILTER)
            .register();

    public static final BlockEntityEntry<OpticalSensorBlockEntity> OPTICAL_SENSOR = REGISTRATE
            .blockEntity("optical_sensor", OpticalSensorBlockEntity::new)
            .validBlocks(COBlocks.OPTICAL_SENSOR)
            .renderer(() -> OpticalSensorRenderer::new)
            .register();
    public static final BlockEntityEntry<PolarizingBeamSplitterBlockEntity> POLARIZING_BEAM_SPLITTER = REGISTRATE
            .blockEntity("polarizing_beam_splitter", PolarizingBeamSplitterBlockEntity::new)
            .validBlocks(COBlocks.POLARIZING_BEAM_SPLITTER_BLOCK)
            .renderer(() -> PolarizingBeamSplitterRenderer::new)
            .register();
    public static final BlockEntityEntry<BeamCondenserBlockEntity> BEAM_CONDENSER = REGISTRATE
            .blockEntity("beam_condenser", BeamCondenserBlockEntity::new)
            .validBlocks(COBlocks.BEAM_CONDENSER)
            .renderer(() -> BeamCondenserRenderer::new)
            .register();

    public static final BlockEntityEntry<BeamFocuserBlockEntity> BEAM_FOCUSER = REGISTRATE
            .blockEntity("beam_focuser", BeamFocuserBlockEntity::new)
            .visual(() -> ShaftVisual::new, true)
            .validBlocks(COBlocks.BEAM_FOCUSER)
            .renderer(() -> BeamFocuserRenderer::new)
            .register();

    public static final BlockEntityEntry<HologramSourceBlockEntity> HOLOGRAM_SOURCE = REGISTRATE
            .blockEntity("hologram_source", HologramSourceBlockEntity::new)
            .validBlocks(COBlocks.HOLOGRAM_SOURCE)
            .renderer(() -> HologramSourceRenderer::new)
            .register();
    public static final BlockEntityEntry<BeamReaderBlockEntity> BEAM_READER = REGISTRATE
            .blockEntity("beam_reader", BeamReaderBlockEntity::new)
            .validBlocks(COBlocks.BEAM_READER)
            .register();
    public static final BlockEntityEntry<BeamModulatorBlockEntity> BEAM_MODULATOR = REGISTRATE
            .blockEntity("beam_modulator", BeamModulatorBlockEntity::new)
            .validBlocks(COBlocks.BEAM_MODULATOR)
            .register();


}
