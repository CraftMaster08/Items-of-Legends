package net.craftmaster08.itemsoflegends.network.packet;

import net.craftmaster08.itemsoflegends.client.RealityFractureClientHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2COpenRealityFractureConfigPacket {
    private final String startDimension;
    private final String destinationDimension;
    private final String mode;
    private final List<String> dimensions;

    public S2COpenRealityFractureConfigPacket(String startDimension, String destinationDimension, String mode, List<String> dimensions) {
        this.startDimension = startDimension;
        this.destinationDimension = destinationDimension;
        this.mode = mode;
        this.dimensions = dimensions;
    }

    public String startDimension() {
        return startDimension;
    }

    public String destinationDimension() {
        return destinationDimension;
    }

    public String mode() {
        return mode;
    }

    public List<String> dimensions() {
        return dimensions;
    }

    public static void encode(S2COpenRealityFractureConfigPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.startDimension);
        buffer.writeUtf(packet.destinationDimension);
        buffer.writeUtf(packet.mode);
        buffer.writeVarInt(packet.dimensions.size());
        for (String dimension : packet.dimensions) {
            buffer.writeUtf(dimension);
        }
    }

    public static S2COpenRealityFractureConfigPacket decode(FriendlyByteBuf buffer) {
        String start = buffer.readUtf();
        String destination = buffer.readUtf();
        String mode = buffer.readUtf();
        int size = buffer.readVarInt();
        List<String> dimensions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            dimensions.add(buffer.readUtf());
        }
        return new S2COpenRealityFractureConfigPacket(start, destination, mode, dimensions);
    }

    public static void handle(S2COpenRealityFractureConfigPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RealityFractureClientHandlers.openConfigScreen(packet));
        context.setPacketHandled(true);
    }
}

