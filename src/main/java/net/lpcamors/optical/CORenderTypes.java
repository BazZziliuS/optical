package net.lpcamors.optical;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import org.joml.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class CORenderTypes {
    public static final RenderStateShard.TextureStateShard HOLOGRAM_GLINT_TEXTURE = new RenderStateShard.TextureStateShard(
            ResourceLocation.fromNamespaceAndPath(CreateOptical.ID,
                    "textures/block/hologram_source/glint.png"),
            true, false);
    public static final RenderStateShard.TexturingStateShard SCROLLING_TEXTURE = new RenderStateShard.TexturingStateShard(
            "scrolling_texture",
            () -> {
                float t = (Minecraft.getInstance().level.getGameTime() % 3000) / 3000f;
                RenderSystem.setTextureMatrix(new Matrix4f().translation(t, t, 0));
            },
            () -> RenderSystem.resetTextureMatrix());

    public static final RenderType TRANSLUCENT_NO_CULL = RenderType.create(
            "translucent_no_cull",
            DefaultVertexFormat.BLOCK,
            Mode.QUADS,
            256,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(true));

    public static final RenderType TRANSPARENT_ADDITIVE = RenderType.create("additive_transparent",
            DefaultVertexFormat.BLOCK,
            Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(RenderStateShard.BLOCK_SHEET)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .createCompositeState(true));

    public static final RenderType HOLOGRAM = RenderType.create("hologram",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
                    .setTextureState(RenderStateShard.BLOCK_SHEET)
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .createCompositeState(true));

    public static final RenderType HOLOGRAM_INNER = RenderType.create("hologram_inner",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
                    .setTextureState(RenderStateShard.BLOCK_SHEET)
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .createCompositeState(true));

    public static final RenderType HOLOGRAM_SCAN = RenderType.create(
            "hologram_scan",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            256,
            true, // needs sorting
            false, // no culling
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(HOLOGRAM_GLINT_TEXTURE)
                    .setTransparencyState(RenderType.LIGHTNING_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(true));
}
