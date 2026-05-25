package net.lpcamors.optical.config;

import net.createmod.catnip.config.ConfigBase;

public class COCKinetics extends ConfigBase {
    public final COCStress stressValues = nested(1, COCStress::new, COCKinetics.Comments.stress);

    @Override
    public String getName() {
        return "optical-kinetics";
    }
    private static class Comments{
        private static final String stress = "Fine tune the kinetic stats of individual components";

    }
}
