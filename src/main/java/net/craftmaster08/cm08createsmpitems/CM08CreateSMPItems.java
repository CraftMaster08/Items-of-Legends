package net.craftmaster08.cm08createsmpitems;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(CM08CreateSMPItems.MODID)
public class CM08CreateSMPItems {
    public static final String MODID = "cm08createsmpitems";

    // Register items
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> COOL_STICK = ITEMS.register("cool_stick",
            () -> new CoolStickItem(new Item.Properties()));
    public static final RegistryObject<Item> DIVINE_LIBERATOR = ITEMS.register("divine_liberator",
            () -> new DivineLiberatorItem(new Item.Properties()));
    public static final RegistryObject<Item> IMMORTAL_SHADOW = ITEMS.register("immortal_shadow",
            () -> new ImmortalShadowItem(new Item.Properties().durability(1500)));

    // Register creative mode tab
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> CUSTOM_TAB = TABS.register("cm08createsmpitems_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.cm08createsmpitems"))
                    .icon(() -> new ItemStack(DIVINE_LIBERATOR.get()))
                    .displayItems((parameters, output) -> {
                        ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                    })
                    .build());

    public CM08CreateSMPItems() {
        System.out.println("Initializing CM08 Create SMP Items mod");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onBlockBreak); // Register block break event
        modEventBus.addListener(this::onCommonSetup);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    private void onBlockBreak(BlockEvent.BreakEvent event) {
        // Check if the broken block is a leaf block
        if (event.getState().is(BlockTags.LEAVES)) {
            // 1/3000 chance to drop CoolStickItem
            if (event.getLevel().getRandom().nextFloat() < (1.0F / 3.0F)) {
                event.getLevel().addFreshEntity(
                        new ItemEntity(
                                (Level) event.getLevel(),
                                event.getPos().getX() + 0.5,
                                event.getPos().getY() + 0.5,
                                event.getPos().getZ() + 0.5,
                                new ItemStack(COOL_STICK.get())
                        )
                );
            }
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize the defense system
            SpecialAbilityDefenseSystem defenseSystem = new SpecialAbilityDefenseSystem();

            // Register special abilities
            SpecialAbilityDefenseSystem.registerSpecialAbility(
                    DIVINE_LIBERATOR.get(),
                    "divine_liberator_wave",
                    SpecialAbilityDefenseSystem.DIVINE_LIBERATOR_DEFENSE
            );
            SpecialAbilityDefenseSystem.registerSpecialAbility(
                    IMMORTAL_SHADOW.get(),
                    "immortal_shadow_strike",
                    (target, hurtEvent, level) -> {
                        // Handled in ImmortalShadowItem's TeleportHandler
                    }
            );
            SpecialAbilityDefenseSystem.registerSpecialAbility(
                    COOL_STICK.get(),
                    null, // Uses playerAttack, not a custom damage source
                    SpecialAbilityDefenseSystem.COOL_STICK_DEFENSE
            );
        });
    }
}