package net.craftmaster08.cm08createsmpitems;

import net.craftmaster08.cm08createsmpitems.util.WhitelistManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoolStickItem extends SwordItem {
    public CoolStickItem(Properties properties) {
        super(Tiers.DIAMOND, 3, -2.4F, properties);
    }

    // Play sound on attack (left-click)
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 1.0F, 2.0F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    // Right-click special ability with 2-second cooldown
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Check cooldown
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        // Check if the player is whitelisted for this item
        if (!WhitelistManager.isPlayerWhitelisted(player, "custom_sword")) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("item.cm08createsmpitems.not_whitelisted"));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.0F);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            // Define the 5-block radius area
            double radius = 5.0;
            AABB aabb = new AABB(player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                    player.getX() + radius, player.getY() + radius, player.getZ() + radius);
            List<Entity> entities = level.getEntities(player, aabb, entity -> entity instanceof LivingEntity);
            Set<Entity> protectedEntities = new HashSet<>(); // Track protected entities to avoid duplicate effects

            // Deal damage to entities in the radius, excluding protected ones
            for (Entity entity : entities) {
                // Check for tamed pets (TamableAnimal) and tamed horses (AbstractHorse)
                boolean isProtected = false;
                if (entity instanceof TamableAnimal tamable && tamable.isTame()) {
                    isProtected = true;
                } else if (entity instanceof AbstractHorse horse && horse.isTamed()) {
                    isProtected = true;
                }

                if (isProtected) {
                    if (!protectedEntities.contains(entity)) {
                        protectedEntities.add(entity); // Mark as protected to avoid repeated effects

                        // Spawn protective particles around the pet (spherical pattern)
                        ServerLevel serverLevel = (ServerLevel) level;
                        double petX = entity.getX();
                        double petY = entity.getY() + entity.getBbHeight() / 2.0;
                        double petZ = entity.getZ();
                        for (int i = 0; i < 20; i++) { // 20 particles for a dense shield effect
                            double theta = level.random.nextDouble() * 2.0 * Math.PI; // Azimuthal angle
                            double phi = Math.acos(2.0 * level.random.nextDouble() - 1.0); // Polar angle
                            double shieldRadius = 0.4 + level.random.nextDouble() * 0.2; // Radius 0.4 to 0.6 blocks
                            double xOffset = shieldRadius * Math.sin(phi) * Math.cos(theta);
                            double yOffset = shieldRadius * Math.sin(phi) * Math.sin(theta);
                            double zOffset = shieldRadius * Math.cos(phi);

                            // White sparkles for the shield
                            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                    petX + xOffset, petY + yOffset, petZ + zOffset,
                                    1, 0.02, 0.02, 0.02, 0.0);

                            // Enchant particles for a magical glow
                            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                                    petX + xOffset, petY + yOffset, petZ + zOffset,
                                    1, 0.1, 0.1, 0.1, 0.1);
                        }

                        // Play protective sounds
                        level.playSound(null, petX, petY, petZ,
                                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 1.0F); // Magical shimmer
                        level.playSound(null, petX, petY, petZ,
                                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.2F); // Gentle chime
                    }
                    continue; // Skip damage for protected entities
                }

                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.hurt(level.damageSources().playerAttack(player), 12.0F); // 6 hearts
                }
            }

            // Spawn explosion particles in the area
            ServerLevel serverLevel = (ServerLevel) level;
            for (double x = player.getX() - radius; x <= player.getX() + radius; x += 1.0) {
                for (double z = player.getZ() - radius; z <= player.getZ() + radius; z += 1.0) {
                    if (level.random.nextFloat() < 0.3) { // Randomly spawn particles for a scattered effect
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, player.getY(), z,
                                1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            }

            // Play anvil place sound at pitch 0
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 1.0F, 0.0F);

            // Apply 2-second cooldown (40 ticks)
            player.getCooldowns().addCooldown(this, 40);
        }

        return InteractionResultHolder.success(stack);
    }
}