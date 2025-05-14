package net.craftmaster08.cm08createsmpitems;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(CM08CreateSMPItems.MODID)
public class CM08CreateSMPItems {
    public static final String MODID = "cm08createsmpitems";

    // Register items
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> CUSTOM_SWORD = ITEMS.register("custom_sword",
            () -> new CustomSwordItem(new Item.Properties()));
    public static final RegistryObject<Item> DIVINE_LIBERATOR = ITEMS.register("divine_liberator",
            () -> new DivineLiberatorItem(new Item.Properties()));

    // Register creative mode tab
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> CUSTOM_TAB = TABS.register("cm08createsmpitems_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.cm08createsmpitems"))
                    .icon(() -> new ItemStack(CUSTOM_SWORD.get()))
                    .displayItems((parameters, output) -> {
                        ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                    })
                    .build());

    public CM08CreateSMPItems() {
        System.out.println("Initializing CM08 Create SMP Items mod");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);

        // Register command handler with the Forge event bus
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}