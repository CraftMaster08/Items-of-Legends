package net.craftmaster08.itemsoflegends.network;

import net.craftmaster08.itemsoflegends.ItemsOfLegends;
import net.craftmaster08.itemsoflegends.network.packet.C2SSaveRealityFractureConfigPacket;
import net.craftmaster08.itemsoflegends.network.packet.S2COpenRealityFractureConfigPacket;
import net.craftmaster08.itemsoflegends.network.packet.S2CSyncRealityFractureStatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ItemsOfLegends.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static boolean registered = false;

    private ModNetwork() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        CHANNEL.messageBuilder(S2COpenRealityFractureConfigPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2COpenRealityFractureConfigPacket::encode)
                .decoder(S2COpenRealityFractureConfigPacket::decode)
                .consumerMainThread(S2COpenRealityFractureConfigPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CSyncRealityFractureStatePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSyncRealityFractureStatePacket::encode)
                .decoder(S2CSyncRealityFractureStatePacket::decode)
                .consumerMainThread(S2CSyncRealityFractureStatePacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SSaveRealityFractureConfigPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SSaveRealityFractureConfigPacket::encode)
                .decoder(C2SSaveRealityFractureConfigPacket::decode)
                .consumerMainThread(C2SSaveRealityFractureConfigPacket::handle)
                .add();
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
