package net.lpcamors.optical;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.ponder.foundation.PonderIndex;
import net.lpcamors.optical.ponder.COPonderPlugin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = CreateOptical.ID, dist = Dist.CLIENT)
public class CreateOpticalClient {

    public CreateOpticalClient(IEventBus modEventBus) {
        onCtorClient(modEventBus);
    }

    public static void onCtorClient(IEventBus modEventBus) {
        modEventBus.addListener(CreateOpticalClient::clientInit);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        SuperByteBufferCache cache = SuperByteBufferCache.getInstance();

        cache.registerCompartment(CachedBuffers.PARTIAL);
        cache.registerCompartment(CachedBuffers.DIRECTIONAL_PARTIAL);

        COPartialModels.initiate();
        PonderIndex.addPlugin(new COPonderPlugin());
    }

}
