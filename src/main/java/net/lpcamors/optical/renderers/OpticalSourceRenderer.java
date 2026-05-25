package net.lpcamors.optical.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.lpcamors.optical.COPartialModels;
import net.lpcamors.optical.CORenderTypes;
import net.lpcamors.optical.COUtils;
import net.lpcamors.optical.blocks.IBeamReceiver;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity.BeamSection;
import net.lpcamors.optical.blocks.optical_source.OpticalSourceBlock;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class OpticalSourceRenderer extends KineticBlockEntityRenderer<GenericOpticalSourceBlockEntity> {

    public OpticalSourceRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRenderOffScreen(GenericOpticalSourceBlockEntity be) {
        return be.shouldRenderBeam();
    }

    @Override
    protected void renderSafe(GenericOpticalSourceBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        if (be.shouldRenderBeam()) {
            renderLaserBeam(be, be.getBlockState(), ms, buffer);
        }

    }

    public static void renderLaserBeam(GenericOpticalSourceBlockEntity be, BlockState state, PoseStack ms,
            MultiBufferSource buffer) {

        Vec3 pos = be.getBlockPos().getCenter();

        Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
        for (BeamSection section : be.sections) {
            Pair<Vec3i, Vec3i> pair = new Pair<>(section.fromPos(), section.toPos());

            BeamHelper.BeamProperties beamProperties = section.beamProperties();
            Vec3i color = beamProperties.color();
            direction = beamProperties.direction();

            int jMax = be.getBeamRadiusCount(),
                    jRest = Math.max(0, jMax - 10);
            Vec3 start0 = Vec3.atCenterOf(pair.getFirst()),
                    start = start0.subtract(IBeamReceiver.getLaserIrradiatedFaceOffsetVar(beamProperties.direction(),
                            new BlockPos(pair.getFirst()), be.getLevel())),
                    end0 = Vec3.atCenterOf(pair.getSecond()),
                    end = end0.add(IBeamReceiver.getLaserIrradiatedFaceOffsetVar(beamProperties.direction(),
                            new BlockPos(pair.getSecond()), be.getLevel())),
                    dir = COUtils.getNormalUnitary(direction), nDir = COUtils.vecOf(1).subtract(dir);
            ;

            ms.pushPose();

            translateForVec(ms, start0.subtract(pos));
            translateForVec(ms, start.subtract(start0));
            translateForVec(ms, end.subtract(start).multiply(0.5D, 0.5D, 0.5D));

            float length = (float) end.subtract(start).length();

            if (COPartialModels.LASER_BEAM.get() == null) {
                ms.popPose();
                continue;
            }

            SuperByteBuffer laser = CachedBuffers.partial(COPartialModels.LASER_BEAM, state)
                    .light(LightTexture.FULL_BRIGHT)
                    .disableDiffuse();

            for (int j = 0; j < jMax; j++) {
                SuperByteBuffer laser0 = laser;
                double radius = 0.8 + (j + jRest) * 0.2;
                int alpha = (int) (155 * (1 - j / 10F));
                laser0.color(color.getX(), color.getY(), color.getZ(), alpha);
                scaleForVec(laser0, dir.scale(length).add(nDir));
                scaleForVec(laser0, nDir.scale(radius).add(dir));
                rotateDirection(laser0, direction);
                laser0.renderInto(ms, buffer.getBuffer(CORenderTypes.TRANSPARENT_ADDITIVE)
                        .setLight((int) (LightTexture.FULL_BRIGHT)));

            }
            ms.popPose();
        }
    }

    static void translateForVec(PoseStack ms, Vec3 vec3) {
        ms.translate(vec3.x, vec3.y, vec3.z);
    }

    static void scaleForVec(SuperByteBuffer s, Vec3 vec3) {
        s.center().scale((float) vec3.x, (float) vec3.y, (float) vec3.z).uncenter();
    }

    static void rotateDirection(SuperByteBuffer buffer, Direction direction) {
        float yRot = (float) (AngleHelper.horizontalAngle(direction) * Math.PI / 180f);
        float xRot = direction.getStepY() * (float) Math.PI / 2F;
        buffer.rotateCentered(yRot, Direction.UP);
        buffer.rotateCentered(xRot, Direction.EAST);

    }

    @Override
    public boolean shouldRender(GenericOpticalSourceBlockEntity p_173568_, Vec3 p_173569_) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    protected SuperByteBuffer getRotatedModel(GenericOpticalSourceBlockEntity opticalLaserSourceBlockEntity,
            BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, state
                .getValue(OpticalSourceBlock.HORIZONTAL_FACING)
                .getOpposite());
    }
}
