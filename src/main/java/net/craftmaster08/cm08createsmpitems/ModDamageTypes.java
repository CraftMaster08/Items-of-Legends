package net.craftmaster08.cm08createsmpitems;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModDamageTypes {
    private static final Logger LOGGER = LogManager.getLogger();

    // Define the ResourceKey for the DamageType
    public static final ResourceKey<DamageType> DIVINE_LIBERATOR_WAVE_KEY = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(CM08CreateSMPItems.MODID, "divine_liberator_wave")
    );

    // Method to create a DamageSource using the custom DamageType
    public static DamageSource waveAttack(Level level, Entity source) {
        Holder<DamageType> damageTypeHolder = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DIVINE_LIBERATOR_WAVE_KEY);
        return new DamageSource(damageTypeHolder, source);
    }
}