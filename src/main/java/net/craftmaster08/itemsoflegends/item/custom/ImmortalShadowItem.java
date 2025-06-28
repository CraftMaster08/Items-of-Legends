package net.craftmaster08.itemsoflegends.item.custom;

import net.craftmaster08.itemsoflegends.damage.ModDamageTypes;
import net.craftmaster08.itemsoflegends.item.ModItems;
import net.craftmaster08.itemsoflegends.util.WhitelistManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImmortalShadowItem extends SwordItem {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int CHARGE_TIME = 10; // 0.5 seconds (10 ticks)
    private static final int COOLDOWN_TICKS = 100; // 5 seconds
    private static final int MAX_TELEPORTS = 12;
    private static final double MAX_RANGE = 50.0;
    private static final int TELEPORT_INTERVAL = 4; // 0.2 seconds (4 ticks)
    private static final int INVULNERABILITY_TICKS = 40; // 2 seconds
    private static final DustColorTransitionOptions SHADOW_TRAIL = new DustColorTransitionOptions(
            new Vector3f(0.0F, 0.0F, 0.0F), // Black
            new Vector3f(0.5F, 0.0F, 0.5F), // Purple
            0.8F
    );

    public ImmortalShadowItem(Properties properties) {
        super(Tiers.DIAMOND, 6, -2.4F, properties);
        MinecraftForge.EVENT_BUS.register(new InvincibilityHandler());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            ServerLevel level = (ServerLevel) attacker.level();
            // Play new sounds
            level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 0.9F);
            level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 0.8F, 1.0F);
            // Add shadow-themed particles
            Vec3 pos = target.getPosition(1.0F).add(0, target.getBbHeight() / 2.0, 0);
            for (int i = 0; i < 20; i++) {
                double theta = level.random.nextDouble() * 2 * Math.PI;
                double radius = 0.8 + level.random.nextDouble() * 0.4;
                double offsetX = Math.cos(theta) * radius;
                double offsetZ = Math.sin(theta) * radius;
                level.sendParticles(ParticleTypes.SOUL,
                        pos.x + offsetX, pos.y + (level.random.nextDouble() - 0.5) * 0.5, pos.z + offsetZ,
                        1, 0.1, 0.1, 0.1, 0.05);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        pos.x + offsetX, pos.y + (level.random.nextDouble() - 0.5) * 0.5, pos.z + offsetZ,
                        1, 0.1, 0.1, 0.1, 0.05);
            }
            level.sendParticles(ParticleTypes.SCULK_SOUL,
                    pos.x, pos.y, pos.z, 10, 0.3, 0.3, 0.3, 0.0);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack))
                .setStyle(Style.EMPTY
                        .withColor(ChatFormatting.DARK_GRAY)
                        .withBold(true)
                        .withItalic(true));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.itemsoflegends.immortal_shadow.description"));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!WhitelistManager.isPlayerWhitelisted(player, "immortal_shadow")) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("item.itemsoflegends.not_whitelisted"));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.0F);
            }
            return InteractionResultHolder.fail(stack);
        }

        LivingEntity target = getTargetEntity(player, level);
        if (target == null) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("item.itemsoflegends.immortal_shadow.no_target"));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.6F, 1.1F);
            }
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if (!(entity instanceof Player player) || level.isClientSide) return;

        int useTime = getUseDuration(stack) - remainingUseTicks;
        ServerLevel serverLevel = (ServerLevel) level;

        // Apply levitation for immersion
        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 2, 2, false, false, false));

        // Charging trail and energy collection particles
        for (int i = 0; i < 20; i++) {
            double angle = level.random.nextDouble() * 2 * Math.PI;
            double radius = 2.5 + level.random.nextDouble() * 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetY = 1.2 + (level.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = Math.sin(angle) * radius;
            double particleX = player.getX() + offsetX;
            double particleY = player.getY() + offsetY;
            double particleZ = player.getZ() + offsetZ;
            double velX = (player.getX() - particleX) * 0.15;
            double velY = (player.getY() + 1.2 - particleY) * 0.15;
            double velZ = (player.getZ() - particleZ) * 0.15;
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    particleX, particleY, particleZ, 1, velX, velY, velZ, 0.05);
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    particleX, particleY, particleZ, 1, velX, velY, velZ, 0.1);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    particleX, particleY, particleZ, 1, velX, velY, velZ, 0.03);
        }

        // Swirling trail around player
        for (int i = 0; i < 15; i++) {
            double angle = (level.random.nextDouble() * 2 * Math.PI) + (useTime * 0.5);
            double radius = 1.0;
            double offsetX = Math.cos(angle) * radius;
            double offsetY = level.random.nextDouble() * 0.5 + 1.0;
            double offsetZ = Math.sin(angle) * radius;
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ, 1, 0.0, 0.05, 0.0, 0.0);
        }

        // Predict and display exact teleport path
        LivingEntity target = getTargetEntity(player, level);
        if (target != null) {
            // Remove glowing from previous target, if any
            LivingEntity prevTarget = player.getPersistentData().getInt("ImmortalShadowGlowingEntity") > 0
                    ? (LivingEntity) level.getEntity(player.getPersistentData().getInt("ImmortalShadowGlowingEntity"))
                    : null;
            if (prevTarget != null && prevTarget != target) {
                prevTarget.removeEffect(MobEffects.GLOWING);
            }

            // Apply glowing to current target
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false, false));
            player.getPersistentData().putInt("ImmortalShadowGlowingEntity", target.getId());

            Vec3 playerPos = player.getPosition(1.0F);
            Vec3 targetPos = target.getPosition(1.0F);
            double distance = playerPos.distanceTo(targetPos);
            // Dynamic teleport count: at least 1, ~1 per 3 blocks, capped at MAX_TELEPORTS
            int teleportsNeeded = Math.min(MAX_TELEPORTS, Math.max(1, (int) Math.ceil(distance / 3.0)));
            List<Vec3> teleportPath = new ArrayList<>();

            // Calculate exact path
            for (int i = 1; i < teleportsNeeded; i++) {
                double t = (double) i / teleportsNeeded;
                Vec3 basePos = playerPos.lerp(targetPos, t);
                double offset = (level.random.nextDouble() - 0.5) * 7.0;
                Vec3 toTarget = targetPos.subtract(playerPos).normalize();
                Vec3 perp = new Vec3(-toTarget.z, 0, toTarget.x).normalize();
                basePos = basePos.add(perp.scale(offset)).add(toTarget.scale((level.random.nextDouble() - 0.5) * 2.0));
                Vec3 safePos = findSafeTeleportPosition(basePos, level, 1);
                if (safePos != null) {
                    teleportPath.add(safePos);
                }
            }

            // Store path in player data
            player.getPersistentData().putInt("ImmortalShadowPathSize", teleportPath.size());
            for (int i = 0; i < teleportPath.size(); i++) {
                Vec3 pos = teleportPath.get(i);
                player.getPersistentData().putDouble("ImmortalShadowPathX" + i, pos.x);
                player.getPersistentData().putDouble("ImmortalShadowPathY" + i, pos.y);
                player.getPersistentData().putDouble("ImmortalShadowPathZ" + i, pos.z);
            }

            // Spawn particles along exact path
            for (int i = 0; i < teleportPath.size(); i++) {
                Vec3 pos = teleportPath.get(i);
                Vec3 prevPos = i == 0 ? playerPos : teleportPath.get(i - 1);
                Vec3 direction = pos.subtract(prevPos);
                double pathDistance = direction.length();
                Vec3 step = direction.normalize().scale(0.3);
                int steps = (int) (pathDistance / 0.3);

                for (int j = 0; j < steps; j++) {
                    Vec3 particlePos = prevPos.add(step.scale(j));
                    serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
                            particlePos.x, particlePos.y + 0.5, particlePos.z, 1, 0.05, 0.05, 0.05, 0.0);
                }
                serverLevel.sendParticles(ParticleTypes.WITCH,
                        pos.x, pos.y + 1.0, pos.z, 5, 0.1, 0.1, 0.1, 0.0);
            }
        }

        // Charging sounds
        if (useTime % 2 == 0) {
            float pitch = 0.8F + (float) useTime / CHARGE_TIME * 0.4F;
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.6F, 0.7F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.8F, pitch);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.0F);
            player.hurt(level.damageSources().generic(), 0.0F);
        }

        // Start teleportation sequence after charge
        if (useTime >= CHARGE_TIME) {
            player.stopUsingItem();
            startTeleportSequence(player, level, stack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || level.isClientSide) return;

        int useTime = getUseDuration(stack) - timeLeft;
        if (useTime < CHARGE_TIME) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 1.0F, 0.5F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.3F, 0.8F);
            // Remove glowing from target on cancel
            LivingEntity target = player.getPersistentData().getInt("ImmortalShadowGlowingEntity") > 0
                    ? (LivingEntity) level.getEntity(player.getPersistentData().getInt("ImmortalShadowGlowingEntity"))
                    : null;
            if (target != null) {
                target.removeEffect(MobEffects.GLOWING);
                player.getPersistentData().remove("ImmortalShadowGlowingEntity");
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    private LivingEntity getTargetEntity(Player player, Level level) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle();
        double maxRange = MAX_RANGE;
        AABB aabb = player.getBoundingBox().expandTowards(lookDir.scale(maxRange)).inflate(2.0);

        List<LivingEntity> candidates = new ArrayList<>();
        for (Entity entity : level.getEntities(player, aabb, e -> e instanceof LivingEntity)) {
            if (!(entity instanceof LivingEntity living)) continue;
            // Skip tamed pets and horses
            if (entity instanceof TamableAnimal tamable && tamable.isTame()) continue;
            if (entity instanceof AbstractHorse horse && horse.isTamed()) continue;
            Vec3 entityPos = living.getPosition(1.0F);
            Vec3 toEntity = entityPos.subtract(eyePos);
            double dist = toEntity.length();
            if (dist > maxRange) continue;

            double dot = toEntity.normalize().dot(lookDir);
            if (dot > 0.99) { // Narrowed to ~8°
                candidates.add(living);
            }
        }

        // Sort by angular proximity to look direction
        return candidates.stream()
                .max(Comparator.comparingDouble(living -> {
                    Vec3 toEntity = living.getPosition(1.0F).subtract(eyePos).normalize();
                    return toEntity.dot(lookDir);
                }))
                .orElse(null);
    }

    private void startTeleportSequence(Player player, Level level, ItemStack stack) {
        if (level.isClientSide) return;

        LivingEntity target = getTargetEntity(player, level);
        if (target == null) {
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            return;
        }

        Vec3 targetPos = target.getPosition(1.0F);
        Vec3 behindTarget = targetPos.subtract(target.getLookAngle().scale(1.5));
        Vec3 newPos = findSafeTeleportPosition(behindTarget, level, 1);
        if (newPos == null) {
            TeleportHandler.handleTeleportFailure(player, level, stack);
            return;
        }

        player.getPersistentData().putBoolean("ImmortalShadowInvincible", true);
        new TeleportHandler(player, target, level, stack);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
    }

    private static Vec3 findSafeTeleportPosition(Vec3 basePos, Level level, int radius) {
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
                        return new Vec3(testPos.getX() + 0.5, belowPos.getY() + 1.0, testPos.getZ() + 0.5);
                    }
                }
            }
        }
        if (radius < 4) {
            return findSafeTeleportPosition(basePos, level, radius * 2);
        }
        return null;
    }

    private static class TeleportHandler {
        private final Player player;
        private final LivingEntity target;
        private final ServerLevel level;
        private final ItemStack stack;
        private int teleportCount;
        private Vec3 lastPos;
        private final Set<Vec3> teleportPositions;
        private long nextTeleportTick;
        private boolean isActive;
        private final List<Vec3> teleportPath;

        public TeleportHandler(Player player, LivingEntity target, Level level, ItemStack stack) {
            this.player = player;
            this.target = target;
            this.level = (ServerLevel) level;
            this.stack = stack;
            this.teleportCount = 0;
            this.lastPos = player.getPosition(1.0F);
            this.teleportPositions = new HashSet<>();
            this.nextTeleportTick = level.getServer().getTickCount() + TELEPORT_INTERVAL;
            this.isActive = true;

            // Load pre-calculated teleport path
            this.teleportPath = new ArrayList<>();
            int pathSize = player.getPersistentData().getInt("ImmortalShadowPathSize");
            for (int i = 0; i < pathSize; i++) {
                double x = player.getPersistentData().getDouble("ImmortalShadowPathX" + i);
                double y = player.getPersistentData().getDouble("ImmortalShadowPathY" + i);
                double z = player.getPersistentData().getDouble("ImmortalShadowPathZ" + i);
                teleportPath.add(new Vec3(x, y, z));
            }

            // Check if target is holding ImmortalShadowItem and start defense
            if (isHoldingImmortalShadow(target)) {
                target.getPersistentData().putBoolean("ImmortalShadowDefending", true);
                // Start background sound
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 0.6F, 0.8F);
            }

            LOGGER.info("Initialized TeleportHandler at tick {}, first teleport scheduled for tick {}", level.getServer().getTickCount(), nextTeleportTick);
            MinecraftForge.EVENT_BUS.register(new TickHandler(this));
        }

        private boolean isHoldingImmortalShadow(LivingEntity entity) {
            if (!(entity instanceof Player player)) return false;
            return player.getMainHandItem().getItem() == ModItems.IMMORTAL_SHADOW.get() ||
                    player.getOffhandItem().getItem() == ModItems.IMMORTAL_SHADOW.get();
        }

        private void performTeleport() {
            LOGGER.info("Performing teleport #{} at tick {}", teleportCount + 1, level.getServer().getTickCount());
            if (teleportCount >= MAX_TELEPORTS || target.isRemoved() || player.distanceTo(target) > MAX_RANGE) {
                performFinalTeleport();
                isActive = false;
                return;
            }

            if (teleportCount < teleportPath.size()) {
                Vec3 newPos = teleportPath.get(teleportCount);
                spawnTrailParticles(lastPos, newPos);
                teleportPlayer(newPos);
                teleportPositions.add(newPos);
                lastPos = newPos;
                teleportCount++;
                nextTeleportTick = level.getServer().getTickCount() + TELEPORT_INTERVAL;
                LOGGER.info("Scheduling next teleport at tick {}", nextTeleportTick);
            } else {
                performFinalTeleport();
                isActive = false;
            }
        }

        private void performFinalTeleport() {
            LOGGER.info("Performing final teleport at tick {}", level.getServer().getTickCount());
            player.getPersistentData().remove("ImmortalShadowInvincible");

            Vec3 targetPos = target.getPosition(1.0F);
            Vec3 behindTarget = targetPos.subtract(target.getLookAngle().scale(1.5));
            Vec3 newPos = findSafeTeleportPosition(behindTarget, level, 1);

            if (newPos == null) {
                handleTeleportFailure(player, level, stack);
                return;
            }

            spawnTrailParticles(lastPos, newPos);
            teleportPlayer(newPos);

            // Check if target is holding ImmortalShadowItem
            if (isHoldingImmortalShadow(target)) {
                // Play dramatic defense sounds
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 0.8F);
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.8F, 0.9F);
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 0.7F, 1.0F);
                // Add epic defense particles
                double x = target.getX(), y = target.getY() + 1.0, z = target.getZ();
                for (int i = 0; i < 40; i++) {
                    double theta = level.random.nextDouble() * 2 * Math.PI;
                    double phi = Math.acos(2 * level.random.nextDouble() - 1);
                    double r = 1.2 + level.random.nextDouble() * 0.5;
                    double offsetX = r * Math.sin(phi) * Math.cos(theta);
                    double offsetY = r * Math.sin(phi) * Math.sin(theta);
                    double offsetZ = r * Math.cos(phi);
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            x + offsetX, y + offsetY, z + offsetZ, 1, 0.1, 0.1, 0.1, 0.05);
                    level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                            x + offsetX, y + offsetY, z + offsetZ, 1, 0.1, 0.1, 0.1, 0.05);
                    level.sendParticles(ParticleTypes.DRAGON_BREATH,
                            x + offsetX, y + offsetY, z + offsetZ, 1, 0.1, 0.1, 0.1, 0.05);
                }
                level.sendParticles(ParticleTypes.SOUL,
                        x, y, z, 30, 0.5, 0.5, 0.5, 0.1);
                level.sendParticles(ParticleTypes.LARGE_SMOKE,
                        x, y, z, 20, 0.4, 0.4, 0.4, 0.0);
                target.getPersistentData().remove("ImmortalShadowDefending");
            } else {
                // Apply damage with ModDamageTypes, including ItemStack
                target.hurt(ModDamageTypes.shadowStrike(level, player, stack), 40.0F);
                LOGGER.debug("Applied shadowStrike damage to {} with item {}", target.getName().getString(), stack.getDisplayName().getString());

                // Check for nearby entities to protect tamed pets
                AABB aabb = new AABB(target.getX() - 2.0, target.getY() - 2.0, target.getZ() - 2.0,
                        target.getX() + 2.0, target.getY() + 2.0, target.getZ() + 2.0);
                for (Entity entity : level.getEntities(player, aabb, e -> e instanceof LivingEntity)) {
                    if (entity instanceof TamableAnimal tamable && tamable.isTame()) continue;
                    if (entity instanceof AbstractHorse horse && horse.isTamed()) continue;
                    if (entity instanceof LivingEntity living && entity != target) {
                        living.hurt(ModDamageTypes.shadowStrike(level, player, stack), 12.0F);
                        LOGGER.debug("Applied splash shadowStrike damage to {} with item {}", living.getName().getString(), stack.getDisplayName().getString());
                    }
                }

                // Final particles
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 30, 0.5, 0.5, 0.5, 0.2);
                level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                level.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.3, 0.3, 0.0);
                level.sendParticles(ParticleTypes.DRAGON_BREATH, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.4, 0.4, 0.4, 0.05);

                // Final sounds
                level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.9F);
                level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.8F, 0.8F);
                level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.7F, 0.9F);
                level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.6F, 0.8F);

                // Reduce durability
                if (!player.isCreative()) {
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
            }

            // Remove glowing
            target.removeEffect(MobEffects.GLOWING);
            player.getPersistentData().remove("ImmortalShadowGlowingEntity");
        }

        private void teleportPlayer(Vec3 pos) {
            player.teleportTo(pos.x, pos.y, pos.z);

            // Rotate to face target
            Vec3 targetPos = target.getPosition(1.0F);
            Vec3 toTarget = targetPos.subtract(pos).normalize();
            player.setYRot((float) (Math.atan2(toTarget.z, toTarget.x) * 180 / Math.PI - 90));
            player.setYHeadRot(player.getYRot());

            // Player location particles
            level.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y + 1.0, pos.z, 8, 0.2, 0.2, 0.2, 0.0);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, pos.x, pos.y + 1.0, pos.z, 12, 0.3, 0.3, 0.3, 0.05);
            for (int i = 0; i < 10; i++) {
                double angle = level.random.nextDouble() * 2 * Math.PI;
                double radius = 0.6;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                level.sendParticles(ParticleTypes.WITCH,
                        pos.x + offsetX, pos.y + 1.0 + (level.random.nextDouble() * 0.5), pos.z + offsetZ, 1, 0.1, 0.1, 0.1, 0.0);
            }

            // Teleport sounds
            float pitch = 0.8F + level.random.nextFloat() * 0.2F;
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_SCREAM, SoundSource.PLAYERS, 0.7F, pitch);
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8F, 0.9F);
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.PLAYERS, 0.6F, pitch);
        }

        private void spawnTrailParticles(Vec3 from, Vec3 to) {
            Vec3 direction = to.subtract(from);
            double distance = direction.length();
            Vec3 step = direction.normalize().scale(0.2);
            int steps = (int) (distance / 0.2);

            for (int i = 0; i < steps; i++) {
                Vec3 pos = from.add(step.scale(i));
                level.sendParticles(SHADOW_TRAIL, pos.x, pos.y + 0.5, pos.z, 3, 0.05, 0.05, 0.05, 0.0);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.x, pos.y + 0.5, pos.z, 2, 0.05, 0.05, 0.05, 0.0);
            }
        }

        private static void handleTeleportFailure(Player player, Level level, ItemStack stack) {
            if (level.isClientSide) return;

            ServerLevel serverLevel = (ServerLevel) level;
            player.getPersistentData().remove("ImmortalShadowInvincible");
            player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS);

            // Failure message
            player.sendSystemMessage(Component.translatable("item.itemsoflegends.immortal_shadow.no_safe_spot"));

            // Failure sounds
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 0.8F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8F, 0.7F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITHER_BREAK_BLOCK, SoundSource.PLAYERS, 0.7F, 0.9F);

            // Epic failure particles
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.3, 0.3, 0.3, 0.05);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.4, 0.4, 0.4, 0.05);

            // Remove glowing from target
            LivingEntity target = player.getPersistentData().getInt("ImmortalShadowGlowingEntity") > 0
                    ? (LivingEntity) level.getEntity(player.getPersistentData().getInt("ImmortalShadowGlowingEntity"))
                    : null;
            if (target != null) {
                target.removeEffect(MobEffects.GLOWING);
                player.getPersistentData().remove("ImmortalShadowGlowingEntity");
            }
        }

        private static class TickHandler {
            private final TeleportHandler handler;

            public TickHandler(TeleportHandler handler) {
                this.handler = handler;
            }

            @SubscribeEvent
            public void onServerTick(TickEvent.ServerTickEvent event) {
                if (event.phase == TickEvent.Phase.END && handler.isActive) {
                    long currentTick = handler.level.getServer().getTickCount();
                    // Spawn particle shield for defending target
                    if (handler.target.getPersistentData().getBoolean("ImmortalShadowDefending")) {
                        double x = handler.target.getX(), y = handler.target.getY() + 1.0, z = handler.target.getZ();
                        for (int i = 0; i < 20; i++) {
                            double theta = handler.level.random.nextDouble() * 2 * Math.PI;
                            double phi = Math.acos(2 * handler.level.random.nextDouble() - 1);
                            double r = 0.8;
                            double offsetX = r * Math.sin(phi) * Math.cos(theta);
                            double offsetY = r * Math.sin(phi) * Math.sin(theta);
                            double offsetZ = r * Math.cos(phi);
                            handler.level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                                    x + offsetX, y + offsetY, z + offsetZ, 1, 0.1, 0.1, 0.1, 0.05);
                            handler.level.sendParticles(ParticleTypes.PORTAL,
                                    x + offsetX, y + offsetY, z + offsetZ, 1, 0.1, 0.1, 0.1, 0.05);
                        }
                    }
                    if (currentTick >= handler.nextTeleportTick) {
                        handler.performTeleport();
                    }
                }
            }
        }
    }

    private static class InvincibilityHandler {
        @SubscribeEvent
        public void onLivingHurt(LivingHurtEvent event) {
            if (event.getEntity() instanceof Player player) {
                if (player.getPersistentData().getBoolean("ImmortalShadowInvincible")) {
                    event.setCanceled(true);
                }
            }
        }
    }
}