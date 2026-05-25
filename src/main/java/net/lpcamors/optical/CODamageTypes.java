package net.lpcamors.optical;

import com.simibubi.create.foundation.damageTypes.DamageTypeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public class CODamageTypes {

    public static final ResourceKey<DamageType>
        GAMMA_RAY = key("gamma_ray");

    public static void bootstrap(BootstrapContext<DamageType> ctx) {
        new DamageTypeBuilder(GAMMA_RAY).scaling(DamageScaling.ALWAYS).register(ctx);

    }
    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(CreateOptical.ID, name));
    }
}
