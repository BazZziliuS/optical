package net.lpcamors.optical.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.lpcamors.optical.COPartialModels;
import net.lpcamors.optical.CORenderTypes;
import net.lpcamors.optical.blocks.polarizing_beam_splitter_block.PolarizingBeamSplitterBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PolarizingBeamSplitterRenderer extends SafeBlockEntityRenderer<PolarizingBeamSplitterBlockEntity> {
    public PolarizingBeamSplitterRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public boolean shouldRender(PolarizingBeamSplitterBlockEntity p_173568_, Vec3 p_173569_) {
        return super.shouldRender(p_173568_,p_173569_);
    }

    @Override
    protected void renderSafe(PolarizingBeamSplitterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {

        BlockState state = be.getBlockState();
        SuperByteBuffer cube = CachedBuffers.partial(COPartialModels.POLARIZING_BEAM_SPLITTER, state).light(light);
        cube.renderInto(ms, bufferSource.getBuffer(CORenderTypes.TRANSLUCENT_NO_CULL));    
    }
    

}
