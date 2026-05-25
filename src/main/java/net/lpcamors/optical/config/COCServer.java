package net.lpcamors.optical.config;

import net.createmod.catnip.config.ConfigBase;

public class COCServer extends ConfigBase {

    public final COCRecipes recipes = nested(0, COCRecipes::new, COCServer.Comments.recipes);

    public final COCKinetics kinetics = nested(0, COCKinetics::new, COCServer.Comments.kinetics);

    @Override
    public String getName() {
        return "optical-server";
    }

    private static class Comments {
        static String recipes = "Packmakers' control panel for internal optical recipe compat";
        static String kinetics = "Parameters and abilities of Optical's kinetic components";
    }

}
