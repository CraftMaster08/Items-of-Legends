package net.craftmaster08.itemsoflegends;

import net.craftmaster08.itemsoflegends.damage.SpecialAbilityDefenseSystem;
import net.craftmaster08.itemsoflegends.item.ModItems;
import net.craftmaster08.itemsoflegends.loot.ModLootModifiers;
import net.craftmaster08.itemsoflegends.util.ModCommands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(ItemsOfLegends.MODID)
public class ItemsOfLegends {
    public static final String MODID = "itemsoflegends";

    // Register creative mode tab
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> CUSTOM_TAB = TABS.register("itemsoflegends_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.itemsoflegends"))
                    .icon(() -> new ItemStack(ModItems.DIVINE_LIBERATOR.get()))
                    .displayItems((parameters, output) -> {
                        ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                    })
                    .build());

    public ItemsOfLegends() {
        System.out.println("Initializing Items of Legends mod");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);

        TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        ModLootModifiers.register(modEventBus);

        modEventBus.addListener(this::onCommonSetup);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize the defense system
            SpecialAbilityDefenseSystem defenseSystem = new SpecialAbilityDefenseSystem();

            // Register special abilities
            SpecialAbilityDefenseSystem.registerSpecialAbility(
                    ModItems.DIVINE_LIBERATOR.get(),
                    "divine_liberator_wave",
                    SpecialAbilityDefenseSystem.DIVINE_LIBERATOR_DEFENSE
            );
            SpecialAbilityDefenseSystem.registerSpecialAbility(
                    ModItems.IMMORTAL_SHADOW.get(),
                    "immortal_shadow_strike",
                    (target, hurtEvent, level) -> {
                        // Handled in ImmortalShadowItem's TeleportHandler
                    }
            );
            SpecialAbilityDefenseSystem.registerSpecialAbility(
                    ModItems.COOL_STICK.get(),
                    null, // Uses playerAttack, not a custom damage source
                    SpecialAbilityDefenseSystem.COOL_STICK_DEFENSE
            );
        });
    }
}