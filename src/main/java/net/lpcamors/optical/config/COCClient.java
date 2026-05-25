package net.lpcamors.optical.config;

import net.createmod.catnip.config.ConfigBase;

public class COCClient extends ConfigBase {

    public final COCHologram hologramDisplay = nested(0, COCHologram::new, Comments.hologram);

    @Override
    public String getName() {
        return "optical-client";
    }

    private static class Comments {
        static String hologram = "Change some parameters of the hologram display.";
    }
}
