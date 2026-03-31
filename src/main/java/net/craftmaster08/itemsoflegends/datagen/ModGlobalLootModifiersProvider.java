package net.craftmaster08.itemsoflegends.datagen;

import net.craftmaster08.itemsoflegends.ItemsOfLegends;
import net.craftmaster08.itemsoflegends.item.ModItems;
import net.craftmaster08.itemsoflegends.loot.AddItemModifier;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    public ModGlobalLootModifiersProvider(PackOutput output) {
        super(output, ItemsOfLegends.MODID);
    }

    @Override
    protected void start() {
        LOGGER.info("adding loot tables");

        add("cool_stick_from_acacia_leaves", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.ACACIA_LEAVES).build(),
                LootItemRandomChanceCondition.randomChance(0.01f).build()}, ModItems.COOL_STICK.get()));
        add("cool_stick_from_azalea_leaves", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.AZALEA_LEAVES).build(),
                LootItemRandomChanceCondition.randomChance(0.01f).build()}, ModItems.COOL_STICK.get()));
        add("cool_stick_from_birch_leaves", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.BIRCH_LEAVES).build(),
                LootItemRandomChanceCondition.randomChance(0.01f).build()}, ModItems.COOL_STICK.get()));
        add("cool_stick_from_cherry_leaves", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.CHERRY_LEAVES).build(),
                LootItemRandomChanceCondition.randomChance(0.01f).build()}, ModItems.COOL_STICK.get()));
        add("cool_stick_from_dark_oak_leaves", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.DARK_OAK_LEAVES).build(),
                LootItemRandomChanceCondition.randomChance(0.01f).build()}, ModItems.COOL_STICK.get()));
        add("cool_stick_from_flowering_azalea_leaves", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.FLOWERING_AZALEA_LEAVES).build(),
                LootItemRandomChanceCondition.randomChance(0.01f).build()}, ModItems.COOL_STICK.get()));
        add("cool_stick_from_jungle_leaves", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.JUNGLE_LEAVES).build(),
                LootItemRandomChanceCondition.randomChance(0.01f).build()}, ModItems.COOL_STICK.get()));
        add("cool_stick_from_mangrove_leaves", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.MANGROVE_LEAVES).build(),
                LootItemRandomChanceCondition.randomChance(0.01f).build()}, ModItems.COOL_STICK.get()));
        add("cool_stick_from_oak_leaves", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.OAK_LEAVES).build(),
                LootItemRandomChanceCondition.randomChance(0.01f).build()}, ModItems.COOL_STICK.get()));
        add("cool_stick_from_spruce_leaves", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SPRUCE_LEAVES).build(),
                LootItemRandomChanceCondition.randomChance(0.01f).build()}, ModItems.COOL_STICK.get()));
    }
}
