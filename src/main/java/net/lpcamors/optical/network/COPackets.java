package net.lpcamors.optical.network;

import java.util.Locale;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.lpcamors.optical.CreateOptical;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public enum COPackets implements BasePacketPayload.PacketTypeProvider {


    CONFIGURE_HOLOGRAM(ConfigureHologramSourcePacket.class, ConfigureHologramSourcePacket.STREAM_CODEC),
    CONFIGURE_OPTICAL_SOURCE(ConfigureOpticalSourcePacket.class, ConfigureOpticalSourcePacket.STREAM_CODEC);


	private final CatnipPacketRegistry.PacketType<?> type;

	<T extends BasePacketPayload> COPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		String name = this.name().toLowerCase(Locale.ROOT);
		this.type = new CatnipPacketRegistry.PacketType<>(
				new CustomPacketPayload.Type<>(CreateOptical.loc(name)),
				clazz, codec
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
		return (CustomPacketPayload.Type<T>) this.type.type();
	}

	public static void register() {
		CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(CreateOptical.ID, CreateOptical.VERSION);
		for (COPackets packet : COPackets.values()) {
			packetRegistry.registerPacket(packet.type);
		}
		packetRegistry.registerAllPackets();
	}
}
