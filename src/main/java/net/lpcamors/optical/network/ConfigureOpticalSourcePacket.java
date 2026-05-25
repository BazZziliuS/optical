package net.lpcamors.optical.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.foundation.networking.BlockEntityDataPacket;

import io.netty.buffer.ByteBuf;
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

    private static final StreamCodec<ByteBuf, List<BeamSection>> SECTIONS_CODEC =
            CatnipStreamCodecBuilders.list(BeamSection.CODEC);
    private static final StreamCodec<ByteBuf, Map<BlockPos, BeamHelper.BeamProperties>> ACTIVATORS_CODEC =
            ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, BeamHelper.PROPERTIES_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureOpticalSourcePacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ConfigureOpticalSourcePacket decode(RegistryFriendlyByteBuf buf) {
                    BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                    List<BeamSection> sections = SECTIONS_CODEC.decode(buf);
                    Map<BlockPos, BeamHelper.BeamProperties> activators =
                            buf.isReadable() ? ACTIVATORS_CODEC.decode(buf) : new HashMap<>();
                    return new ConfigureOpticalSourcePacket(pos, sections, activators);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ConfigureOpticalSourcePacket packet) {
                    BlockPos.STREAM_CODEC.encode(buf, packet.pos);
                    SECTIONS_CODEC.encode(buf, packet.sections);
                    ACTIVATORS_CODEC.encode(buf, packet.activators);
                }
            };

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
