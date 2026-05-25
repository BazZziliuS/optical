package net.lpcamors.optical.config;


import net.createmod.catnip.config.ConfigBase;

public class COCHologram extends ConfigBase {

    public final ConfigBase.ConfigFloat generalTransparency =
            f(1.0F, 0.0F, 1F, "generalTransparency", Comments.generalTransparency);

    public final ConfigBase.ConfigFloat normalTransparency =
            f(0.75F, 0.0F, 1F, "normalTransparency", Comments.normalTransparency);


    public final ConfigBase.ConfigFloat additiveTransparency =
            f(1.0F, 0.0F, 1F, "additiveTransparency", Comments.additiveTransparency);


    @Override
    public String getName() {
        return "optical-hologram";
    }

    public static class Comments {
        static String generalTransparency = "Regulate the transparency of the hologram in general.";
        static String normalTransparency = "Regulate the transparency of the non bright texture.";
        static String additiveTransparency = "Regulate the transparency of the bright texture.";
    }

}
