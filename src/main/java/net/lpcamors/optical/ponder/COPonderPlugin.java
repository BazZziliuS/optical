package net.lpcamors.optical.ponder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.lpcamors.optical.CreateOptical;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class COPonderPlugin implements PonderPlugin {
    @Override
    public @NotNull String getModId() {
        return CreateOptical.ID;
    }

    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        COPonderScenes.register(helper);
    }

    @Override
    public void registerTags(@NotNull PonderTagRegistrationHelper<ResourceLocation> helper) {
        COPonderTags.register(helper);
    }
}
