package net.lpcamors.optical.renderers;

import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.theme.Color;
import net.lpcamors.optical.COPartialModels;
import net.lpcamors.optical.CORenderTypes;
import net.lpcamors.optical.COUtils;
import net.lpcamors.optical.blocks.hologram_source.HologramSourceBlock;
import net.lpcamors.optical.blocks.hologram_source.HologramSourceBlockEntity;
import net.lpcamors.optical.blocks.hologram_source.HologramSourceBlockEntity.HologramSourceProfile;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.config.COConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientHooks;

public class HologramSourceRenderer extends SafeBlockEntityRenderer<HologramSourceBlockEntity> {

    private static final Map<Item, ModelResourceLocation> CUSTOM_ITEM_MODEL = Map.of(
            Items.TRIDENT, ModelResourceLocation.inventory(ResourceLocation.withDefaultNamespace("trident")),
            Items.SPYGLASS, ModelResourceLocation.inventory(ResourceLocation.withDefaultNamespace("spyglass")));

    public HologramSourceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 512;
    }

    @Override
    protected void renderSafe(HologramSourceBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
            int light, int overlay) {

        if (!be.isActive())
            return;

        if (be.getController() == null || be.getController().getProfile() == null)
            return;

        if (be.getController().getProfile().hasMessages() || !be.getController().getProfile().stack.isEmpty()) {

            BeamHelper.BeamProperties beamProperties = be.getOptionalBeamProperties().get();
            Vec3i color = beamProperties.color();

            ms.pushPose();
            SuperByteBuffer cube = CachedBuffers.partial(COPartialModels.HOLOGRAM_BEAM, be.getBlockState()).center()
                    .light(LightTexture.FULL_BRIGHT)
                    .color(color.getX(), color.getY(), color.getZ(), 255);
            cube.uncenter().renderInto(ms, buffer.getBuffer(CORenderTypes.HOLOGRAM));
            ms.popPose();

            if (be.isController()) {
                if (be.getProfile().stack.isEmpty()) {
                    this.renderText(be, partialTicks, ms, buffer, light, overlay);
                } else {
                    this.renderItem(be.getProfile().stack, be, partialTicks, ms, buffer, light, overlay);
                }
            }
        }
    }

    public void renderText(HologramSourceBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource buffer,
            int light, int overlay) {
        HologramSourceProfile profile = be.getProfile();
        BlockState state = be.getBlockState();
        RandomSource random = be.getLevel().getRandom();

        List<String> messages = profile.getMessages();
        var ts = TransformStack.of(ms);

        Vec3i color = be.getOptionalBeamProperties().get().color(),
                colorvar = COUtils.meanPoint(color, COUtils.getColor(DyeColor.WHITE));
        Vec3 vec3 = be.getProjectionBox().getCenter().subtract(Vec3.atCenterOf(be.getBlockPos()));

        double sec = (be.getTickCount() + partialTicks) / 20f, rot;
        int n = profile.getMessages().size(),
                iColor = COUtils.colorInt(colorvar),
                ibgColor = COUtils.colorInt(color);
        iColor = Color.mixColors(iColor, ibgColor, random.nextFloat() / 5);

        float H = profile.getConnectionLength(),
                e = 0.4f,
                lineHeight = H / (n + ((n + 1) * e)),
                scale = lineHeight / 9f;

        if (be.hasFixedAngle()) {
            rot = profile.getFixedAngle();
        } else {
            rot = profile.getAngleVelocity() * sec;
            if (profile.getDisplayMode().equals(HologramSourceBlockEntity.Mode.ROTATING_CLOCKWISE)) {
                rot = -rot;
            }
        }

        ts.translate(vec3);
        ts.center().rotateY(AngleHelper.rad(rot)).uncenter();

        if (state.getValue(HologramSourceBlock.HORIZONTAL_AXIS).equals(Axis.X)) {
            ts.center().rotateY(AngleHelper.rad(90)).uncenter();
        } else {

            ts.center().rotateY(AngleHelper.rad(0)).uncenter();
        }

        for (int i = 0; i < n; i++) {
            String c = messages.get(i);
            float charWidth = Minecraft.getInstance().font.width(c);

            float y = (i - 0.0f + ((i + 1) * e)) * lineHeight;
            ts.pushPose();
            ts.translate(0, -y + (H / 2d), 0)
                    .center().scale(scale, -scale, scale)
                    .uncenter();
            ts.pushPose().translate(-charWidth / 2f, 0, 0);
            renderPane(ms, buffer, charWidth, 9f, color, i, be.getTickCount() + partialTicks);
            ts.translate(0, 0, 0.2);
            drawInWorldString(ms, buffer, c,
                    iColor, ibgColor);

            ts.popPose();

            ts.pushPose().rotateY(AngleHelper.rad(180))
                    .translate(-charWidth / 2f, 0, 0);
            drawInWorldString(ms, buffer, c, iColor, ibgColor);
            ts.popPose();
            ts.popPose();

        }

    }

    private static void renderPane(PoseStack ms, MultiBufferSource buffer, float width, float height, Vec3i color,
            int line, float ticks) {
        VertexConsumer vc = buffer.getBuffer(CORenderTypes.HOLOGRAM_SCAN);
        float e = 0.1f;
        float y0 = -e * (height / 2);
        float y1 = height * (1 + (e / 2));
        float x0 = -e * (width / 2);
        float x1 = width * (1 + (e / 2));
        float aspect = 0.2f;
        float speed = 0.002f;
        float timeOffset = ticks * speed;
        float lineOffset = line * 0.37f;
        float v0 = (timeOffset + lineOffset) % 1.0f;
        float v1 = v0 + aspect;
        int alpha = 175;
        PoseStack.Pose pose = ms.last();

        vc.addVertex(pose, x0, y0, 0)
                .setColor(color.getX(), color.getY(), color.getZ(), alpha)
                .setUv(0, v0).setUv2(15728880, 15728880)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        vc.addVertex(pose, x0, y1, 0)
                .setColor(color.getX(), color.getY(), color.getZ(), alpha)
                .setUv(0, v1).setUv2(15728880, 15728880)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        vc.addVertex(pose, x1, y1, 0)
                .setColor(color.getX(), color.getY(), color.getZ(), alpha)
                .setUv(1, v1).setUv2(15728880, 15728880)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        vc.addVertex(pose, x1, y0, 0)
                .setColor(color.getX(), color.getY(), color.getZ(), alpha)
                .setUv(1, v0).setUv2(15728880, 15728880)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    static void drawInWorldString(PoseStack ms, MultiBufferSource buffer, String c, int color, int bgColor) {
        Font fontRenderer = Minecraft.getInstance().font;
        fontRenderer.drawInBatch8xOutline(FormattedCharSequence.forward(c, Style.EMPTY), 0f, 1f, color, bgColor,
                ms.last().pose(), buffer, LightTexture.FULL_BRIGHT);
    }

    void renderItem(ItemStack heldItem, HologramSourceBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource buffer,
            int light, int overlay) {

        BeamHelper.BeamProperties beamProperties = be.getOptionalBeamProperties().get();
        ItemRenderer itemRenderer = Minecraft.getInstance()
                .getItemRenderer();
        HologramSourceProfile profile = be.getProfile();
        Vec3 vec3 = be.getProjectionBox().getCenter().subtract(Vec3.atLowerCornerOf(be.getBlockPos()));
        BakedModel bakedmodel = itemRenderer.getModel(heldItem, be.getLevel(), null, 0),
                bakedmodel1 = itemRenderer.getModel(heldItem, be.getLevel(), null, 0);
        if (bakedmodel.isCustomRenderer()) {
            heldItem = new ItemStack(Items.BARRIER);
            bakedmodel = itemRenderer.getModel(heldItem, be.getLevel(), null, 0);
            bakedmodel1 = itemRenderer.getModel(heldItem, be.getLevel(), null, 0);
        }
        var ts = TransformStack.of(ms);

        float normalAlpha = COConfigs.client().hologramDisplay.normalTransparency.getF(),
                additiveAlpha = COConfigs.client().hologramDisplay.additiveTransparency.getF();
        normalAlpha *= COConfigs.client().hologramDisplay.generalTransparency.getF();
        additiveAlpha *= COConfigs.client().hologramDisplay.generalTransparency.getF();

        boolean blockItem = itemRenderer.getModel(heldItem, null, null, 0)
                .isGui3d();
        double radius = (blockItem ? 1.3 : 0.7),
                sec = (be.getTickCount() + partialTicks) / 20f,
                rot;

        if (heldItem.is(Items.TRIDENT) || heldItem.is(Items.SPYGLASS))
            radius *= 0.5;

        if (be.hasFixedAngle()) {
            rot = profile.getFixedAngle();
        } else {
            rot = profile.getAngleVelocity() * sec;
            if (profile.getDisplayMode().equals(HologramSourceBlockEntity.Mode.ROTATING_CLOCKWISE)) {
                rot = -rot;
            }
        }

        ms.pushPose();
        ts.translate(vec3).scale((float) (be.getProfile().getConnectionLength() * radius));
        this.render(beamProperties, itemRenderer, heldItem, ItemDisplayContext.FIXED, false, ms, buffer,
                LightTexture.FULL_BRIGHT, overlay, bakedmodel,
                buffer.getBuffer(CORenderTypes.HOLOGRAM_INNER), normalAlpha, AngleHelper.rad(rot));

        ms.popPose();

        ms.pushPose();
        ts.translate(vec3).scale((float) (be.getProfile().getConnectionLength() * radius * 1.1));
        this.render(beamProperties, itemRenderer, heldItem, ItemDisplayContext.FIXED, false, ms, buffer,
                LightTexture.FULL_BRIGHT, overlay, bakedmodel1,
                buffer.getBuffer(CORenderTypes.HOLOGRAM), additiveAlpha, AngleHelper.rad(rot));
        ms.popPose();

    }

    void render(BeamHelper.BeamProperties beamProperties, ItemRenderer renderer, ItemStack itemStack,
            ItemDisplayContext itemDisplayContext, boolean p_115146_, PoseStack ms, MultiBufferSource buffer, int light,
            int overlay,
            BakedModel bakedModel, VertexConsumer vertexConsumer, float alpha, double rotation) {
        if (!itemStack.isEmpty()) {
            ms.pushPose();

            if (CUSTOM_ITEM_MODEL.containsKey(itemStack.getItem())) {
                bakedModel = renderer.getItemModelShaper().getModelManager()
                        .getModel(CUSTOM_ITEM_MODEL.get(itemStack.getItem()));
            }

            bakedModel = ClientHooks.handleCameraTransforms(ms, bakedModel, itemDisplayContext, p_115146_);
            ms.translate(-0.5F, -0.5F, -0.5F);

            TransformStack.of(ms)
                    .rotateCentered((float) rotation, Direction.UP);
            boolean flag1;
            if (itemDisplayContext != ItemDisplayContext.GUI && !itemDisplayContext.firstPerson()
                    && itemStack.getItem() instanceof BlockItem) {
                Block block = ((BlockItem) itemStack.getItem()).getBlock();
                flag1 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
            } else {
                flag1 = true;
            }

            for (BakedModel model : bakedModel.getRenderPasses(itemStack, flag1)) {

                this.renderModelLists(
                        beamProperties,
                        renderer,
                        model,
                        itemStack,
                        light,
                        overlay,
                        ms,
                        vertexConsumer,
                        alpha);
            }
            ms.popPose();
        }
    }

    void renderModelLists(BeamHelper.BeamProperties beamProperties, ItemRenderer renderer, BakedModel model,
            ItemStack stack, int light, int overlay, PoseStack pose, VertexConsumer vc,
            float alpha) {
        RandomSource randomsource = RandomSource.create();

        float r = randomsource.nextFloat();
        for (Direction direction : Direction.values()) {
            randomsource.setSeed(42L);
            this.renderQuadList(beamProperties, pose, vc,
                    model.getQuads((BlockState) null, direction, randomsource), stack, light, overlay,
                    alpha, r);
        }

        randomsource.setSeed(42L);
        this.renderQuadList(beamProperties, pose, vc,
                model.getQuads((BlockState) null, (Direction) null, randomsource), stack, light, overlay,
                alpha, r);
    }

    void renderQuadList(BeamHelper.BeamProperties beamProperties, PoseStack pose, VertexConsumer vc,
            List<BakedQuad> quads, ItemStack stack, int light, int overlay, float alpha,
            float randFloat) {
        PoseStack.Pose posestack$pose = pose.last();
        Vec3i color = beamProperties.color(), colorvar = COUtils.meanPoint(color, COUtils.getColor(DyeColor.WHITE));
        color = COUtils.colorVec(
                Color.mixColors(COUtils.colorInt(color), COUtils.colorInt(colorvar),
                        randFloat / 5));
        float d = 1.5f;
        d = 1f;
        for (BakedQuad bakedquad : quads) {
            vc.putBulkData(posestack$pose, bakedquad, (float) (color.getX() * d / 255D),
                    (float) (color.getY() * d / 255D), (float) (color.getZ() * d / 255D), alpha, light, overlay,
                    false);
        }

    }

}
