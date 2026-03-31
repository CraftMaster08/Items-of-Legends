package net.craftmaster08.itemsoflegends.reality;

import net.craftmaster08.itemsoflegends.ItemsOfLegends;
import net.craftmaster08.itemsoflegends.network.ModNetwork;
import net.craftmaster08.itemsoflegends.network.packet.S2CSyncRealityFractureStatePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ItemsOfLegends.MODID)
public class RealityFractureEvents {
    private static final String ROOT_TAG = "reality_fracture";

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag originalPersisted = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!originalPersisted.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag currentPersisted = event.getEntity().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        currentPersisted.put(ROOT_TAG, originalPersisted.getCompound(ROOT_TAG).copy());
        event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, currentPersisted);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncClientState(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncClientState(serverPlayer);
        }
    }

    private static void syncClientState(ServerPlayer player) {
        ResourceKey<Level> start = RealityFracturePlayerData.getStartDimensionOrNull(player);
        ResourceKey<Level> destination = RealityFracturePlayerData.getDestinationDimensionOrNull(player);
        ModNetwork.sendToPlayer(player, new S2CSyncRealityFractureStatePacket(
                RealityFracturePlayerData.isConfigured(player),
                start == null ? "" : start.location().toString(),
                destination == null ? "" : destination.location().toString()
        ));
    }
}
