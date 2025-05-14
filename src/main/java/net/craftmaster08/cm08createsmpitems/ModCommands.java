package net.craftmaster08.cm08createsmpitems;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.craftmaster08.cm08createsmpitems.util.WhitelistManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ModCommands {
    private static final SuggestionProvider<CommandSourceStack> WHITELIST_SUGGESTION_PROVIDER = (context, builder) -> {
        String itemName = context.getArgument("itemname", String.class);
        return SharedSuggestionProvider.suggest(WhitelistManager.getWhitelist(itemName), builder);
    };

    private static final SuggestionProvider<CommandSourceStack> ONLINE_PLAYER_SUGGESTION_PROVIDER = (context, builder) -> {
        return SharedSuggestionProvider.suggest(
                context.getSource().getServer().getPlayerList().getPlayers().stream()
                        .map(player -> player.getGameProfile().getName())
                        .toList(),
                builder
        );
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("createsmpitems")
                        .requires(source -> source.hasPermission(2)) // Requires OP level 2
                        .then(
                                Commands.literal("list")
                                        .executes(ModCommands::listItems)
                        )
                        .then(
                                Commands.literal("whitelist")
                                        .then(
                                                Commands.literal("list")
                                                        .then(
                                                                Commands.argument("itemname", StringArgumentType.word())
                                                                        .suggests((context, builder) -> {
                                                                            WhitelistManager.getValidItems().forEach(builder::suggest);
                                                                            return builder.buildFuture();
                                                                        })
                                                                        .executes(context -> listWhitelist(context, StringArgumentType.getString(context, "itemname")))
                                                        )
                                        )
                                        .then(
                                                Commands.literal("add")
                                                        .then(
                                                                Commands.argument("itemname", StringArgumentType.word())
                                                                        .suggests((context, builder) -> {
                                                                            WhitelistManager.getValidItems().forEach(builder::suggest);
                                                                            return builder.buildFuture();
                                                                        })
                                                                        .then(
                                                                                Commands.argument("playername", EntityArgument.player())
                                                                                        .suggests(ONLINE_PLAYER_SUGGESTION_PROVIDER)
                                                                                        .executes(context -> addToWhitelist(
                                                                                                context,
                                                                                                StringArgumentType.getString(context, "itemname"),
                                                                                                EntityArgument.getPlayer(context, "playername").getGameProfile().getName()
                                                                                        ))
                                                                        )
                                                        )
                                        )
                                        .then(
                                                Commands.literal("remove")
                                                        .then(
                                                                Commands.argument("itemname", StringArgumentType.word())
                                                                        .suggests((context, builder) -> {
                                                                            WhitelistManager.getValidItems().forEach(builder::suggest);
                                                                            return builder.buildFuture();
                                                                        })
                                                                        .then(
                                                                                Commands.argument("playername", StringArgumentType.word())
                                                                                        .suggests(WHITELIST_SUGGESTION_PROVIDER)
                                                                                        .executes(context -> removeFromWhitelist(
                                                                                                context,
                                                                                                StringArgumentType.getString(context, "itemname"),
                                                                                                StringArgumentType.getString(context, "playername")
                                                                                        ))
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static int listItems(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String items = String.join(", ", WhitelistManager.getValidItems());
        source.sendSuccess(() -> Component.translatable("commands.cm08createsmpitems.list.success", items), false);
        return 1;
    }

    private static int listWhitelist(CommandContext<CommandSourceStack> context, String itemName) {
        CommandSourceStack source = context.getSource();
        if (!WhitelistManager.getValidItems().contains(itemName)) {
            source.sendFailure(Component.translatable("commands.cm08createsmpitems.invalid_item", itemName));
            return 0;
        }
        List<String> whitelist = WhitelistManager.getWhitelist(itemName);
        if (whitelist.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.cm08createsmpitems.whitelist.list.empty", itemName), false);
        } else {
            String players = String.join(", ", whitelist);
            source.sendSuccess(() -> Component.translatable("commands.cm08createsmpitems.whitelist.list.success", itemName, players), false);
        }
        return 1;
    }

    private static int addToWhitelist(CommandContext<CommandSourceStack> context, String itemName, String playerName) {
        CommandSourceStack source = context.getSource();
        if (!WhitelistManager.getValidItems().contains(itemName)) {
            source.sendFailure(Component.translatable("commands.cm08createsmpitems.invalid_item", itemName));
            return 0;
        }
        if (WhitelistManager.addPlayerToWhitelist(itemName, playerName)) {
            source.sendSuccess(() -> Component.translatable("commands.cm08createsmpitems.whitelist.add.success", playerName, itemName), false);
            return 1;
        } else {
            source.sendFailure(Component.translatable("commands.cm08createsmpitems.whitelist.add.failure", playerName, itemName));
            return 0;
        }
    }

    private static int removeFromWhitelist(CommandContext<CommandSourceStack> context, String itemName, String playerName) {
        CommandSourceStack source = context.getSource();
        if (!WhitelistManager.getValidItems().contains(itemName)) {
            source.sendFailure(Component.translatable("commands.cm08createsmpitems.invalid_item", itemName));
            return 0;
        }
        if (WhitelistManager.removePlayerFromWhitelist(itemName, playerName)) {
            source.sendSuccess(() -> Component.translatable("commands.cm08createsmpitems.whitelist.remove.success", playerName, itemName), false);
            return 1;
        } else {
            source.sendFailure(Component.translatable("commands.cm08createsmpitems.whitelist.remove.failure", playerName, itemName));
            return 0;
        }
    }
}