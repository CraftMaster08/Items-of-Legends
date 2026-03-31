package net.craftmaster08.itemsoflegends.item;

import net.craftmaster08.itemsoflegends.item.custom.CoolStickItem;
import net.craftmaster08.itemsoflegends.item.custom.DivineLiberatorItem;
import net.craftmaster08.itemsoflegends.item.custom.ImmortalShadowItem;
import net.craftmaster08.itemsoflegends.item.custom.RealityFractureItem;
import net.craftmaster08.itemsoflegends.ItemsOfLegends;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ItemsOfLegends.MODID);

    public static final RegistryObject<Item> COOL_STICK = ITEMS.register("cool_stick",
            () -> new CoolStickItem(new Item.Properties()));
    public static final RegistryObject<Item> DIVINE_LIBERATOR = ITEMS.register("divine_liberator",
            () -> new DivineLiberatorItem(new Item.Properties()));
    public static final RegistryObject<Item> IMMORTAL_SHADOW = ITEMS.register("immortal_shadow",
            () -> new ImmortalShadowItem(new Item.Properties().durability(1500)));
    public static final RegistryObject<Item> REALITY_FRACTURE = ITEMS.register("reality_fracture",
            () -> new RealityFractureItem(new Item.Properties()));

    public static void register(IEventBus eventbus) {
        ITEMS.register(eventbus);
    }
}
