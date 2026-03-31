package net.craftmaster08.itemsoflegends.reality;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public final class RealityFractureTeleportService {
    private RealityFractureTeleportService() {
    }

    public static boolean teleport(ServerPlayer player) {
        RealityFracturePlayerData.ensureDefaults(player);

        if (!RealityFracturePlayerData.isConfigured(player)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("item.itemsoflegends.reality_fracture.not_configured")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        ResourceKey<Level> currentDimension = player.serverLevel().dimension();
        ResourceKey<Level> startDimension = RealityFracturePlayerData.getStartDimensionOrNull(player);
        ResourceKey<Level> destinationDimension = RealityFracturePlayerData.getDestinationDimensionOrNull(player);

        if (startDimension == null || destinationDimension == null || startDimension.equals(destinationDimension)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("item.itemsoflegends.reality_fracture.not_configured")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        List<ResourceKey<Level>> dimensions = RealityFracturePlayerData.getAvailableDimensions(player.server);
        if (!dimensions.contains(startDimension) || !dimensions.contains(destinationDimension)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("item.itemsoflegends.reality_fracture.invalid_dimension")
                    .withStyle(ChatFormatting.RED));
            RealityFracturePlayerData.setConfigured(player, false);
            return false;
        }

        ResourceKey<Level> targetDimension = getTargetDimension(currentDimension, startDimension, destinationDimension);
        if (targetDimension == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("item.itemsoflegends.reality_fracture.invalid_dimension")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        ServerLevel targetLevel = player.server.getLevel(targetDimension);
        if (targetLevel == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("item.itemsoflegends.reality_fracture.invalid_dimension")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        Vec3 currentPosition = player.position();
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        RealityFracturePlayerData.saveLocation(player, currentDimension, currentPosition, yaw, pitch);

        Optional<RealityFracturePlayerData.SavedLocation> remembered =
                RealityFracturePlayerData.getSavedLocation(player, targetDimension);
        RealityFractureMode mode = RealityFracturePlayerData.getMode(player);

        Vec3 baseTarget = chooseBaseTarget(player, targetLevel, remembered, mode);
        Vec3 safeTarget = SafeSpotFinder.findSafeSpot(targetLevel, baseTarget);

        if (mode == RealityFractureMode.LAST_LOCATION && remembered.isPresent()) {
            yaw = remembered.get().yaw();
            pitch = remembered.get().pitch();
        }

        player.teleportTo(targetLevel, safeTarget.x, safeTarget.y, safeTarget.z, yaw, pitch);
        RealityFracturePlayerData.saveLocation(player, targetDimension, safeTarget, yaw, pitch);

        targetLevel.playSound(null, safeTarget.x, safeTarget.y, safeTarget.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);

        return true;
    }

    private static ResourceKey<Level> getTargetDimension(ResourceKey<Level> current, ResourceKey<Level> start, ResourceKey<Level> destination) {
        if (start.equals(destination)) {
            return null;
        }
        if (current.equals(start)) {
            return destination;
        }
        if (current.equals(destination)) {
            return start;
        }
        // If used in a third dimension, return to the configured start dimension.
        return start;
    }

    private static Vec3 chooseBaseTarget(ServerPlayer player,
                                         ServerLevel targetLevel,
                                         Optional<RealityFracturePlayerData.SavedLocation> remembered,
                                         RealityFractureMode mode) {
        if (remembered.isEmpty()) {
            BlockPos spawn = targetLevel.getSharedSpawnPos();
            return new Vec3(spawn.getX() + 0.5D, spawn.getY() + 1.0D, spawn.getZ() + 0.5D);
        }

        return switch (mode) {
            case LAST_LOCATION -> {
                RealityFracturePlayerData.SavedLocation saved = remembered.get();
                yield new Vec3(saved.x(), saved.y(), saved.z());
            }
            case SAME_COORDS -> player.position();
            case TARGET_SPAWN -> {
                BlockPos spawn = targetLevel.getSharedSpawnPos();
                yield new Vec3(spawn.getX() + 0.5D, spawn.getY() + 1.0D, spawn.getZ() + 0.5D);
            }
        };
    }
}
