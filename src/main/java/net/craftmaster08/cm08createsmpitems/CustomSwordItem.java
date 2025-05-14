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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class CustomSwordItem extends SwordItem {
    public CustomSwordItem(Properties properties) {
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

            // Deal damage to entities in the radius
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.hurt(level.damageSources().playerAttack(player), 12.0F); // 4 damage (2 hearts)
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
            player.getCooldowns().addCooldown(this, 20);
        }

        return InteractionResultHolder.success(stack);
    }
}