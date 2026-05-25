package net.lpcamors.optical.recipes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;

import org.jetbrains.annotations.NotNull;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.lpcamors.optical.COPartialModels;
import net.lpcamors.optical.blocks.COBlocks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;

public class AnimatedFocus extends AnimatedKinetics {

    private static final int CYCLE_TICK = 15;
    private static final float HEIGHT = 21 / 16F;

    private final boolean depot;

    public AnimatedFocus(boolean depot) {
        this.depot = depot;
    }

    @Override
    public void draw(@NotNull GuiGraphics graphics, int xOffset, int yOffset) {
        float tic = AnimationTickHolder.getRenderTime();
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 24;

        blockElement(shaft(Direction.Axis.Z))
                .rotateBlock(0, 0, getCurrentAngle())
                .scale(scale)
                .render(graphics);

        GuiGameElement.GuiRenderBuilder r = GuiGameElement.of(COPartialModels.FOCUS_BEAM)
                .scale(scale)
                .rotateBlock(0, 90, 0)
                .atLocal(0, -0.1 + getYOffset(tic, 0), 0)
                .withAlpha(0.11F);
        r.render(graphics);

        GuiGameElement.GuiRenderBuilder r1 = GuiGameElement.of(COPartialModels.FOCUS_BEAM)
                .scale(scale)
                .rotateBlock(0, 90, 0)
                .atLocal(0, -0.1 + getYOffset(tic, 1), 0)
                .withAlpha(0.11F);
        r1.render(graphics);

        blockElement(COBlocks.BEAM_FOCUSER.getDefaultState())
                .scale(scale)
                .render(graphics);

        if (depot) {
            blockElement(AllBlocks.DEPOT.getDefaultState())
                    .atLocal(0, 1.65, 0)
                    .scale(scale)
                    .render(graphics);
        }

        matrixStack.popPose();
    }

    private float getYOffset(float tick, int index) {
        
        float pt = tick - (int) tick;
        int ticks = ((index * CYCLE_TICK / 2) + (int) tick) % CYCLE_TICK;
        return (ticks + pt) * (HEIGHT / (float) CYCLE_TICK);
    }
}
