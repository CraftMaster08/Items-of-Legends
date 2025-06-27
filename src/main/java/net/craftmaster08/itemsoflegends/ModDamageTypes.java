package net.craftmaster08.itemsoflegends;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModDamageTypes {
    private static final Logger LOGGER = LogManager.getLogger();

    // Define the ResourceKey for Divine Liberator Wave DamageType
    public static final ResourceKey<DamageType> DIVINE_LIBERATOR_WAVE_KEY = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(ItemsOfLegends.MODID, "divine_liberator_wave")
    );

    // Define the ResourceKey for Immortal Shadow Strike DamageType
    public static final ResourceKey<DamageType> IMMORTAL_SHADOW_STRIKE_KEY = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(ItemsOfLegends.MODID, "immortal_shadow_strike")
    );

    public static DamageSource waveAttack(Level level, Entity attacker, ItemStack item) {
        DamageSource source = new CustomDamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DIVINE_LIBERATOR_WAVE_KEY),
                attacker,
                item
        );
        LOGGER.debug("Created waveAttack DamageSource with attacker: {}, item: {}",
                attacker != null ? attacker.getName().getString() : "null",
                item != null ? item.getDisplayName().getString() : "null");
        return source;
    }

    public static DamageSource shadowStrike(Level level, Entity attacker, ItemStack item) {
        DamageSource source = new CustomDamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(IMMORTAL_SHADOW_STRIKE_KEY),
                attacker,
                item
        );
        LOGGER.debug("Created shadowStrike DamageSource with attacker: {}, item: {}",
                attacker != null ? attacker.getName().getString() : "null",
                item != null ? item.getDisplayName().getString() : "null");
        return source;
    }

    private static class CustomDamageSource extends DamageSource {
        private final ItemStack item;

        public CustomDamageSource(Holder<DamageType> damageType, Entity attacker, ItemStack item) {
            super(damageType, attacker);
            this.item = item != null ? item.copy() : ItemStack.EMPTY;
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity entity) {
            Component itemName = item.isEmpty() ? Component.literal("Unknown Item") : item.getDisplayName();
            Component attackerName = this.getEntity() != null ? this.getEntity().getDisplayName() : Component.literal("Unknown");
            Component victimName = entity.getDisplayName();
            String translationKey = "death.attack." + this.getMsgId();
            return Component.translatable(translationKey, victimName, attackerName, itemName);
        }
    }
}