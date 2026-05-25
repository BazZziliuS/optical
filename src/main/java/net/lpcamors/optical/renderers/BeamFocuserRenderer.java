package net.lpcamors.optical.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.lpcamors.optical.COPartialModels;
import net.lpcamors.optical.CORenderTypes;
import net.lpcamors.optical.blocks.beam_focuser.BeamFocuserBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.lighting.LightEngine;

public class BeamFocuserRenderer extends ShaftRenderer<BeamFocuserBlockEntity> {

    private static final int CYCLE_TICK = 15;
    private static final float HEIGHT = 21 / 16F;

    public BeamFocuserRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BeamFocuserBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
            int light,
            int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);
        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            if (be.processingTicks >= 5 && be.getOptionalBeamProperties().isPresent()) {
                Vec3i color = be.getOptionalBeamProperties().get().color();
                ms.translate(0, 4 / 16f, 0);
                for (int i = 0; i < 2; i++) {
                    ms.pushPose();
                    float yOffset = getYOffset(be, partialTicks, i);
                    float scale = 1f + ((yOffset / 1.5f) / 3);
                    ms.translate(0, -yOffset, 0);
                    ms.translate(0.5, 0.5, 0.5);
                    ms.scale(scale, scale, scale);
                    ms.translate(-0.5, -0.5, -0.5);
                    CachedBuffers.partial(COPartialModels.FOCUS_BEAM, be.getBlockState())
                            .light(LightEngine.MAX_LEVEL).color(color.getX(), color.getY(), color.getZ(), 150)
                            .renderInto(ms, buffer.getBuffer(CORenderTypes.TRANSPARENT_ADDITIVE));
                    ms.translate(0, -0.01, 0);
                    CachedBuffers.partial(COPartialModels.FOCUS_BEAM, be.getBlockState())
                            .light(LightEngine.MAX_LEVEL / 2).color(color.getX(), color.getY(), color.getZ(), 255)
                            .renderInto(ms, buffer.getBuffer(CORenderTypes.TRANSPARENT_ADDITIVE));

                    ms.popPose();
                }
            }
        }
    }

    private float getYOffset(BeamFocuserBlockEntity be, float pt, int index) {
        int ticks = ((index * CYCLE_TICK / 2) + be.getProcessDuration() + 5
                - be.processingTicks) % CYCLE_TICK;
        return (ticks + pt) * (HEIGHT / (float) CYCLE_TICK);
    }
}
