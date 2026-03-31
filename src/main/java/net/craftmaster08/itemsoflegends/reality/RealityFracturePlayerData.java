package net.craftmaster08.itemsoflegends.reality;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class RealityFracturePlayerData {
    private static final String ROOT_TAG = "reality_fracture";
    private static final String START_DIM_TAG = "start_dimension";
    private static final String DEST_DIM_TAG = "destination_dimension";
    private static final String MODE_TAG = "mode";
    private static final String LOCATIONS_TAG = "saved_locations";
    private static final String CONFIGURED_TAG = "configured";

    private RealityFracturePlayerData() {
    }

    public static void ensureDefaults(ServerPlayer player) {
        ResourceKey<Level> currentDimension = player.serverLevel().dimension();
        List<ResourceKey<Level>> dimensions = getAvailableDimensions(player.server);

        ResourceKey<Level> configuredStart = getStartDimensionOrNull(player);
        if (configuredStart == null || !dimensions.contains(configuredStart)) {
            setStartDimension(player, currentDimension);
        }

        ResourceKey<Level> configuredDestination = getDestinationDimensionOrNull(player);
        if (configuredDestination != null && !dimensions.contains(configuredDestination)) {
            clearDestinationDimension(player);
            setConfigured(player, false);
        }
    }

    public static List<ResourceKey<Level>> getAvailableDimensions(MinecraftServer server) {
        List<ResourceKey<Level>> keys = new ArrayList<>(server.levelKeys());
        keys.sort(Comparator.comparing(key -> key.location().toString()));
        return keys;
    }

    public static ResourceKey<Level> getStartDimension(ServerPlayer player, ResourceKey<Level> fallback) {
        ResourceKey<Level> key = getStartDimensionOrNull(player);
        return key != null ? key : fallback;
    }

    public static ResourceKey<Level> getStartDimensionOrNull(ServerPlayer player) {
        return readDimension(getRootTag(player).getString(START_DIM_TAG));
    }

    public static void setStartDimension(ServerPlayer player, ResourceKey<Level> startDimension) {
        getRootTag(player).putString(START_DIM_TAG, startDimension.location().toString());
    }

    public static ResourceKey<Level> getDestinationDimension(ServerPlayer player, ResourceKey<Level> fallback) {
        ResourceKey<Level> key = getDestinationDimensionOrNull(player);
        return key != null ? key : fallback;
    }

    public static ResourceKey<Level> getDestinationDimensionOrNull(ServerPlayer player) {
        return readDimension(getRootTag(player).getString(DEST_DIM_TAG));
    }

    public static void setDestinationDimension(ServerPlayer player, ResourceKey<Level> destinationDimension) {
        getRootTag(player).putString(DEST_DIM_TAG, destinationDimension.location().toString());
    }

    public static void clearDestinationDimension(ServerPlayer player) {
        getRootTag(player).remove(DEST_DIM_TAG);
    }

    public static RealityFractureMode getMode(ServerPlayer player) {
        String value = getRootTag(player).getString(MODE_TAG);
        if (value == null || value.isBlank()) {
            return RealityFractureMode.LAST_LOCATION;
        }
        return RealityFractureMode.fromName(value);
    }

    public static void setMode(ServerPlayer player, RealityFractureMode mode) {
        getRootTag(player).putString(MODE_TAG, mode.name());
    }

    public static boolean isConfigured(ServerPlayer player) {
        CompoundTag root = getRootTag(player);
        if (!root.getBoolean(CONFIGURED_TAG)) {
            return false;
        }
        ResourceKey<Level> start = getStartDimensionOrNull(player);
        ResourceKey<Level> destination = getDestinationDimensionOrNull(player);
        return start != null && destination != null && !start.equals(destination);
    }

    public static void setConfigured(ServerPlayer player, boolean configured) {
        getRootTag(player).putBoolean(CONFIGURED_TAG, configured);
    }

    public static Optional<SavedLocation> getSavedLocation(ServerPlayer player, ResourceKey<Level> dimension) {
        ListTag list = getRootTag(player).getList(LOCATIONS_TAG, Tag.TAG_COMPOUND);
        for (Tag tag : list) {
            CompoundTag entry = (CompoundTag) tag;
            if (dimension.location().toString().equals(entry.getString("dimension"))) {
                return Optional.of(new SavedLocation(
                        entry.getDouble("x"),
                        entry.getDouble("y"),
                        entry.getDouble("z"),
                        entry.getFloat("yaw"),
                        entry.getFloat("pitch")
                ));
            }
        }
        return Optional.empty();
    }

    public static void saveLocation(ServerPlayer player, ResourceKey<Level> dimension, Vec3 location, float yaw, float pitch) {
        CompoundTag root = getRootTag(player);
        ListTag list = root.getList(LOCATIONS_TAG, Tag.TAG_COMPOUND);
        ListTag replacement = new ListTag();

        for (Tag tag : list) {
            CompoundTag entry = (CompoundTag) tag;
            if (!dimension.location().toString().equals(entry.getString("dimension"))) {
                replacement.add(entry.copy());
            }
        }

        CompoundTag updated = new CompoundTag();
        updated.putString("dimension", dimension.location().toString());
        updated.putDouble("x", location.x);
        updated.putDouble("y", location.y);
        updated.putDouble("z", location.z);
        updated.putFloat("yaw", yaw);
        updated.putFloat("pitch", pitch);
        replacement.add(updated);

        root.put(LOCATIONS_TAG, replacement);
    }

    private static CompoundTag getRootTag(Player player) {
        CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persistent.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            persistent.put(ROOT_TAG, new CompoundTag());
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
        }
        return persistent.getCompound(ROOT_TAG);
    }

    private static ResourceKey<Level> readDimension(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            return null;
        }
        return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, location);
    }

    public record SavedLocation(double x, double y, double z, float yaw, float pitch) {
    }
}
