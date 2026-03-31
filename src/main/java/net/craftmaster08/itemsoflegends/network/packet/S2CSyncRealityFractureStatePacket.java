package net.craftmaster08.itemsoflegends.network.packet;

import net.craftmaster08.itemsoflegends.reality.RealityFractureClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncRealityFractureStatePacket {
    private final boolean configured;
    private final String startDimension;
    private final String destinationDimension;

    public S2CSyncRealityFractureStatePacket(boolean configured, String startDimension, String destinationDimension) {
        this.configured = configured;
        this.startDimension = startDimension;
        this.destinationDimension = destinationDimension;
    }

    public static void encode(S2CSyncRealityFractureStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.configured);
        buffer.writeUtf(packet.startDimension);
        buffer.writeUtf(packet.destinationDimension);
    }

    public static S2CSyncRealityFractureStatePacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncRealityFractureStatePacket(buffer.readBoolean(), buffer.readUtf(), buffer.readUtf());
    }

    public static void handle(S2CSyncRealityFractureStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> RealityFractureClientState.update(packet.configured, packet.startDimension, packet.destinationDimension));
        context.setPacketHandled(true);
    }
}

