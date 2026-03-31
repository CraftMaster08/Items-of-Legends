package net.craftmaster08.itemsoflegends.reality;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public final class SafeSpotFinder {
    private SafeSpotFinder() {
    }

    public static Vec3 findSafeSpot(ServerLevel level, Vec3 basePosition) {
        BlockPos base = BlockPos.containing(basePosition);
        int minY = level.getMinBuildHeight() + 1;
        int maxY = getMaxCandidateY(level);
        if (base.getY() < minY || base.getY() > maxY) {
            base = new BlockPos(base.getX(), Math.max(minY, Math.min(maxY, base.getY())), base.getZ());
        }

        Vec3 nearby = searchAround(level, base, 16, 12, minY, maxY);
        if (nearby != null) {
            return nearby;
        }

        BlockPos spawn = level.getSharedSpawnPos();
        int spawnY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
        spawnY = Math.max(minY, Math.min(maxY, spawnY));
        Vec3 spawnSpot = searchAround(level, new BlockPos(spawn.getX(), spawnY, spawn.getZ()), 16, 20, minY, maxY);
        if (spawnSpot != null) {
            return spawnSpot;
        }

        int fallbackY = Math.max(minY, Math.min(maxY, spawn.getY() + 1));
        return new Vec3(spawn.getX() + 0.5D, fallbackY, spawn.getZ() + 0.5D);
    }

    private static Vec3 searchAround(ServerLevel level,
                                     BlockPos center,
                                     int horizontalRadius,
                                     int verticalRadius,
                                     int minY,
                                     int maxY) {
        for (int radius = 0; radius <= horizontalRadius; radius++) {
            for (int y = -verticalRadius; y <= verticalRadius; y++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (Math.max(Math.abs(x), Math.abs(z)) != radius) {
                            continue;
                        }
                        BlockPos feet = center.offset(x, y, z);
                        if (feet.getY() < minY || feet.getY() > maxY) {
                            continue;
                        }
                        if (isSafeStandPosition(level, feet)) {
                            return new Vec3(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static int getMaxCandidateY(ServerLevel level) {
        int maxY = level.getMaxBuildHeight() - 2;
        if (level.dimensionType().hasCeiling()) {
            // Keep teleport targets under the ceiling layer (e.g. below Nether bedrock roof).
            maxY = Math.min(maxY, level.getLogicalHeight() - 6);
        }
        return maxY;
    }

    private static boolean isSafeStandPosition(ServerLevel level, BlockPos feet) {
        BlockPos head = feet.above();
        BlockPos floor = feet.below();

        BlockState feetState = level.getBlockState(feet);
        BlockState headState = level.getBlockState(head);
        BlockState floorState = level.getBlockState(floor);

        boolean freeBodySpace = feetState.getCollisionShape(level, feet).isEmpty()
                && headState.getCollisionShape(level, head).isEmpty();
        boolean noFluid = level.getFluidState(feet).isEmpty() && level.getFluidState(head).isEmpty();
        boolean stableFloor = floorState.isFaceSturdy(level, floor, Direction.UP);
        boolean noLavaFloor = !floorState.is(Blocks.LAVA) && !floorState.is(Blocks.MAGMA_BLOCK);

        return freeBodySpace && noFluid && stableFloor && noLavaFloor;
    }
}
