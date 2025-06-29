package net.craftmaster08.itemsoflegends.item.custom;

import net.craftmaster08.itemsoflegends.damage.ModDamageTypes;
import net.craftmaster08.itemsoflegends.util.WhitelistManager;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DivineLiberatorItem extends SwordItem {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int CHARGE_TIME = 20; // 1 second (20 ticks)
    private static final DustColorTransitionOptions BLACK_TO_RED = new DustColorTransitionOptions(
            new Vector3f(0.0F, 0.0F, 0.0F), // Black (RGB: 0, 0, 0)
            new Vector3f(1.0F, 0.0F, 0.0F), // Red (RGB: 255, 0, 0)
            1.0F
    );
    private static final DustColorTransitionOptions RED_TO_DARK_GRAY = new DustColorTransitionOptions(
            new Vector3f(1.0F, 0.0F, 0.0F), // Red (RGB: 255, 0, 0)
            new Vector3f(0.3F, 0.3F, 0.3F), // Dark Gray (RGB: ~76, 76, 76)
            1.0F
    );
    private static final DustColorTransitionOptions DARK_GRAY_TO_MAROON = new DustColorTransitionOptions(
            new Vector3f(0.3F, 0.3F, 0.3F), // Dark Gray (RGB: ~76, 76, 76)
            new Vector3f(0.5F, 0.0F, 0.0F), // Maroon (RGB: ~128, 0, 0)
            1.0F
    );
    private static final DustColorTransitionOptions BLACK_TO_DARK_GRAY = new DustColorTransitionOptions(
            new Vector3f(0.0F, 0.0F, 0.0F), // Black (RGB: 0, 0, 0)
            new Vector3f(0.3F, 0.3F, 0.3F), // Dark Gray (RGB: ~76, 76, 76)
            0.5F
    );
    private static final DustColorTransitionOptions RED_VORTEX = new DustColorTransitionOptions(
            new Vector3f(1.0F, 0.0F, 0.0F), // Red (RGB: 255, 0, 0)
            new Vector3f(0.5F, 0.0F, 0.0F), // Maroon (RGB: ~128, 0, 0)
            0.8F
    );
    private static final DustColorTransitionOptions BLACK_TO_BLACK = new DustColorTransitionOptions(
            new Vector3f(0.0F, 0.0F, 0.0F), // Black (RGB: 0, 0, 0)
            new Vector3f(0.0F, 0.0F, 0.0F), // Black (RGB: 0, 0, 0)
            1.0F
    );
    private static final DustColorTransitionOptions WHITE_TO_WHITE = new DustColorTransitionOptions(
            new Vector3f(1.0F, 1.0F, 1.0F), // White (RGB: 255, 255, 255)
            new Vector3f(1.0F, 1.0F, 1.0F), // White (RGB: 255, 255, 255)
            1.0F
    );
    private static final ParticleOptions ORANGE_WAX = ParticleTypes.WAX_ON; // Orange particles for charging

    public DivineLiberatorItem(Properties properties) {
        // Set durability to 3200, attack damage and speed unchanged
        super(Tiers.DIAMOND, 8, -2.7F, properties.durability(3200));
        // Register the event handler for player ticks
        MinecraftForge.EVENT_BUS.register(new PlayerDeathHandler());
    }

    private boolean hasPlayedHum = false; // Track if the low hum has played

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            ServerLevel level = (ServerLevel) attacker.level();

            // Play hit sound (ANVIL_PLACE, pitch 2.0)
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 1.0F, 2.0F);

            // Add particles
            Vec3 pos = target.getPosition(1.0F).add(0, target.getBbHeight() / 2.0, 0);
            for (int i = 0; i < 20; i++) {
                double theta = level.random.nextDouble() * 2 * Math.PI;
                double radius = 0.8 + level.random.nextDouble() * 0.4;
                double offsetX = Math.cos(theta) * radius;
                double offsetZ = Math.sin(theta) * radius;
                level.sendParticles(ParticleTypes.FLAME,
                        pos.x + offsetX, pos.y + (level.random.nextDouble() - 0.5) * 0.5, pos.z + offsetZ,
                        1, 0.1, 0.1, 0.1, 0.05);
                level.sendParticles(ParticleTypes.ASH,
                        pos.x + offsetX, pos.y + (level.random.nextDouble() - 0.5) * 0.5, pos.z + offsetZ,
                        1, 0.1, 0.1, 0.1, 0.05);
            }
            level.sendParticles(ParticleTypes.DRIPPING_LAVA,
                    pos.x, pos.y, pos.z, 10, 0.3, 0.3, 0.3, 0.0);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        // Use translation key for the description
        tooltip.add(Component.translatable("item.itemsoflegends.divine_liberator.description"));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if (!(entity instanceof Player player)) return;

        int useTime = getUseDuration(stack) - remainingUseTicks;

        // Apply Slowness and Glowing effects while charging
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 2, false, false, false)); // Slowness III for 5 ticks
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 5, 0, false, false, false)); // Glowing for 5 ticks
        }

        // Visual charging indicator: closing-in particles + spiraling wax on + vortex
        if (!level.isClientSide) {
            float progress = Math.min((float) useTime / CHARGE_TIME, 1.0F);
            double maxDistance = 5.0; // Particles start 5 blocks away
            double currentDistance = maxDistance * (1.0 - progress); // Particles move inward
            int particleCount = 64; // Increased density for dramatic effect

            // Closing-in dust particles
            for (int i = 0; i < particleCount; i++) {
                double theta = Math.random() * 2.0 * Math.PI; // Azimuthal angle
                double phi = Math.acos(2.0 * Math.random() - 1.0); // Polar angle
                double xOffset = currentDistance * Math.sin(phi) * Math.cos(theta);
                double yOffset = currentDistance * Math.sin(phi) * Math.sin(theta);
                double zOffset = currentDistance * Math.cos(phi);

                ((ServerLevel) level).sendParticles(BLACK_TO_RED,
                        player.getX() + xOffset, player.getY() + 1.0 + yOffset, player.getZ() + zOffset,
                        1, 0.0, 0.0, 0.0, 0.0);
                ((ServerLevel) level).sendParticles(RED_TO_DARK_GRAY,
                        player.getX() + xOffset, player.getY() + 1.0 + yOffset, player.getZ() + zOffset,
                        1, 0.0, 0.0, 0.0, 0.0);
            }

            // Add swirling vortex of red particles rising from the ground
            for (int i = 0; i < 20; i++) {
                double theta = (useTime * 0.3 + i * 2.0 * Math.PI / 20); // Swirling motion
                double radius = 2.0 * (1.0 - progress); // Vortex shrinks as charge progresses
                double xOffset = radius * Math.cos(theta);
                double zOffset = radius * Math.sin(theta);
                double yOffset = (useTime * 0.05) % 2.0; // Rise from ground to player height

                ((ServerLevel) level).sendParticles(RED_VORTEX,
                        player.getX() + xOffset, player.getY() + yOffset, player.getZ() + zOffset,
                        1, 0.0, 0.0, 0.0, 0.0);
            }

            // Add spiraling orange wax on particles from bottom to top
            for (int i = 0; i < 10; i++) {
                double theta = (useTime * 0.5 + i * 2.0 * Math.PI / 10); // Fast spiral rotation
                double radius = 1.0 * (1.0 - progress * 0.5); // Radius shrinks slightly as charge progresses
                double xOffset = radius * Math.cos(theta);
                double zOffset = radius * Math.sin(theta);
                double yOffset = -0.5 + (2.5 * useTime / CHARGE_TIME) + (i * 0.2); // Spiral upward

                ((ServerLevel) level).sendParticles(ORANGE_WAX,
                        player.getX() + xOffset, player.getY() + yOffset, player.getZ() + zOffset,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        // Play charging sound every 5 ticks
        if (useTime % 5 == 0 && useTime < CHARGE_TIME) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 1.0F, 0.89F);
        }

        // Add ominous undertone every 10 ticks
        if (useTime % 10 == 0 && useTime < CHARGE_TIME) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 0.5F, 0.7F);
        }

        // Automatically fire after fully charging
        if (useTime >= CHARGE_TIME && !level.isClientSide) {
            player.stopUsingItem();
            launchWave(player, level, stack); // Pass the ItemStack to reduce durability
            player.getCooldowns().addCooldown(this, 40); // 2-second cooldown
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // Standard use duration (same as bow)
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        // Check if the player is whitelisted for this item
        if (!WhitelistManager.isPlayerWhitelisted(player, "divine_liberator")) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("item.itemsoflegends.not_whitelisted"));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.0F);
            }
            return InteractionResultHolder.fail(stack);
        }

        hasPlayedHum = false; // Reset the hum flag when starting a new charge
        player.startUsingItem(hand); // Start the charging process
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        int useTime = getUseDuration(stack) - timeLeft;
        if (useTime < CHARGE_TIME && !level.isClientSide) {
            // Stop the charging sound for all nearby players
            ((ServerLevel) level).getPlayers(p -> true).forEach(p ->
                    p.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 0.0F, 0.0F) // Volume 0 to stop sound
            );

            // Play cancel sound (ANVIL_LAND) for all nearby players
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 2.0F);

            // Clear Slowness and Glowing effects on cancel
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            player.removeEffect(MobEffects.GLOWING);
        }
    }

    private void launchWave(Player player, Level level, ItemStack stack) {
        if (!level.isClientSide) {
            // Clear Slowness and Glowing effects on launch
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            player.removeEffect(MobEffects.GLOWING);

            // Reduce durability if the player is in Survival mode
            if (!player.isCreative()) {
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }

            // Apply random knockback (2 to 5 blocks) in the opposite direction of the wave
            Vec3 lookDirection = player.getLookAngle().normalize();
            double knockbackDistance = 2.0 + level.random.nextDouble() * 3.0; // Random between 2 and 5 blocks
            Vec3 knockbackVector = lookDirection.scale(-knockbackDistance * 0.5); // Scale for velocity (0.5 blocks/tick)
            player.setDeltaMovement(player.getDeltaMovement().add(knockbackVector.x, 0.1, knockbackVector.z)); // Small upward push
            player.hurtMarked = true; // Mark for velocity update on server

            // Play initial shot sound (WARDEN_SONIC_BOOM + ENDER_DRAGON_GROWL)
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0F, 1.1F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.5F, 1.0F);

            // Add crimson spore burst on launch
            ((ServerLevel) level).sendParticles(ParticleTypes.CRIMSON_SPORE,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    50, 1.0, 1.0, 1.0, 0.0);

            // Schedule the wave movement
            new WaveHandler(player, level, stack);
        }
    }

    // Inner class to handle the wave movement
    private static class WaveHandler {
        private final Player player;
        private final ServerLevel level;
        private final ItemStack stack; // Store the ItemStack
        private final Vec3 direction;
        private Vec3 position;
        private int distanceTraveled;
        private final Set<Entity> hitEntities;
        private final double waveSpeed = 2.0; // Blocks per tick (fast movement)
        private final double waveWidth = 5.0; // 5 blocks in each direction (10 total width)
        private final int maxDistance = 100; // Max 100 blocks
        private int soundTimer = 0; // Timer for wave sound

        public WaveHandler(Player player, Level level, ItemStack stack) {
            this.player = player;
            this.level = (ServerLevel) level;
            this.stack = stack.copy(); // Copy to prevent modifications
            this.direction = player.getLookAngle().normalize();
            this.position = player.position().add(0, 1.5, 0); // Start at player's eye level
            this.distanceTraveled = 0;
            this.hitEntities = new HashSet<>();
            MinecraftForge.EVENT_BUS.register(this);
        }

        private void spawnWaveParticles(Vec3 particlePosition) {
            // Calculate the perpendicular vector for the wave width (left and right)
            Vec3 perpendicular = new Vec3(-direction.z, 0, direction.x).normalize();
            Vec3 leftEdge = particlePosition.add(perpendicular.scale(-waveWidth));
            Vec3 rightEdge = particlePosition.add(perpendicular.scale(waveWidth));

            // Spawn custom colored dust particles along the wave with increased density
            for (double i = -waveWidth; i <= waveWidth; i += 0.3) { // Reduced step size for more density
                Vec3 particlePos = particlePosition.add(perpendicular.scale(i));
                level.sendParticles(BLACK_TO_RED, particlePos.x, particlePos.y, particlePos.z,
                        8, 0.1, 0.1, 0.1, 0.0); // Increased count
                level.sendParticles(RED_TO_DARK_GRAY, particlePos.x, particlePos.y, particlePos.z,
                        5, 0.1, 0.1, 0.1, 0.0); // Increased count
                level.sendParticles(DARK_GRAY_TO_MAROON, particlePos.x, particlePos.y, particlePos.z,
                        3, 0.1, 0.1, 0.1, 0.0); // Increased count
                // Add fiery edges with FLAME particles
                if (Math.abs(i - waveWidth) < 0.5 || Math.abs(i + waveWidth) < 0.5) {
                    level.sendParticles(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z,
                            2, 0.1, 0.1, 0.1, 0.0);
                }
            }
        }

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            if (distanceTraveled >= maxDistance) {
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }

            // Store current position for trail and particle calculations
            Vec3 currentPosition = position;

            // Move the wave forward
            position = position.add(direction.scale(waveSpeed));
            distanceTraveled += (int) waveSpeed;

            // Check for block collision, ignoring grass, tall grass, water, and snow layers
            BlockState blockState = level.getBlockState(BlockPos.containing(position));
            if (!blockState.isAir() && !blockState.is(Blocks.GRASS) && !blockState.is(Blocks.TALL_GRASS) && !blockState.is(Blocks.WATER) && !blockState.is(Blocks.SNOW)) {
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }

            // Play wave travel sounds every 10 ticks
            soundTimer++;
            if (soundTimer >= 10) {
                level.playSound(null, position.x, position.y, position.z,
                        SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.5F);
                soundTimer = 0;
            }

            // Spawn particles at the current position
            spawnWaveParticles(currentPosition);

            // Calculate the next position (for particle purposes only, not updating position)
            Vec3 nextPosition = position.add(direction.scale(waveSpeed));
            // Spawn particles at the midpoint between current and next position
            Vec3 intermediatePosition = currentPosition.add(nextPosition.subtract(currentPosition).scale(0.5));
            spawnWaveParticles(intermediatePosition);

            // Add a faint trail behind the wave
            Vec3 trailPos = currentPosition.add(direction.scale(-waveSpeed * 0.5)); // Slightly behind the wave
            for (int i = 0; i < 5; i++) {
                double xOffset = (level.random.nextDouble() - 0.5) * waveWidth;
                double zOffset = (level.random.nextDouble() - 0.5) * waveWidth;
                level.sendParticles(BLACK_TO_DARK_GRAY,
                        trailPos.x + xOffset, trailPos.y, trailPos.z + zOffset,
                        1, 0.1, 0.1, 0.1, 0.0);
            }

            // Calculate the perpendicular vector for the wave width (left and right) for entity detection
            Vec3 perpendicular = new Vec3(-direction.z, 0, direction.x).normalize();
            Vec3 leftEdge = position.add(perpendicular.scale(-waveWidth));
            Vec3 rightEdge = position.add(perpendicular.scale(waveWidth));

            // Check for entities in the wave's path (reduced vertical expansion)
            AABB aabb = new AABB(leftEdge, rightEdge).inflate(1.0, 1.0, 1.0); // Reduced vertical inflation to 1.0
            List<Entity> entities = level.getEntities(player, aabb, entity -> entity instanceof LivingEntity);

            for (Entity entity : entities) {
                // Check for tamed pets (TamableAnimal) and tamed horses (AbstractHorse)
                boolean isProtected = false;
                if (entity instanceof TamableAnimal tamable && tamable.isTame()) {
                    isProtected = true;
                } else if (entity instanceof AbstractHorse horse && horse.isTamed()) {
                    isProtected = true;
                }

                if (isProtected) {
                    if (!hitEntities.contains(entity)) {
                        hitEntities.add(entity); // Mark as hit to avoid repeated effects

                        // Spawn protective particles around the pet (spherical pattern)
                        double petX = entity.getX();
                        double petY = entity.getY() + entity.getBbHeight() / 2.0;
                        double petZ = entity.getZ();
                        for (int i = 0; i < 30; i++) { // 30 particles for a dense shield effect
                            double theta = level.random.nextDouble() * 2.0 * Math.PI; // Azimuthal angle
                            double phi = Math.acos(2.0 * level.random.nextDouble() - 1.0); // Polar angle
                            double radius = 0.5 + level.random.nextDouble() * 0.3; // Radius 0.5 to 0.8 blocks
                            double xOffset = radius * Math.sin(phi) * Math.cos(theta);
                            double yOffset = radius * Math.sin(phi) * Math.sin(theta);
                            double zOffset = radius * Math.cos(phi);

                            // White sparkles for the shield
                            level.sendParticles(WHITE_TO_WHITE,
                                    petX + xOffset, petY + yOffset, petZ + zOffset,
                                    1, 0.02, 0.02, 0.02, 0.0);

                            // Enchant particles for a magical glow
                            level.sendParticles(ParticleTypes.ENCHANT,
                                    petX + xOffset, petY + yOffset, petZ + zOffset,
                                    1, 0.1, 0.1, 0.1, 0.1);
                        }

                        // Play epic immunity sounds
                        level.playSound(null, petX, petY, petZ,
                                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.2F); // Magical shimmer
                        level.playSound(null, petX, petY, petZ,
                                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.8F, 0.9F); // Epic tone
                    }
                    continue; // Skip damage for protected entities
                }

                if (!hitEntities.contains(entity) && entity instanceof LivingEntity livingEntity) {
                    hitEntities.add(entity);
                    try {
                        // Apply damage using the custom DamageSource with ItemStack
                        livingEntity.hurt(ModDamageTypes.waveAttack(level, player, stack), 48.0F); // 24 hearts
                        LOGGER.debug("Applied wave damage to {} with item {}", livingEntity.getName().getString(), stack.getDisplayName().getString());
                    } catch (IllegalStateException e) {
                        LOGGER.error("Failed to apply wave damage: {}", e.getMessage());
                        continue; // Skip this entity to prevent crash
                    }
                    // Mark players as "cleansed" for follow-up death message and store attacker's name
                    if (livingEntity instanceof Player targetPlayer) {
                        targetPlayer.getPersistentData().putLong("DivineLiberatorCleansed", level.getGameTime());
                        targetPlayer.getPersistentData().putString("DivineLiberatorAttacker", player.getGameProfile().getName()); // Store attacker's name
                        LOGGER.debug("Marked player as cleansed: {} by attacker: {}", targetPlayer.getName().getString(), player.getGameProfile().getName());
                    }
                    // Set the entity on fire for 5 seconds (100 ticks)
                    livingEntity.setSecondsOnFire(5);
                    // Play impact sounds
                    level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.PLAYERS, 0.5F, 1.2F);
                    // Add impact particles (custom dust + firework flash + crimson spore burst)
                    level.sendParticles(BLACK_TO_RED, entity.getX(), entity.getY() + 1.0, entity.getZ(),
                            15, 0.5, 0.5, 0.5, 0.0); // Increased count
                    level.sendParticles(RED_TO_DARK_GRAY, entity.getX(), entity.getY() + 1.0, entity.getZ(),
                            10, 0.5, 0.5, 0.5, 0.0); // Increased count
                    level.sendParticles(ParticleTypes.FIREWORK, entity.getX(), entity.getY() + 1.0, entity.getZ(),
                            5, 0.3, 0.3, 0.3, 0.1);
                    level.sendParticles(ParticleTypes.CRIMSON_SPORE, entity.getX(), entity.getY() + 1.0, entity.getZ(),
                            20, 0.5, 0.5, 0.5, 0.0);
                }
            }
        }
    }

    // Inner class to handle follow-up death messages
    private static class PlayerDeathHandler {
        private static final long CLEANSED_DURATION = 200; // 10 seconds (200 ticks)

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event) {
            // ... (unchanged)
        }

        @SubscribeEvent
        public void onLivingDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (player.level().isClientSide) return;

            // Check if the player is marked as "cleansed"
            if (player.getPersistentData().contains("DivineLiberatorCleansed")) {
                long cleansedTime = player.getPersistentData().getLong("DivineLiberatorCleansed");
                long currentTime = player.level().getGameTime();
                if (currentTime - cleansedTime < CLEANSED_DURATION) {
                    // Get the attacker's name from PersistentData
                    String attackerName = player.getPersistentData().getString("DivineLiberatorAttacker");
                    Component attackerComponent = Component.literal(attackerName.isEmpty() ? "Unknown" : attackerName);

                    // Check if the damage source is the wave attack
                    DamageSource source = event.getSource();
                    if (source.getMsgId().equals("divine_liberator_wave")) {
                        // Let the translation key handle the death message
                        LOGGER.debug("Player killed by wave: {}", player.getName().getString());
                        return;
                    }

                    // Use translation key for the follow-up death message
                    Component newMessage = Component.translatable(
                            "death.attack.divine_liberator_escape",
                            player.getDisplayName(),
                            attackerComponent,
                            source.getLocalizedDeathMessage(player)
                    );
                    event.setCanceled(true); // Cancel the default death handling
                    player.setHealth(0); // Ensure the player dies
                    player.level().getServer().getPlayerList().broadcastSystemMessage(newMessage, false);
                    LOGGER.debug("Applied follow-up death message for player: {} by attacker: {}", player.getName().getString(), attackerName);
                }
            }

            // Always remove the tags on death
            player.getPersistentData().remove("DivineLiberatorCleansed");
            player.getPersistentData().remove("DivineLiberatorAttacker");
        }
    }
}