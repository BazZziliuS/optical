package net.lpcamors.optical.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.foundation.networking.BlockEntityDataPacket;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity.BeamSection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ConfigureOpticalSourcePacket extends BlockEntityDataPacket<GenericOpticalSourceBlockEntity> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureOpticalSourcePacket> STREAM_CODEC = StreamCodec
            .composite(
                    BlockPos.STREAM_CODEC, packet -> packet.pos,
                    CatnipStreamCodecBuilders.list(BeamSection.CODEC), p -> p.sections,
                    ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, BeamHelper.PROPERTIES_CODEC),
                    packet -> packet.activators,
                    ConfigureOpticalSourcePacket::new);

    public final BlockPos pos;
    public final List<BeamSection> sections;

    public final Map<BlockPos, BeamProperties> activators;
    public ConfigureOpticalSourcePacket(GenericOpticalSourceBlockEntity be) {
        this(be.getBlockPos(), List.copyOf(be.sections), Map.copyOf(be.activators));
    }

    public ConfigureOpticalSourcePacket(BlockPos pos, List<BeamSection> sections,
            Map<BlockPos, BeamProperties> activators) {
        super(pos);
        this.pos = pos;
        this.sections = sections;
        this.activators = activators;
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return COPackets.CONFIGURE_OPTICAL_SOURCE;
    }

    @Override
    protected void handlePacket(GenericOpticalSourceBlockEntity blockEntity) {
        blockEntity.sections.clear();
        blockEntity.activators.clear();
        blockEntity.sections.addAll(this.sections);
        blockEntity.activators.putAll(this.activators);
        blockEntity.updateSections();
    }

}
