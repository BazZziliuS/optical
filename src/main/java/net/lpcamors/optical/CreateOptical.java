package net.lpcamors.optical;

import java.util.function.Function;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;

import org.slf4j.Logger;

import net.createmod.catnip.lang.FontHelper;
import net.lpcamors.optical.blocks.COBlockEntities;
import net.lpcamors.optical.blocks.COBlocks;
import net.lpcamors.optical.blocks.thermal_optical_source.ThermalOpticalSourceBlockEntity;
import net.lpcamors.optical.config.COConfigs;
import net.lpcamors.optical.data.CODataGen;
import net.lpcamors.optical.data.COLang;
import net.lpcamors.optical.items.COItems;
import net.lpcamors.optical.network.COPackets;
import net.lpcamors.optical.recipes.FocusingRecipeParams.BeamTypeConditionProfile;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(CreateOptical.ID)
public class CreateOptical {
    public static final String ID = "create_optical";
    public static final String VERSION = "0.1";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final Function<String, ResourceLocation> LOC_FUNC = s -> ResourceLocation.fromNamespaceAndPath(ID,
            s);

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

    static {
        REGISTRATE.setTooltipModifierFactory(
                item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                        .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    public static ResourceLocation loc(String name) {
        return LOC_FUNC.apply(name);
    }

    public CreateOptical(net.neoforged.bus.api.IEventBus modEventBus, ModContainer container) {

        REGISTRATE.registerEventListeners(modEventBus);

        IEventBus forgeEventBus = NeoForge.EVENT_BUS;
        
        COBlocks.initiate();
        COItems.initiate();
        COBlockEntities.initiate();
        COCreativeModeTabs.initiate(modEventBus);
        CODisplaySources.initiate();
        CORecipeTypes.register(modEventBus);
        COLang.initiate();

        COPackets.register();
        COConfigs.register(ModLoadingContext.get(), container);

        forgeEventBus.addListener(CreateOptical::onServerStarting);
        forgeEventBus.addListener(CreateOptical::onDatapackSync);
        modEventBus.addListener(EventPriority.HIGHEST, CODataGen::gatherDataHighPriority);
        modEventBus.addListener(EventPriority.LOWEST, CODataGen::gatherData);
    }

    @EventBusSubscriber
    public static class ModBusEvents {

        @net.neoforged.bus.api.SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {

            ThermalOpticalSourceBlockEntity.registerCapabilities(event);

        }
    }

    private static void onServerStarting(ServerStartingEvent event) {
        var server = event.getServer();
        BeamTypeConditionProfile.rebuild(server);
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null)
            return;
        var server = event.getPlayerList().getServer();
        BeamTypeConditionProfile.rebuild(server);
    }
}
