package net.lpcamors.optical.config;


import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.simibubi.create.api.stress.BlockStressValues;

import org.apache.commons.lang3.tuple.Pair;

import net.createmod.catnip.config.ConfigBase;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber
public class COConfigs {
    private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

    private static COCServer server;
    private static COCClient client;

    public static COCServer server(){
        return server;
    }
    public static COCClient client(){
        return client;
    }
    private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
		Pair<T, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(builder -> {
			T config = factory.get();
			config.registerAll(builder);
			return config;
		});

		T config = specPair.getLeft();
		config.specification = specPair.getRight();
		CONFIGS.put(side, config);
		return config;
	}

	public static void register(ModLoadingContext context, ModContainer container) {
		client = register(COCClient::new, ModConfig.Type.CLIENT);
		server = register(COCServer::new, ModConfig.Type.SERVER);

		for (Entry<ModConfig.Type, ConfigBase> pair : CONFIGS.entrySet())
			container.registerConfig(pair.getKey(), pair.getValue().specification);

		COCStress stress = server().kinetics.stressValues;
		BlockStressValues.IMPACTS.registerProvider(stress::getImpact);
		BlockStressValues.CAPACITIES.registerProvider(stress::getCapacity);
	}

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        for (ConfigBase config : CONFIGS.values())
            if (config.specification == event.getConfig()
                    .getSpec())
                config.onLoad();
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        for (ConfigBase config : CONFIGS.values())
            if (config.specification == event.getConfig()
                    .getSpec())
                config.onReload();
    }


}

