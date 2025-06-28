package net.craftmaster08.itemsoflegends.damage;

import net.craftmaster08.itemsoflegends.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SpecialAbilityDefenseSystem {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<Item, DefenseEntry> DEFENSE_REGISTRY = new HashMap<>();
    private static final Item IMMORTAL_SHADOW = ModItems.IMMORTAL_SHADOW.get();

    // Functional interface for defense handlers
    @FunctionalInterface
    public interface DefenseHandler {
        void apply(Player target, LivingHurtEvent event, ServerLevel level);
    }

    // Class to store defense data
    private static class DefenseEntry {
        final String damageSourceId;
        final DefenseHandler handler;

        DefenseEntry(String damageSourceId, DefenseHandler handler) {
            this.damageSourceId = damageSourceId;
            this.handler = handler;
        }
    }

    public SpecialAbilityDefenseSystem() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    // Register a special ability
    public static void registerSpecialAbility(Item item, String damageSourceId, DefenseHandler handler) {
        DEFENSE_REGISTRY.put(item, new DefenseEntry(damageSourceId, handler));
        LOGGER.info("Registered special ability for item: {}", item);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player target) || target.level().isClientSide) return;
        if (!isHoldingImmortalShadow(target)) return;

        DamageSource source = event.getSource();
        ServerLevel level = (ServerLevel) target.level();

        // Check each registered special ability
        for (Map.Entry<Item, DefenseEntry> entry : DEFENSE_REGISTRY.entrySet()) {
            Item item = entry.getKey();
            DefenseEntry defense = entry.getValue();

            // Handle custom damage sources
            if (defense.damageSourceId != null && source.getMsgId().equals(defense.damageSourceId)) {
                event.setCanceled(true);
                defense.handler.apply(target, event, level);
                LOGGER.debug("Blocked {} damage for player holding ImmortalShadow", defense.damageSourceId);
                return;
            }

            // Handle playerAttack (e.g., CustomSwordItem)
            if (source.getEntity() instanceof Player attacker && source.getMsgId().equals("player")) {
                ItemStack heldItem = attacker.getMainHandItem();
                if (heldItem.getItem() == item) {
                    event.setCanceled(true);
                    defense.handler.apply(target, event, level);
                    LOGGER.debug("Blocked playerAttack from {} for player holding ImmortalShadow", item);
                    return;
                }
            }
        }
    }

    private boolean isHoldingImmortalShadow(Player player) {
        return player.getMainHandItem().getItem() == IMMORTAL_SHADOW ||
                player.getOffhandItem().getItem() == IMMORTAL_SHADOW;
    }

    // Reused from ImmortalShadowItem for safe teleport
    private static Vec3 findSafeTeleportPosition(Vec3 basePos, Level level, int radius, Vec3 wavePos, Vec3 waveDir, double waveWidth) {
        BlockPos baseBlockPos = new BlockPos((int) basePos.x, (int) basePos.y, (int) basePos.z);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -radius * 2; y <= radius * 2; y++) {
                    BlockPos testPos = baseBlockPos.offset(x, y, z);
                    BlockPos belowPos = testPos.below();
                    BlockState testState = level.getBlockState(testPos);
                    BlockState aboveState = level.getBlockState(testPos.above());
                    BlockState belowState = level.getBlockState(belowPos);

                    boolean isValidPos = testState.isAir() || !testState.isSolid();
                    boolean isAboveValid = aboveState.isAir();
                    boolean isBelowSolid = belowState.isCollisionShapeFullBlock(level, belowPos);

                    if (isValidPos && isAboveValid && isBelowSolid) {
                        Vec3 candidatePos = new Vec3(testPos.getX() + 0.5, belowPos.getY() + 1.0, testPos.getZ() + 0.5);
                        // Check if position is clear of wave path
                        if (wavePos != null && waveDir != null) {
                            Vec3 toCandidate = candidatePos.subtract(wavePos);
                            double distanceToWaveAxis = toCandidate.cross(waveDir).length();
                            if (distanceToWaveAxis < waveWidth + 1.0) {
                                continue; // Skip positions too close to wave path
                            }
                        }
                        return candidatePos;
                    }
                }
            }
        }
        if (radius < 4) {
            return findSafeTeleportPosition(basePos, level, radius * 2, wavePos, waveDir, waveWidth);
        }
        return null;
    }

    // Defense handlers
    public static final DefenseHandler DIVINE_LIBERATOR_DEFENSE = (target, event, level) -> {
        // Get wave source information from DamageSource
        Vec3 wavePos = null;
        Vec3 waveDir = null;
        double waveWidth = 5.0; // Matches DivineLiberatorItem waveWidth
        if (event.getSource().getEntity() instanceof Player attacker) {
            wavePos = attacker.position().add(0, 1.5, 0); // Approximate wave start
            waveDir = attacker.getLookAngle().normalize(); // Wave direction
        }

        // Teleport to a safe spot clear of the wave
        Vec3 basePos = target.position().add(0, 1.5, 0);
        Vec3 safePos = findSafeTeleportPosition(
                basePos.add(level.random.nextDouble() * 10 - 5, 0, level.random.nextDouble() * 10 - 5),
                level, 1, wavePos, waveDir, waveWidth
        );
        if (safePos != null) {
            target.teleportTo(safePos.x, safePos.y, safePos.z);
            // Shadow-themed particles
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, safePos.x, safePos.y + 1.0, safePos.z, 30, 0.5, 0.5, 0.5, 0.1);
            level.sendParticles(ParticleTypes.SOUL, safePos.x, safePos.y + 1.0, safePos.z, 20, 0.3, 0.3, 0.3, 0.05);
            level.sendParticles(ParticleTypes.SCULK_SOUL, safePos.x, safePos.y + 1.0, safePos.z, 15, 0.4, 0.4, 0.4, 0.05);
            // Epic sounds
            level.playSound(null, safePos.x, safePos.y, safePos.z, SoundEvents.ENDERMAN_SCREAM, SoundSource.PLAYERS, 1.0F, 0.8F);
            level.playSound(null, safePos.x, safePos.y, safePos.z, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.8F, 0.9F);
            // Grant temporary fire resistance to prevent fire ticks
            target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, false, false, false));
        } else {
            // Fallback: cancel damage and apply fire resistance
            LOGGER.warn("No safe teleport spot found for Divine Liberator defense");
            target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, false, false, false));
        }
    };

    public static final DefenseHandler COOL_STICK_DEFENSE = (target, event, level) -> {
        // Protective particle shield
        double x = target.getX(), y = target.getY() + 1.0, z = target.getZ();
        for (int i = 0; i < 30; i++) {
            double theta = level.random.nextDouble() * 2 * Math.PI;
            double phi = Math.acos(2 * level.random.nextDouble() - 1);
            double r = 0.8;
            double offsetX = r * Math.sin(phi) * Math.cos(theta);
            double offsetY = r * Math.sin(phi) * Math.sin(theta);
            double offsetZ = r * Math.cos(phi);
            level.sendParticles(ParticleTypes.ENCHANT, x + offsetX, y + offsetY, z + offsetZ, 1, 0.1, 0.1, 0.1, 0.1);
            level.sendParticles(ParticleTypes.WITCH, x + offsetX, y + offsetY, z + offsetZ, 1, 0.1, 0.1, 0.1, 0.05);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, x + offsetX, y + offsetY, z + offsetZ, 1, 0.1, 0.1, 0.1, 0.05);
        }
        // Protective sound
        level.playSound(null, x, y, z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    };
}