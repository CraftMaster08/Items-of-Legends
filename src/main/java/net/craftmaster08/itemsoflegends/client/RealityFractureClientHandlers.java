package net.craftmaster08.itemsoflegends.client;

import net.craftmaster08.itemsoflegends.client.screen.RealityFractureConfigScreen;
import net.craftmaster08.itemsoflegends.network.packet.S2COpenRealityFractureConfigPacket;
import net.minecraft.client.Minecraft;

public final class RealityFractureClientHandlers {
    private RealityFractureClientHandlers() {
    }

    public static void openConfigScreen(S2COpenRealityFractureConfigPacket packet) {
        Minecraft.getInstance().setScreen(new RealityFractureConfigScreen(packet));
    }
}

