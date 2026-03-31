package net.craftmaster08.itemsoflegends.network.packet;

import net.craftmaster08.itemsoflegends.network.ModNetwork;
import net.craftmaster08.itemsoflegends.reality.RealityFractureMode;
import net.craftmaster08.itemsoflegends.reality.RealityFracturePlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class C2SSaveRealityFractureConfigPacket {
    private final String destinationDimension;
    private final String mode;

    public C2SSaveRealityFractureConfigPacket(String destinationDimension, String mode) {
        this.destinationDimension = destinationDimension;
        this.mode = mode;
    }

    public static void encode(C2SSaveRealityFractureConfigPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.destinationDimension);
        buffer.writeUtf(packet.mode);
    }

    public static C2SSaveRealityFractureConfigPacket decode(FriendlyByteBuf buffer) {
        return new C2SSaveRealityFractureConfigPacket(buffer.readUtf(), buffer.readUtf());
    }

    public static void handle(C2SSaveRealityFractureConfigPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        ResourceKey<Level> startDimension = player.serverLevel().dimension();
        RealityFracturePlayerData.setStartDimension(player, startDimension);

        List<ResourceKey<Level>> dimensions = RealityFracturePlayerData.getAvailableDimensions(player.server);
        ResourceKey<Level> destination = parseDimension(packet.destinationDimension);

        if (destination == null || !dimensions.contains(destination) || destination.equals(startDimension)) {
            RealityFracturePlayerData.clearDestinationDimension(player);
            RealityFracturePlayerData.setConfigured(player, false);
            sync(player);
            player.sendSystemMessage(Component.translatable("item.itemsoflegends.reality_fracture.not_configured")
                    .withStyle(ChatFormatting.RED));
            context.setPacketHandled(true);
            return;
        }

        RealityFracturePlayerData.setDestinationDimension(player, destination);
        RealityFracturePlayerData.setMode(player, RealityFractureMode.fromName(packet.mode));
        RealityFracturePlayerData.setConfigured(player, true);
        sync(player);

        player.sendSystemMessage(Component.translatable("gui.itemsoflegends.reality_fracture.saved")
                .withStyle(ChatFormatting.GREEN));
        context.setPacketHandled(true);
    }

    private static void sync(ServerPlayer player) {
        ResourceKey<Level> start = RealityFracturePlayerData.getStartDimensionOrNull(player);
        ResourceKey<Level> destination = RealityFracturePlayerData.getDestinationDimensionOrNull(player);
        ModNetwork.sendToPlayer(player, new S2CSyncRealityFractureStatePacket(
                RealityFracturePlayerData.isConfigured(player),
                start == null ? "" : start.location().toString(),
                destination == null ? "" : destination.location().toString()
        ));
    }

    private static ResourceKey<Level> parseDimension(String value) {
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            return null;
        }
        return ResourceKey.create(Registries.DIMENSION, location);
    }
}
