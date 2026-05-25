package net.lpcamors.optical.network;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.lpcamors.optical.CreateOptical;
import net.lpcamors.optical.blocks.hologram_source.HologramSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class ConfigureHologramSourcePacket extends BlockEntityConfigurationPacket<HologramSourceBlockEntity> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureHologramSourcePacket> STREAM_CODEC = StreamCodec
            .composite(
                    BlockPos.STREAM_CODEC, packet -> packet.pos,
                    ByteBufCodecs.INT, a -> a.mode,
                    ByteBufCodecs.INT, a -> a.angle,
                    ByteBufCodecs.INT, a -> a.angleVelocity,
                    ConfigureHologramSourcePacket::new);

    private final int mode, angle, angleVelocity;

    public ConfigureHologramSourcePacket(BlockPos pos, int mode, int angle, int angleVelocity) {
        super(pos);
        this.mode = mode;
        this.angle = angle;
        this.angleVelocity = angleVelocity;
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return COPackets.CONFIGURE_HOLOGRAM;
    }

    @Override
    protected void applySettings(ServerPlayer player, HologramSourceBlockEntity be) {
        try {
            HologramSourceBlockEntity controller = be.getController();
            controller.getProfile().displayMode = HologramSourceBlockEntity.Mode.values()[this.mode
                    % HologramSourceBlockEntity.Mode.values().length];
            controller.getProfile().fixedAngle = this.angle;
            controller.getProfile().angleVelocity = this.angleVelocity;
            be.sendData();
        } catch (Exception ex) {
            CreateOptical.LOGGER.error("Unable to send data to server in " + be.toString());
        }

    }
}
