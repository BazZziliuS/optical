package net.lpcamors.optical.ponder;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.lpcamors.optical.COUtils;
import net.lpcamors.optical.blocks.COBlocks;
import net.lpcamors.optical.blocks.beam_focuser.BeamFocuserBlockEntity;
import net.lpcamors.optical.blocks.hologram_source.HologramSourceBlockEntity;
import net.lpcamors.optical.blocks.hologram_source.HologramSourceBlockEntity.Mode;
import net.lpcamors.optical.blocks.optical_receptor.OpticalReceptorBlockEntity;
import net.lpcamors.optical.blocks.optical_sensor.OpticalSensorBlock;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity.BeamSection;
import net.lpcamors.optical.blocks.thermal_optical_source.ThermalOpticalSourceBlockEntity;
import net.lpcamors.optical.items.COItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class COPonderScenes {

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(COBlocks.OPTICAL_SOURCE, COBlocks.LIGHT_OPTICAL_RECEPTOR, COBlocks.HEAVY_OPTICAL_RECEPTOR)
                .addStoryBoard("opticals/base", COPonderScenes::base, COPonderTags.OPTICALS);

        HELPER.forComponents(COBlocks.LIGHT_OPTICAL_RECEPTOR, COBlocks.HEAVY_OPTICAL_RECEPTOR)
                .addStoryBoard("opticals/receptors", COPonderScenes::receptors, COPonderTags.OPTICALS);

        HELPER.forComponents(COBlocks.OPTICAL_SOURCE).addStoryBoard("opticals/beam_types", COPonderScenes::beamTypes);

        HELPER.forComponents(COBlocks.ENCASED_MIRROR, COBlocks.OPTICAL_SOURCE).addStoryBoard("opticals/mirror",
                COPonderScenes::mirror, COPonderTags.OPTICALS);

        HELPER.forComponents(COBlocks.ABSORPTION_POLARIZING_FILTER, COBlocks.OPTICAL_SOURCE)
                .addStoryBoard("opticals/polarizing_filter", COPonderScenes::polarizingFilter, COPonderTags.OPTICALS);

        HELPER.forComponents(COBlocks.POLARIZING_BEAM_SPLITTER_BLOCK, COBlocks.OPTICAL_SOURCE)
                .addStoryBoard("opticals/polarizing_cube", COPonderScenes::polarizingCube, COPonderTags.OPTICALS);

        HELPER.forComponents(COBlocks.OPTICAL_SENSOR, COBlocks.OPTICAL_SOURCE).addStoryBoard("opticals/sensor",
                COPonderScenes::sensor, COPonderTags.OPTICALS);

        HELPER.forComponents(COBlocks.OPTICAL_SOURCE, COBlocks.BEAM_CONDENSER, COBlocks.LIGHT_OPTICAL_RECEPTOR,
                COBlocks.HEAVY_OPTICAL_RECEPTOR)
                .addStoryBoard("opticals/condenser", COPonderScenes::condenser, COPonderTags.OPTICALS);

        HELPER.forComponents(COBlocks.OPTICAL_SOURCE, COBlocks.BEAM_FOCUSER).addStoryBoard("opticals/focuser",
                COPonderScenes::focuser, COPonderTags.OPTICALS);

        HELPER.forComponents(COBlocks.HOLOGRAM_SOURCE)
                .addStoryBoard("opticals/hologram_source", COPonderScenes::hologramItem, COPonderTags.OPTICALS);

        HELPER.forComponents(COBlocks.HOLOGRAM_SOURCE)
                .addStoryBoard("opticals/hologram_display", COPonderScenes::hologramMessagesItem,
                        COPonderTags.OPTICALS);

        HELPER.forComponents(COBlocks.OPTICAL_SOURCE, COBlocks.THERMAL_OPTICAL_SOURCE)
                .addStoryBoard("opticals/thermal_source", COPonderScenes::thermalSource, COPonderTags.OPTICALS);

        HELPER.forComponents(COBlocks.BEAM_READER, COBlocks.BEAM_MODULATOR)
                .addStoryBoard("opticals/beam_reader", COPonderScenes::reader,
                        COPonderTags.OPTICALS);

        HELPER.forComponents(COBlocks.BEAM_READER, COBlocks.BEAM_MODULATOR)
                .addStoryBoard("opticals/modulator", COPonderScenes::modulator,
                        COPonderTags.OPTICALS);

    }

    public static void base(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.base", "Transmitting Rotational Force with Light");
        scene.configureBasePlate(0, 0, 5);
        BlockPos source = util.grid().at(4, 1, 2);
        Selection sourceSystemSelect = util.select().fromTo(4, 1, 2, 5, 1, 2);
        Selection bigWheelSelect = util.select().position(5, 0, 1);
        BlockPos receptor = util.grid().at(0, 1, 2);
        Selection receptorSystemSelect = util.select().fromTo(0, 1, 2, 2, 1, 3);
        BlockPos blocking = util.grid().at(3, 1, 2);
        Selection blockingSelect = util.select().position(blocking);

        addSourceSection(scene, util, source, source, receptor, Direction.EAST);

        scene.idle(5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(4, 1, 2, 5, 1, 2), Direction.DOWN);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 0);
        scene.world().setKineticSpeed(receptorSystemSelect, 0);
        scene.idle(20);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 64);
        scene.effects().indicateSuccess(source);
        scene.idle(20);
        scene.overlay().showText(40)
                .text("Optical sources transform rotational force into light")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.UP));
        scene.idle(60);

        addAllSensorToReceptor(scene, util, receptor);

        scene.world().showSection(util.select().fromTo(0, 1, 2, 2, 1, 3), Direction.DOWN);
        scene.idle(20);
        scene.world().setKineticSpeed(receptorSystemSelect, 64);
        scene.effects().rotationSpeedIndicator(receptor);
        scene.idle(20);
        scene.overlay().showText(80)
                .text("Optical receptors can transform the received light into rotational force")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(util.grid().at(0, 1, 2), Direction.WEST));
        scene.idle(100);

        scene.world().showSection(blockingSelect, Direction.DOWN);
        scene.world().setKineticSpeed(receptorSystemSelect, 0);
        scene.effects().rotationSpeedIndicator(receptor);

        removeLastSourceSection(scene, util, source);
        addSourceSection(scene, util, source, source, blocking, Direction.EAST);

        scene.idle(20);
        scene.overlay().showText(80)
                .text("Tinted glass, non-transparent blocks and entities can block the light")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(blocking, Direction.UP));
        scene.idle(100);
    }

    public static void beamTypes(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.beam_types", "Propagating Different Types of Light");
        scene.configureBasePlate(0, 0, 5);
        BlockPos source = util.grid().at(4, 1, 2);
        Selection sourceSystemSelect = util.select().fromTo(4, 1, 2, 5, 1, 2);
        Selection bigWheelSelect = util.select().position(5, 0, 1);
        Selection beamSelect = util.select().fromTo(1, 1, 2, 3, 1, 2);
        AABB selectBB = new AABB(1, 1, 2, 4, 2, 3);
        scene.idle(5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(util.select().layer(1), Direction.DOWN);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 0);
        scene.idle(20);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 64);
        scene.effects().indicateSuccess(source);
        addSourceSection(scene, util, source, source, new BlockPos(0, 1, 2), Direction.EAST);

        scene.idle(20);
        scene.overlay().showText(60)
                .text("Optical sources can propagate different types of light depending on the given rotational force")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.UP));
        scene.idle(80);

        String[] actions = new String[] { "Radio Waves,", "Microwaves,", "Visible Light,", "and Gamma Ray" };
        scene.overlay().showText(110)
                .attachKeyFrame()
                .independent(40)
                .placeNearTarget()
                .text("The beam types are:");

        int y = 60;
        int time = 100;
        for (String s : actions) {
            scene.idle(10);
            scene.overlay().showText(time)
                    .colored(PonderPalette.MEDIUM)
                    .placeNearTarget()
                    .independent(y)
                    .text(s);
            y += 18;
            time -= 10;
        }
        scene.idle(10);
        scene.overlay().showText(60)
                .independent(y + 4)
                .placeNearTarget()
                .text("Higher the frequency, lower the range of the beam.");
        scene.idle(80);
        scene.overlay().showText(60)
                .attachKeyFrame()
                .independent(40)
                .colored(PonderPalette.GREEN)
                .placeNearTarget()
                .text("Each beam type has it's own specific properties, explore them by your own");
        scene.idle(80);

        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 256);
        scene.effects().indicateSuccess(source);
        clearSourceSection(scene, util, source);
        scene.idle(10);
        scene.world().showSection(beamSelect, Direction.DOWN);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, source, selectBB, 80);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .independent(72)
                .placeNearTarget()
                .text("But caution with higher frequencies, entities (including players) can be hurt by gamma rays");
        scene.idle(110);
    }

    public static void mirror(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.mirror", "Propagating Beams through mirrors");
        scene.configureBasePlate(0, 0, 5);
        BlockPos mirror = util.grid().at(1, 1, 2);
        BlockPos source = util.grid().at(4, 1, 2);
        BlockPos upReceptor = util.grid().at(1, 1, 4);
        BlockPos lowerReceptor = util.grid().at(1, 1, 0);
        Selection sourceSystemSelect = util.select().fromTo(4, 1, 2, 5, 1, 2);
        Selection rotationSection = util.select().fromTo(1, 1, 2, 1, 2, 2);
        Selection bigWheelSelect = util.select().position(5, 0, 1);
        Selection upReceptorSelect = util.select().fromTo(0, 1, 4, 1, 1, 4);
        Selection downReceptorSelect = util.select().fromTo(0, 1, 0, 1, 1, 0);

        scene.idle(5);

        addAllSensorToReceptor(scene, util, upReceptor);
        addAllSensorToReceptor(scene, util, lowerReceptor);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(sourceSystemSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(upReceptorSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(downReceptorSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(rotationSection, Direction.DOWN);
        scene.idle(5);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 0);
        scene.idle(20);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 64);
        scene.effects().indicateSuccess(source);

        addSourceSection(scene, util, source, source, mirror, Direction.EAST);
        addSourceSection(scene, util, source, mirror, lowerReceptor, Direction.NORTH);

        scene.idle(20);
        scene.world().setKineticSpeed(downReceptorSelect, 64);
        scene.effects().indicateSuccess(lowerReceptor);
        scene.idle(20);
        scene.overlay().showText(60)
                .text("Encased mirrors can propagate the incident beam perpendicularly.")
                .pointAt(util.vector().blockSurface(mirror, Direction.UP));
        scene.idle(80);

        scene.overlay().showText(60)
                .text("You can rotate it to make the beam propagate wherever you want")
                .pointAt(util.vector().blockSurface(mirror, Direction.UP));

        scene.world().setKineticSpeed(rotationSection, 32);
        scene.world().setKineticSpeed(downReceptorSelect, 0);

        removeLastSourceSection(scene, util, source);

        scene.idle(15);
        scene.world().setKineticSpeed(rotationSection, 0);
        addSourceSection(scene, util, source, mirror, upReceptor, Direction.NORTH);
        scene.idle(10);

        scene.world().setKineticSpeed(upReceptorSelect, 64);
        scene.effects().indicateSuccess(upReceptor);

        scene.idle(40);

    }

    public static void polarizingFilter(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.polarizing_filter", "Polarizing Filter");
        scene.configureBasePlate(0, 0, 5);

        BlockPos source = util.grid().at(4, 1, 2);
        BlockPos receptor = util.grid().at(0, 1, 0);
        Selection sourceSystemSelect = util.select().fromTo(4, 1, 2, 5, 1, 2);
        Selection bigWheelSelect = util.select().position(5, 0, 1);
        Selection receptorSystemSelect = util.select().fromTo(0, 1, 0, 1, 1, 0);
        BlockPos mirror = util.grid().at(0, 1, 2);
        Selection mirrorSelection = util.select().position(mirror);
        BlockPos filterPos0 = util.grid().at(1, 1, 2);
        Selection filterSelection0 = util.select().position(filterPos0);
        BlockPos filterPos1 = util.grid().at(3, 1, 2);
        Selection filterSelection1 = util.select().position(filterPos1);
        addAllSensorToReceptor(scene, util, receptor);

        scene.idle(5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(sourceSystemSelect, Direction.DOWN);
        scene.world().showSection(receptorSystemSelect, Direction.DOWN);
        scene.world().showSection(mirrorSelection, Direction.DOWN);
        scene.idle(5);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 0);
        scene.idle(10);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 32);
        scene.idle(20);
        scene.effects().indicateSuccess(source);

        scene.overlay().showText(60)
                .independent(0).colored(PonderPalette.BLUE)
                .text("Propagated beams have certain properties. One of them, is the polarization.");
        scene.idle(80);
        scene.world().setKineticSpeed(receptorSystemSelect, 32);
        scene.effects().rotationSpeedIndicator(receptor);
        scene.idle(20);
        scene.overlay().showText(80)
                .text("This receptor is receiving a randomly polarized beam. This means that the beam is not oscillating in a preferred direction.")
                .pointAt(util.vector().blockSurface(receptor, Direction.UP));
        scene.idle(100);

        scene.world().showSection(filterSelection0, Direction.DOWN);
        scene.idle(5);
        scene.world().setKineticSpeed(receptorSystemSelect, 16);
        scene.effects().rotationSpeedIndicator(receptor);
        scene.idle(20);
        scene.overlay().showText(60)
                .text("Polarizing filters can change the polarization of the beam and its intensity.")
                .attachKeyFrame().placeNearTarget()
                .pointAt(util.vector().blockSurface(filterPos0, Direction.UP));
        scene.idle(80);
        scene.overlay().showText(80)
                .independent(0)
                .text("Regardless of the direction of polarization, if the ray is randomly polarized it will always reduce its intensity by half.");
        scene.idle(100);
        scene.world().showSection(filterSelection1, Direction.DOWN);
        scene.idle(5);
        scene.world().setKineticSpeed(receptorSystemSelect, 0);

        scene.effects().rotationSpeedIndicator(receptor);
        scene.idle(20);
        scene.overlay().showText(100)
                .text("You can combine polarizing filters to get your desired intensity. Keep in mind that polarizing a beam perpendicularly will nullify its intensity.")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(filterPos1, Direction.UP));
        scene.idle(120);
        scene.world().hideSection(filterSelection1, Direction.UP);
        scene.idle(5);
        Vec3 blockSurface = util.vector().blockSurface(source, Direction.NORTH)
                .add(2 / 16f, 0, 3 / 16f);
        scene.overlay().showFilterSlotInput(blockSurface, Direction.NORTH, 80);
        scene.overlay().showControls(blockSurface, Pointing.DOWN, 60).rightClick();
        scene.idle(20);
        scene.overlay().showText(60)
                .text("The initial polarization can be configured in the source panel.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(blockSurface);
        scene.idle(80);
    }

    public static void receptors(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.receptors", "Receptor types: Theory into practice");
        scene.configureBasePlate(0, 0, 5);

        BlockPos source = util.grid().at(4, 1, 2);
        BlockPos source2 = util.grid().at(2, 1, 4);
        BlockPos lightReceptor = util.grid().at(2, 1, 2);
        BlockPos heavyReceptor = util.grid().at(0, 1, 0);
        Selection sourceSystemSelect = util.select().fromTo(4, 1, 2, 5, 1, 2)
                .add(util.select().fromTo(2, 1, 4, 2, 1, 5));
        Selection bigWheelSelect = util.select().position(5, 0, 1).add(util.select().position(3, 0, 5));

        Selection secondSection = util.select().position(2, 1, 0).add(util.select().position(0, 1, 2))
                .add(util.select().position(heavyReceptor));

        Vec3 blockSurface = util.vector().blockSurface(lightReceptor, Direction.WEST)
                .add(-0, 0, 0);

        scene.idle(5);

        addAllSensorToReceptor(scene, util, heavyReceptor);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(sourceSystemSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(lightReceptor), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showControls(blockSurface, Pointing.LEFT, 60).rightClick()
                .withItem(COItems.OPTICAL_DEVICE.asStack());
        scene.idle(10);
        addAllSensorToReceptor(scene, util, lightReceptor);

        scene.idle(10);

        scene.overlay().showText(60)
                .text("Right clicking with Optical Devices on a face of receptor adds a sensor to that direction.")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(lightReceptor, Direction.UP));
        scene.idle(70);

        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 0);
        scene.idle(10);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 129);
        scene.effects().indicateSuccess(source);
        scene.effects().indicateSuccess(source2);

        scene.idle(20);
        scene.world().setKineticSpeed(util.select().position(lightReceptor), 64);
        scene.effects().indicateSuccess(lightReceptor);
        scene.idle(20);
        scene.overlay().showText(40)
                .text("Faces with sensor can receive beams. ")
                .pointAt(util.vector().blockSurface(lightReceptor, Direction.UP));
        scene.idle(50);

        scene.overlay().showText(60)
                .text("This is a light receptor. Adding more beams makes the speed of the receptor increase.")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(lightReceptor, Direction.UP));
        scene.idle(70);

        scene.world().hideSection(util.select().position(lightReceptor), Direction.UP);
        scene.idle(20);
        scene.world().showSection(secondSection, Direction.DOWN);
        scene.idle(20);
        scene.world().setKineticSpeed(util.select().position(heavyReceptor), 64);
        scene.effects().indicateSuccess(heavyReceptor);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("This is a heavy receptor. Adding more beams makes the stress capacity of the receptor increase.")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(heavyReceptor, Direction.UP));
        scene.idle(70);

        scene.overlay().showText(60)
                .text("With this receptor you can create stress sources very distant from the contraption.");
        scene.idle(70);

    }

    public static void polarizingCube(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.polarizing_cube", "Polarizing Cube");
        scene.configureBasePlate(0, 0, 5);

        BlockPos source = util.grid().at(4, 1, 3);
        BlockPos polCube = util.grid().at(3, 1, 3);
        Selection polCubeSelect = util.select().position(polCube);
        Selection sel = util.select().position(0, 1, 3).add(util.select().position(3, 1, 0));

        AABB strBB = new AABB(1, 1, 3, 3, 2, 4);
        AABB perp1BB = new AABB(3, 1, 1, 4, 2, 3);

        Selection sourceSystemSelect = util.select().fromTo(4, 1, 3, 5, 1, 3);
        Selection bigWheelSelect = util.select().position(5, 0, 2);
        scene.idle(5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(sourceSystemSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(polCubeSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(sel, Direction.DOWN);
        scene.idle(5);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 0);
        scene.idle(10);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 64);
        scene.idle(20);
        scene.effects().indicateSuccess(source);

        addSourceSection(scene, util, source, source, polCube, Direction.EAST);
        addSourceSection(scene, util, source, polCube, util.grid().at(0, 1, 3), Direction.EAST);
        addSourceSection(scene, util, source, polCube, util.grid().at(3, 1, 0), Direction.NORTH);

        scene.overlay().showText(60)
                .independent(0)
                .text("Polarizing Beam Splitter blocks can split the beam in two polarized beams.")
                .pointAt(util.vector().blockSurface(polCube, Direction.UP));
        scene.idle(80);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, polCube, strBB, 80);
        scene.idle(10);
        scene.overlay().showText(60)
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .independent(72)
                .placeNearTarget()
                .text("One of them will propagate straight and be vertically polarized.");
        scene.idle(80);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, polCube, perp1BB, 80);
        scene.idle(10);
        scene.overlay().showText(60)
                .colored(PonderPalette.BLUE)
                .independent(82)
                .placeNearTarget()
                .text("The other one of them will propagate perpendicularly and be horizontally polarized.");
        scene.idle(80);
        scene.idle(10);

    }

    public static void sensor(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.sensor", "Sensor");
        scene.configureBasePlate(0, 0, 5);

        BlockPos source = util.grid().at(4, 1, 2);
        BlockPos sensor = util.grid().at(1, 1, 2);
        BlockPos pol = util.grid().at(2, 1, 2);
        BlockPos glass = util.grid().at(3, 1, 2);
        BlockPos tube = util.grid().at(0, 1, 2);
        Vec3 sensorCenter = util.vector().blockSurface(sensor, Direction.UP).add(0, -8 / 16F, 0);
        Selection sourceSystemSelect = util.select().fromTo(4, 1, 2, 5, 1, 2);
        Selection bigWheelSelect = util.select().position(5, 0, 2);
        Selection sensorGroupSelect = util.select().fromTo(1, 1, 2, 1, 2, 3);
        Selection tubeSelect = util.select().position(tube);
        scene.idle(5);
        scene.world().modifyBlock(sensor,
                state -> state.setValue(OpticalSensorBlock.MODE, OpticalSensorBlock.Mode.INTENSITY), false);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(sourceSystemSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(sensorGroupSelect, Direction.DOWN);
        scene.world().modifyBlockEntityNBT(tubeSelect, NixieTubeBlockEntity.class,
                nbt -> nbt.putInt("RedstoneStrength", 0));
        scene.world().showSection(tubeSelect, Direction.UP);
        scene.idle(5);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 0);
        scene.idle(10);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 32);
        scene.idle(20);
        scene.effects().indicateSuccess(source);
        scene.idle(10);
        scene.world().toggleRedstonePower(util.select().position(sensor));
        scene.world().modifyBlockEntityNBT(tubeSelect, NixieTubeBlockEntity.class,
                nbt -> nbt.putInt("RedstoneStrength", 15));
        scene.effects().indicateRedstone(sensor);
        scene.overlay().showText(60)
                .attachKeyFrame()
                .independent(0)
                .placeNearTarget()
                .text("Optical sensors emits redstone signal if a beam hits it.")
                .pointAt(sensorCenter);
        scene.idle(80);
        Vec3 blockSurface = util.vector().blockSurface(sensor, Direction.UP)
                .add(0, -8 / 16F, 0);
        scene.overlay().showFilterSlotInput(blockSurface, Direction.NORTH, 80);
        scene.overlay().showControls(blockSurface, Pointing.DOWN, 60).withItem(AllItems.WRENCH.asStack());
        scene.overlay().showText(60)
                .attachKeyFrame()
                .independent(0)
                .placeNearTarget()
                .text("You can change the criteria of the redstone level mapping using the wrench.")
                .pointAt(sensorCenter);
        scene.idle(80);
        scene.world().showSection(util.select().position(pol), Direction.DOWN);
        scene.idle(5);
        scene.world().modifyBlockEntityNBT(tubeSelect, NixieTubeBlockEntity.class,
                nbt -> nbt.putInt("RedstoneStrength", 7));
        scene.overlay().showText(60)
                .attachKeyFrame()
                .independent(0)
                .text("The intensity mode makes a reduction based on the intensity of the incident beam.")
                .pointAt(util.vector().blockSurface(tube, Direction.UP));
        scene.idle(80);
        scene.overlay().showFilterSlotInput(blockSurface, Direction.NORTH, 40);
        scene.overlay().showControls(blockSurface, Pointing.DOWN, 30).withItem(AllItems.WRENCH.asStack());
        scene.world().modifyBlock(sensor,
                state -> state.setValue(OpticalSensorBlock.MODE, OpticalSensorBlock.Mode.COLOR), false);
        scene.idle(10);
        scene.world().hideSection(util.select().position(pol), Direction.UP);
        scene.world().showSection(util.select().position(glass), Direction.UP);
        scene.world().modifyBlockEntityNBT(tubeSelect, NixieTubeBlockEntity.class,
                nbt -> nbt.putInt("RedstoneStrength", 13));

        scene.overlay().showText(60)
                .attachKeyFrame()
                .independent(0)
                .text("The color mode maps a color to an specific signal.")
                .pointAt(util.vector().blockSurface(tube, Direction.UP));
        scene.idle(80);
        scene.overlay().showFilterSlotInput(blockSurface, Direction.NORTH, 40);
        scene.overlay().showControls(blockSurface, Pointing.DOWN, 30).withItem(AllItems.WRENCH.asStack());
        scene.world().modifyBlock(sensor,
                state -> state.setValue(OpticalSensorBlock.MODE, OpticalSensorBlock.Mode.DIGITAL), false);
        scene.idle(10);
        scene.world().modifyBlockEntityNBT(tubeSelect, NixieTubeBlockEntity.class,
                nbt -> nbt.putInt("RedstoneStrength", 15));

        scene.overlay().showText(80)
                .attachKeyFrame()
                .independent(0)
                .text("The digital mode make the block full signal if there is a beam incident, if it don't have, then zero redstone signal.")
                .pointAt(util.vector().blockSurface(tube, Direction.UP));
        scene.idle(80);
        scene.world().hideSection(tubeSelect, Direction.UP);
        scene.idle(20);

        scene.overlay().showText(60)
                .independent(0)
                .text("In this mode, the sensor absorbs the color of the beam and produces light.")
                .pointAt(sensorCenter);
        scene.idle(80);
        scene.idle(10);

        scene.overlay().showFilterSlotInput(blockSurface, Direction.NORTH, 40);
        scene.overlay().showControls(blockSurface, Pointing.DOWN, 30).withItem(AllBlocks.BRASS_CASING.asStack())
                .rightClick();

        scene.world().modifyBlock(sensor,
                state -> state.setValue(OpticalSensorBlock.CASING, 1), true);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .independent(0)
                .text("You can change the visual of the sensor applying a casing to it.")
                .pointAt(util.vector().blockSurface(tube, Direction.UP));
        scene.idle(80);

    }

    public static void condenser(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.condenser", "Condenser and Receptor's heaviness");
        scene.configureBasePlate(0, 0, 5);

        Selection middleSourceSelect = util.select().fromTo(3, 1, 2, 4, 1, 2)
                .add(util.select().fromTo(5, 1, 0, 5, 1, 5));
        Selection leftSourceSelect = util.select().fromTo(4, 1, 4, 4, 1, 4);
        Selection rightSourceSelect = util.select().fromTo(4, 1, 0, 4, 1, 0);
        Selection leftMirror = util.select().fromTo(2, 1, 3, 3, 1, 4);
        Selection rightMirror = util.select().fromTo(2, 1, 0, 3, 1, 1);
        BlockPos condenser = util.grid().at(2, 1, 2);
        BlockPos receptor = util.grid().at(0, 1, 2);
        addAllSensorToReceptor(scene, util, receptor);
        Selection condenserSelect = util.select().position(condenser);
        Selection receptorSelect = util.select().fromTo(0, 1, 1, 0, 1, 2);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(condenserSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(middleSourceSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().setKineticSpeed(middleSourceSelect, 64);
        scene.idle(5);
        scene.world().showSection(rightSourceSelect, Direction.DOWN);
        scene.world().showSection(rightMirror, Direction.DOWN);
        scene.idle(5);
        scene.world().setKineticSpeed(rightSourceSelect, 64);
        scene.world().showSection(leftSourceSelect, Direction.DOWN);
        scene.world().showSection(leftMirror, Direction.DOWN);
        scene.idle(5);
        scene.world().setKineticSpeed(leftSourceSelect, 64);
        scene.effects().indicateSuccess(condenser);
        scene.idle(10);
        scene.overlay().showText(60)
                .attachKeyFrame()
                .independent(0)
                .text("Optical Condensers can combine up to three beams simultaneously!")
                .pointAt(util.vector().blockSurface(condenser, Direction.UP));
        scene.idle(60);
        String[] actions = new String[] { "Intensity is the sum of the beams intensities,",
                "Beam Type and speed direction is the same of the higher beam,",
                "Visibility depends if there's at least one visible beam composing it,",
                "Polarization follow the composing system." };
        scene.overlay().showText(300)
                .attachKeyFrame()
                .independent(20)
                .placeNearTarget()
                .text("The resultant beam acts exactly like a normal beam. But its properties follow these criteria:");
        int y = 62;
        int time = 275;
        for (String s : actions) {
            scene.idle(25);
            scene.overlay().showText(time)
                    .colored(PonderPalette.MEDIUM)
                    .placeNearTarget()
                    .independent(y)
                    .text(s);
            y += 32;
            time -= 25;
        }
        scene.idle(200);
        scene.idle(50);
        scene.overlay().showText(140)
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .placeNearTarget()
                .independent(20)
                .text("Composing system means that the components can compose a different result than itself. Combining different polarizations create a beam with random polarization. ");
        scene.idle(40);
        scene.overlay().showText(100)
                .colored(PonderPalette.BLUE)
                .placeNearTarget()
                .independent(92)
                .text("The color of the beam is the sum of the color of the visible beams.");
        scene.idle(120);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .independent(30)
                .placeNearTarget()
                .text("To transform really fast beams into rotational force you'll need a greater receptor");
        scene.idle(40);
        scene.world().showSection(receptorSelect, Direction.DOWN);
        scene.idle(10);
        scene.world().setKineticSpeed(receptorSelect, 32);
        scene.effects().rotationSpeedIndicator(receptor);
        scene.idle(40);
        scene.overlay().showText(60)
                .independent(0)
                .text("This is a heavy optical receptor. It works more like the light receptor but this one supports greater intensities.")
                .pointAt(util.vector().blockSurface(receptor, Direction.UP));
        scene.idle(70);
        scene.overlay().showText(40)
                .independent(0)
                .text("It have fixed speed.")
                .pointAt(util.vector().blockSurface(receptor, Direction.UP));
        scene.idle(50);

    }

    public static void focuser(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.beam_focuser", "Beam Focuser");
        scene.configureBasePlate(0, 0, 5);

        BlockPos focuser = util.grid().at(3, 3, 2);
        BlockPos source = util.grid().at(3, 4, 4);
        BlockPos mirror = util.grid().at(3, 4, 2);
        BlockPos beltStart = util.grid().at(4, 1, 2);
        Selection base = util.select().fromTo(0, 0, 0, 4, 0, 4);
        Selection belt = util.select().fromTo(0, 1, 2, 4, 1, 2);
        Selection motor = util.select().fromTo(3, 1, 3, 3, 3, 3);
        Selection sourceSelect = util.select().fromTo(3, 0, 5, 3, 4, 5)
                .add(util.select().position(source))
                .add(util.select().fromTo(3, 1, 4, 3, 3, 4));

        scene.world().showSection(base, Direction.UP);
        scene.idle(5);
        scene.world().showSection(belt, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(motor, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(focuser), Direction.DOWN);
        scene.idle(5);
        scene.world().setKineticSpeed(belt, 16);
        scene.world().setKineticSpeed(util.select().position(focuser), 16);
        scene.world().setKineticSpeed(motor, 16);
        scene.world().setKineticSpeed(sourceSelect, 16);
        scene.effects().indicateSuccess(focuser);
        scene.idle(5);
        scene.idle(10);
        scene.overlay().showText(40)
                .attachKeyFrame()
                .independent(0)
                .text("Optical focuser can process items with incident light and rotational power")
                .pointAt(util.vector().blockSurface(focuser, Direction.UP));
        scene.idle(50);
        scene.world().showSection(util.select().position(mirror), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(sourceSelect, Direction.DOWN);
        scene.overlay().showText(60)
                .independent(0)
                .attachKeyFrame()
                .text("This component needs a incident beam to work properly, you'll need to control the beam type of the source to process certain items")
                .pointAt(util.vector().blockSurface(focuser, Direction.UP));
        scene.idle(70);

        ElementLink<BeltItemElement> item = scene.world().createItemOnBelt(beltStart, Direction.NORTH,
                new ItemStack(Items.GLASS_PANE));
        scene.idle(30);
        scene.world().stallBeltItem(item, true);
        activateFocuser(scene, util, focuser, true);
        scene.idle(35);
        activateFocuser(scene, util, focuser, false);
        scene.world().removeItemsFromBelt(focuser.below(2));
        scene.world().createItemOnBeltLike(focuser.below(2), Direction.UP, new ItemStack(COItems.MIRROR.get()));
        scene.idle(60);
        scene.overlay().showText(40)
                .independent(0)
                .attachKeyFrame()
                .text("Some recipes need specific items to operate with the required mode")
                .pointAt(util.vector().blockSurface(focuser, Direction.EAST));
        scene.world().modifyBlockEntity(focuser, BeamFocuserBlockEntity.class,
                pte -> pte.filtering.setFilter(Direction.WEST, new ItemStack(Items.BLUE_DYE)));
        scene.effects().indicateSuccess(focuser);
        scene.idle(15);
        item = scene.world().createItemOnBelt(beltStart, Direction.NORTH, new ItemStack(Items.WHITE_WOOL));
        scene.idle(30);
        scene.world().stallBeltItem(item, true);
        activateFocuser(scene, util, focuser, true);
        scene.idle(35);
        activateFocuser(scene, util, focuser, false);
        scene.world().removeItemsFromBelt(focuser.below(2));
        scene.world().createItemOnBeltLike(focuser.below(2), Direction.UP, new ItemStack(Items.BLUE_WOOL));
        scene.idle(60);

    }

    public static void hologramItem(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.hologram_source", "Cool displays");
        scene.configureBasePlate(0, 0, 5);

        BlockPos source = util.grid().at(4, 1, 2);
        BlockPos hologramSource1 = util.grid().at(3, 1, 2);
        BlockPos hologramSource2 = util.grid().at(1, 1, 2);
        BlockPos glass = util.grid().at(2, 1, 2);
        Selection holograms = util.select().fromTo(0, 1, 2, 3, 1, 2);
        Selection sourceSystemSelect = util.select().fromTo(4, 1, 2, 5, 1, 2);
        Selection bigWheelSelect = util.select().position(5, 0, 1).add(util.select().position(3, 0, 5));

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(sourceSystemSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(holograms, Direction.DOWN);
        scene.idle(20);

        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 0);
        scene.idle(10);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 64);
        scene.effects().indicateSuccess(source);
        addSourceSection(scene, util, source, hologramSource1, glass, Direction.EAST);
        addSourceSection(scene, util, source, glass, hologramSource2, Direction.EAST,
                b -> b.color(COUtils.getColor(DyeColor.BLUE)));
        scene.idle(20);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("These hologram sources require visible light to work properly")
                .pointAt(util.vector().blockSurface(hologramSource2, Direction.UP));

        Vec3 blockSurface1 = util.vector().blockSurface(hologramSource1, Direction.DOWN)
                .add(0, 0, 0);
        Vec3 blockSurface2 = util.vector().blockSurface(hologramSource2, Direction.DOWN)
                .add(0, 0, 0);
        scene.idle(50);
        scene.overlay().showControls(blockSurface1, Pointing.UP, 30).rightClick()
                .withItem(new ItemStack(Items.NETHERITE_AXE));
        scene.idle(10);
        hologramActivation(scene, util, hologramSource1, BeamHelper.BeamProperties.BASE);
        setHologramItem(scene, util, hologramSource1, Items.NETHERITE_AXE, Mode.ROTATING_CLOCKWISE, 0);
        scene.idle(20);
        scene.overlay().showControls(blockSurface2, Pointing.UP, 30).rightClick()
                .withItem(new ItemStack(Items.GRASS_BLOCK));
        scene.idle(10);
        hologramActivation(scene, util, hologramSource2,
                new BeamHelper.BeamProperties.Builder(BeamProperties.BASE).color(COUtils.getColor(DyeColor.BLUE))
                        .build());
        setHologramItem(scene, util, hologramSource2, Items.GRASS_BLOCK, Mode.ROTATING_CLOCKWISE, 0);

        scene.idle(20);

        scene.idle(10);

        scene.overlay().showText(60)
                .text("Right clicking with an item displays it as a hologram")
                .pointAt(util.vector().blockSurface(hologramSource2, Direction.DOWN));
        scene.idle(80);

        scene.overlay().showText(60)
                .text("You can color the hologram by coloring the incident beam.")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(glass, Direction.UP));
        scene.idle(80);
        scene.overlay().showControls(blockSurface1, Pointing.UP, 30).rightClick();
        scene.idle(10);
        scene.overlay().showControls(blockSurface2, Pointing.UP, 30).rightClick();
        scene.idle(10);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .text("You can access the panel by right clicking it with no items in hand. There you can control the rotation of the hologram.")
                .pointAt(util.vector().blockSurface(hologramSource2, Direction.UP));

        setHologramItem(scene, util, hologramSource1, Items.NETHERITE_AXE, Mode.SPECIFIC_ANGLE, 45);
        scene.idle(10);

        setHologramItem(scene, util, hologramSource2, Items.GRASS_BLOCK, Mode.SPECIFIC_ANGLE, 45);
        scene.idle(90);

        scene.overlay().showControls(blockSurface1, Pointing.UP, 30).whileSneaking().rightClick();
        scene.idle(10);
        scene.overlay().showControls(blockSurface2, Pointing.UP, 30).whileSneaking().rightClick();
        scene.idle(10);

        setHologramItem(scene, util, hologramSource1, ItemStack.EMPTY, Mode.ROTATING_CLOCKWISE, 0);
        scene.idle(10);
        setHologramItem(scene, util, hologramSource2, ItemStack.EMPTY, Mode.ROTATING_CLOCKWISE, 0);
        scene.idle(10);

        scene.overlay().showText(60).attachKeyFrame()
                .text("You can remove the item by shift + right click with empty hand")
                .colored(PonderPalette.BLUE);
        scene.idle(80);

        scene.overlay().showText(60).attachKeyFrame()
                .text("Due to some limitations of the component, some items cannot be displayed correctly")
                .colored(PonderPalette.BLUE).pointAt(hologramSource2.getCenter());

        scene.overlay().showControls(blockSurface1, Pointing.UP, 30).withItem(new ItemStack(Items.SHIELD));
        scene.idle(10);
        scene.overlay().showControls(blockSurface2, Pointing.UP, 30).withItem(new ItemStack(Items.CONDUIT));
        scene.idle(10);

        setHologramItem(scene, util, hologramSource1, Items.SHIELD, Mode.SPECIFIC_ANGLE, 45);
        scene.idle(10);

        setHologramItem(scene, util, hologramSource2, Items.CONDUIT, Mode.SPECIFIC_ANGLE, 45);

        scene.idle(80);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .text("Hologram sources can be combined in a connection to make the hologram bigger!")
                .colored(PonderPalette.BLUE);
        scene.idle(10);
        scene.world().showSection(util.select().position(3, 1, 4), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(2, 1, 4), Direction.DOWN);
        scene.idle(10);
        ;
        scene.world().showSection(util.select().position(2, 1, 0), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(1, 1, 0), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(0, 1, 0), Direction.DOWN);
        scene.idle(30);

        scene.idle(10);

    }

    public static void hologramMessagesItem(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.hologram_display", "Cool messages");
        scene.configureBasePlate(0, 0, 5);

        BlockPos source = util.grid().at(4, 1, 2);
        BlockPos hologramSource1 = util.grid().at(2, 1, 2);
        BlockPos glass = util.grid().at(0, 1, 2);
        BlockPos linkPos = util.grid().at(1, 1, 4);
        BlockPos depot = util.grid().at(2, 1, 4);
        Selection sourceSystemSelect = util.select().fromTo(4, 1, 2, 5, 1, 2);
        Selection bigWheelSelect = util.select().position(5, 0, 1).add(util.select().position(3, 0, 5));
        Selection link = util.select().position(linkPos);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(sourceSystemSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(hologramSource1), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(glass), Direction.DOWN);
        scene.idle(5);

        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 0);
        scene.idle(10);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 64);
        scene.effects().indicateSuccess(source);
        addSourceSection(scene, util, source, source, hologramSource1, Direction.EAST);
        addSourceSection(scene, util, source, hologramSource1, glass, Direction.EAST);
        scene.idle(40);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("You can use Hologram Displays to also display messages...")
                .pointAt(util.vector().blockSurface(hologramSource1, Direction.DOWN));
        scene.idle(10);
        scene.overlay().showControls(hologramSource1.getCenter(), Pointing.UP, 60).rightClick()
                .withItem(AllBlocks.DISPLAY_LINK.asStack());
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, link, new AABB(hologramSource1), 60);
        scene.idle(40);
        scene.world().showSection(util.select().position(depot), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(link, Direction.DOWN);
        scene.idle(10);
        ItemStack stack = COItems.MIRROR.asStack();
        scene.world().createItemOnBeltLike(depot, Direction.SOUTH, stack);
        scene.idle(20);
        scene.world().flashDisplayLink(linkPos);
        hologramActivation(scene, util, hologramSource1, BeamProperties.BASE);
        setHologramMessage(scene, util, hologramSource1, stack.getHoverName().getString(), Mode.ROTATING_CLOCKWISE, 0);
        scene.idle(80);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("You can configure the display mode of hologram")
                .pointAt(util.vector().blockSurface(hologramSource1, Direction.DOWN));
        scene.idle(20);
        scene.overlay().showControls(hologramSource1.getCenter(), Pointing.UP, 60).rightClick();
        scene.idle(10);
        setHologramMessage(scene, util, hologramSource1, stack.getHoverName().getString(), Mode.SPECIFIC_ANGLE, 0);

        scene.idle(40);

    }

    public static void thermalSource(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.thermal_source", "Light Evolution");
        scene.configureBasePlate(0, 0, 5);
        BlockPos source = util.grid().at(4, 1, 2);
        Selection sourceSystemSelect = util.select().fromTo(4, 1, 2, 5, 1, 2);
        Selection tubes = util.select().fromTo(4, 1, 3, 5, 1, 3);
        Selection bigWheelSelect = util.select().position(5, 0, 1);
        BlockPos receptor = util.grid().at(1, 1, 2);
        BlockPos gaugePos = util.grid().at(1, 1, 3);
        Selection receptorSystemSelect = util.select().fromTo(1, 1, 2, 1, 1, 3);

        scene.idle(5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(4, 1, 2, 5, 1, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(tubes, Direction.DOWN);

        addAllSensorToReceptor(scene, util, receptor);
        scene.idle(10);
        scene.world().showSection(receptorSystemSelect, Direction.DOWN);
        scene.idle(10);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 0);
        scene.world().setKineticSpeed(receptorSystemSelect, 0);
        scene.idle(10);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 64);

        scene.idle(20);
        scene.overlay().showText(40)
                .text("Thermal Optical sources require a fluid to emmit a beam")
                .pointAt(util.vector().blockSurface(source, Direction.UP));
        scene.idle(60);

        scene.world().setKineticSpeed(tubes, -64);

        Vec3 blockSurface = util.vector().blockSurface(BlockPos.containing(tubes.getCenter()), Direction.DOWN)
                .add(0, 1, 0);

        scene.overlay().showControls(blockSurface, Pointing.DOWN, 30).withItem(new ItemStack(Items.WATER_BUCKET));
        scene.idle(10);
        FluidStack content = new FluidStack(Fluids.WATER
                .getSource(), 1000);
        scene.world().modifyBlockEntity(source, ThermalOpticalSourceBlockEntity.class,
                be -> be.internalTank.getPrimaryHandler().fill(content, FluidAction.EXECUTE));
        scene.idle(10);
        scene.effects().indicateSuccess(source);
        scene.idle(10);

        scene.world().setKineticSpeed(receptorSystemSelect, 32);

        scene.world().modifyBlockEntityNBT(util.select().position(gaugePos), StressGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .5f));
        scene.effects().indicateRedstone(gaugePos);

        scene.idle(10);
        scene.overlay().showText(40)
                .text("Once filled with some fluid these sources begin to emmit light.")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(source, Direction.UP));
        scene.idle(60);
        scene.idle(10);
        scene.overlay().showText(40)
                .text("Unlike normal sources, these have double kinetic stress impact")
                .placeNearTarget()
                .independent(10)
                .colored(PonderPalette.BLUE);
        scene.idle(30);
        scene.overlay().showText(40)
                .text("This means double intensity!")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(gaugePos, Direction.UP))
                .colored(PonderPalette.RED);
        scene.idle(60);

        scene.world().modifyBlockEntity(source, ThermalOpticalSourceBlockEntity.class,
                be -> be.internalTank.getPrimaryHandler().drain(1000, FluidAction.EXECUTE));
        scene.idle(20);

        scene.world().setKineticSpeed(receptorSystemSelect, 0);
        scene.world().modifyBlockEntityNBT(util.select().position(gaugePos), StressGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", 0f));

        scene.idle(20);

        scene.overlay().showControls(blockSurface, Pointing.DOWN, 30).withItem(new ItemStack(Items.LAVA_BUCKET));
        scene.idle(10);
        FluidStack content1 = new FluidStack(Fluids.LAVA
                .getSource(), 1000);
        scene.world().modifyBlockEntity(source, ThermalOpticalSourceBlockEntity.class,
                be -> be.internalTank.getPrimaryHandler().fill(content1, FluidAction.EXECUTE));
        scene.idle(10);
        scene.effects().indicateSuccess(source);
        scene.idle(10);

        scene.world().setKineticSpeed(receptorSystemSelect, 32);
        scene.world().modifyBlockEntityNBT(util.select().position(gaugePos), StressGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .9f));
        scene.effects().indicateRedstone(gaugePos);

        scene.idle(10);

        scene.overlay().showText(40)
                .text("Lava grants double intensity comparing to water.")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(gaugePos, Direction.UP));
        scene.idle(60);
        scene.overlay().showText(60)
                .text("If you want to transport big stress capacity, there is the way you can do it.")
                .colored(PonderPalette.BLUE)
                .independent();
        scene.idle(70);
        scene.idle(10);

    }

    public static void reader(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.reader", "Reading beam properties");
        scene.configureBasePlate(0, 0, 5);

        BlockPos source = util.grid().at(4, 1, 2);
        BlockPos reader = util.grid().at(2, 1, 2);
        BlockPos dLink = util.grid().at(2, 1, 3);
        BlockPos glass = util.grid().at(0, 1, 2);
        Selection sourceSystemSelect = util.select().fromTo(4, 1, 2, 5, 1, 2);
        Selection bigWheelSelect = util.select().position(5, 0, 1).add(util.select().position(3, 0, 5));

        Selection tubesSelection = util.select().fromTo(4, 1, 4, 4, 1, 4);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(sourceSystemSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(reader), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(glass), Direction.DOWN);
        scene.idle(20);

        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 0);
        scene.idle(10);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 64);
        scene.effects().indicateSuccess(source);
        addSourceSection(scene, util, source, source, reader, Direction.EAST);
        addSourceSection(scene, util, source, reader, glass, Direction.EAST);
        scene.idle(20);

        scene.overlay().showControls(reader.getCenter(), Pointing.DOWN, 60).withItem(AllItems.GOGGLES.asStack());
        scene.idle(10);
        scene.overlay().showText(60)
                .attachKeyFrame()
                .placeNearTarget()
                .text("Using goggles you can analyze the properties of the incident beam")
                .pointAt(util.vector().blockSurface(reader, Direction.DOWN));
        scene.idle(80);
        scene.idle(5);
        scene.world().showSection(util.select().position(dLink), Direction.NORTH);
        scene.idle(5);
        scene.world().showSection(tubesSelection, Direction.DOWN);
        scene.idle(5);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("You can display the properties using a display link")
                .pointAt(util.vector().blockSurface(dLink, Direction.DOWN));

        scene.idle(5);
        scene.world().modifyBlockEntityNBT(tubesSelection, NixieTubeBlockEntity.class,
                nbt -> {
                    nbt.putString("RawCustomText", "16iu");
                    nbt.putString("CustomText", "16iu");
                });
        scene.effects().indicateRedstone(util.grid().at(4, 1, 4));
        scene.effects().indicateRedstone(util.grid().at(3, 1, 4));
        scene.idle(75);

    }

    public static void modulator(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("opticals.modulator", "Communicating by light");
        scene.configureBasePlate(0, 0, 5);

        BlockPos source = util.grid().at(4, 1, 0);
        BlockPos modulator = util.grid().at(3, 1, 0);
        BlockPos reader = util.grid().at(1, 1, 0);
        BlockPos dLink = util.grid().at(1, 1, 1);
        BlockPos glass = util.grid().at(0, 1, 0);
        Selection sourceSystemSelect = util.select().fromTo(4, 1, 0, 5, 1, 0);
        Selection bigWheelSelect = util.select().position(5, 0, 1);

        BlockPos boardPos = util.grid().at(2, 2, 4);
        Selection board = util.select().fromTo(0, 1, 4, 2, 2, 4);
        Selection boardWheel = util.select().fromTo(3, 1, 4, 3, 1, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(sourceSystemSelect, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(modulator), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(glass), Direction.DOWN);
        scene.idle(20);

        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 0);
        scene.idle(10);
        changeSpeed(scene, sourceSystemSelect, bigWheelSelect, 64);
        scene.effects().indicateSuccess(source);
        addSourceSection(scene, util, source, source, modulator, Direction.EAST);
        addSourceSection(scene, util, source, modulator, reader, Direction.EAST);
        addSourceSection(scene, util, source, reader, glass, Direction.EAST);
        scene.idle(10);
        changeSpeed(scene, boardWheel, bigWheelSelect, 64);
        scene.idle(40);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .placeNearTarget()
                .text("Using the modulator, you can communicate from one point to another...")
                .pointAt(util.vector().blockSurface(modulator, Direction.DOWN));
        scene.idle(80);
        scene.overlay().showControls(modulator.getCenter(), Pointing.DOWN, 60).withItem(AllBlocks.CLIPBOARD.asStack());
        scene.idle(10);

        scene.overlay().showText(60)
                .placeNearTarget()
                .text("Just write some text on a clipboard and apply it to the component")
                .pointAt(util.vector().blockSurface(modulator, Direction.DOWN));
        scene.idle(80);
        scene.idle(5);
        scene.world().showSection(util.select().position(reader), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(dLink), Direction.NORTH);
        scene.idle(5);
        scene.world().showSection(board, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(boardWheel, Direction.DOWN);
        scene.idle(5);
        changeSpeed(scene, boardWheel, board, 64);
        scene.idle(10);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("To receive the signal you'll need a beam reader and a display...")
                .pointAt(util.vector().blockSurface(dLink, Direction.DOWN));

        scene.idle(80);
        scene.overlay().showText(60)
                .attachKeyFrame()
                .independent()
                .colored(PonderPalette.GREEN)
                .text("But this requires both components to be in tune");
        scene.overlay().showControls(modulator.getCenter(), Pointing.DOWN, 60).rightClick();
        scene.idle(10);
        scene.overlay().showControls(reader.getCenter(), Pointing.DOWN, 60).rightClick()
                .withItem(AllItems.WRENCH.asStack());

        scene.idle(10);
        scene.world().flashDisplayLink(dLink);

        scene.world().setDisplayBoardText(boardPos, 0, Component.literal("Create"));
        scene.world().setDisplayBoardText(boardPos, 1, Component.literal("Optical :)"));
        scene.world().setDisplayBoardText(boardPos, 3, Component.literal(" - lpcamors"));

        scene.idle(60);

    }

    public static void changeSpeed(CreateSceneBuilder scene, Selection laser, Selection bigWheel, int speed) {
        scene.world().setKineticSpeed(laser, speed);
        scene.world().setKineticSpeed(bigWheel, (int) (speed * (-0.5)));
    }

    public static void addSensorToReceptor(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos receptorPos,
            Direction dir) {
        Map<Direction, ItemStack> map = new EnumMap<>(Direction.class);

        map.put(dir, COItems.OPTICAL_DEVICE.asStack());
        scene.world().modifyBlockEntityNBT(util.select().position(receptorPos), OpticalReceptorBlockEntity.class,
                nbt -> {
                    OpticalReceptorBlockEntity.writeSensors(nbt, scene.world().getHolderLookupProvider(), map);
                });
    }

    public static void addAllSensorToReceptor(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos receptorPos) {
        Map<Direction, ItemStack> map = new EnumMap<>(Direction.class);

        for (Direction dir : Direction.values()) {
            map.put(dir, COItems.OPTICAL_DEVICE.asStack());
        }
        scene.world().modifyBlockEntityNBT(util.select().position(receptorPos), OpticalReceptorBlockEntity.class,
                nbt -> {
                    OpticalReceptorBlockEntity.writeSensors(nbt, scene.world().getHolderLookupProvider(), map);
                });

    }

    public static void addSourceSection(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos source,
            BlockPos fromPos, BlockPos toPos,
            Direction dir) {
        addSourceSection(scene, util, source, fromPos, toPos, dir, a -> {
        });
    }

    public static void addSourceSection(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos source,
            BlockPos fromPos, BlockPos toPos,
            Direction dir, Consumer<BeamProperties.Builder> consumer) {

        scene.world().modifyBlockEntity(source, GenericOpticalSourceBlockEntity.class, be -> {
            BeamProperties.Builder prop = new BeamProperties.Builder(be.getInitialBeamProperties()).direction(dir);
            consumer.accept(prop);
            be.sections.add(new BeamSection(fromPos, toPos, prop.build()));
            be.sendData();
        });
    }

    public static void clearSourceSection(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos source) {
        scene.world().modifyBlockEntity(source, GenericOpticalSourceBlockEntity.class, be -> {
            be.sections.clear();
            be.sendData();
        });
    }

    public static void removeLastSourceSection(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos source) {
        scene.world().modifyBlockEntity(source, GenericOpticalSourceBlockEntity.class, be -> {
            if (be.sections.isEmpty())
                return;
            be.sections.remove(be.sections.size() - 1);
            be.sendData();
        });
    }

    public static void hologramActivation(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos pos,
            @Nullable BeamProperties prop) {
        scene.world().modifyBlockEntity(pos, HologramSourceBlockEntity.class,
                be -> {
                    be.updateChain();
                    if (prop == null) {
                        be.removeBeam();
                    } else {
                        be.receiveBeam(prop, true);
                    }
                    be.sendData();
                });
    }

    public static void setHologramItem(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos pos, Item item,
            HologramSourceBlockEntity.Mode mode, int angle) {
        setHologramItem(scene, util, pos, new ItemStack(item), mode, angle);
    }

    public static void setHologramItem(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos pos, ItemStack item,
            HologramSourceBlockEntity.Mode mode, int angle) {
        scene.world().modifyBlockEntityNBT(util.select().position(pos), HologramSourceBlockEntity.class,
                nbt -> {
                    HologramSourceBlockEntity.HologramSourceProfile profile = new HologramSourceBlockEntity.HologramSourceProfile();
                    profile.stack = item;
                    profile.displayMode = mode;
                    profile.fixedAngle = angle;
                    profile.write(nbt, scene.world().getHolderLookupProvider());
                });
    }

    public static void setHologramMessage(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos pos,
            String message,
            HologramSourceBlockEntity.Mode mode, int angle) {
        scene.world().modifyBlockEntityNBT(util.select().position(pos), HologramSourceBlockEntity.class,
                nbt -> {
                    HologramSourceBlockEntity.HologramSourceProfile profile = new HologramSourceBlockEntity.HologramSourceProfile();
                    profile.addSection(0, message);
                    profile.displayMode = mode;
                    profile.fixedAngle = angle;
                    profile.write(nbt, scene.world().getHolderLookupProvider());
                });
    }

    public static void activateFocuser(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos pos,
            boolean activate) {

        scene.world().modifyBlockEntity(pos, BeamFocuserBlockEntity.class,
                be -> {
                    if (activate) {
                        be.receiveBeam(BeamProperties.BASE, true);
                        be.processingTicks = be.getProcessDuration() + 5;
                    } else {

                        be.removeBeam();
                        be.processingTicks = 8;
                    }
                    be.update();
                });
    }
}
