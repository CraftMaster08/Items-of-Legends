package net.craftmaster08.itemsoflegends.item.custom;

import net.craftmaster08.itemsoflegends.network.ModNetwork;
import net.craftmaster08.itemsoflegends.network.packet.S2COpenRealityFractureConfigPacket;
import net.craftmaster08.itemsoflegends.reality.RealityFractureClientState;
import net.craftmaster08.itemsoflegends.reality.RealityFractureMode;
import net.craftmaster08.itemsoflegends.reality.RealityFracturePlayerData;
import net.craftmaster08.itemsoflegends.reality.RealityFractureTeleportService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class RealityFractureItem extends Item {
    public RealityFractureItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.itemsoflegends.reality_fracture.description").withStyle(ChatFormatting.GRAY));

        if (!RealityFractureClientState.isConfigured()) {
            tooltip.add(Component.translatable("item.itemsoflegends.reality_fracture.tooltip.not_configured")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        tooltip.add(Component.translatable("item.itemsoflegends.reality_fracture.tooltip.configured")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.itemsoflegends.reality_fracture.tooltip.route",
                        RealityFractureClientState.getStartDimension(),
                        RealityFractureClientState.getDestinationDimension())
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || RealityFractureClientState.isConfigured();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        if (player.isShiftKeyDown()) {
            RealityFracturePlayerData.setStartDimension(serverPlayer, serverPlayer.serverLevel().dimension());
            RealityFracturePlayerData.ensureDefaults(serverPlayer);

            List<ResourceKey<Level>> dimensions = RealityFracturePlayerData.getAvailableDimensions(serverPlayer.server);
            ResourceKey<Level> startDimension = RealityFracturePlayerData.getStartDimension(serverPlayer, serverPlayer.serverLevel().dimension());

            ResourceKey<Level> destinationDimension = RealityFracturePlayerData.getDestinationDimensionOrNull(serverPlayer);
            if (destinationDimension == null || destinationDimension.equals(startDimension) || !dimensions.contains(destinationDimension)) {
                destinationDimension = dimensions.stream()
                        .filter(key -> !key.equals(startDimension))
                        .findFirst()
                        .orElse(startDimension);
            }

            RealityFractureMode mode = RealityFracturePlayerData.getMode(serverPlayer);

            ModNetwork.sendToPlayer(serverPlayer, new S2COpenRealityFractureConfigPacket(
                    startDimension.location().toString(),
                    destinationDimension.location().toString(),
                    mode.name(),
                    dimensions.stream().map(key -> key.location().toString()).toList()
            ));
            return InteractionResultHolder.consume(stack);
        }

        boolean success = RealityFractureTeleportService.teleport(serverPlayer);
        if (success) {
            serverPlayer.getCooldowns().addCooldown(this, 20);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.fail(stack);
    }
}
