package net.craftmaster08.itemsoflegends.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class WhitelistManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String WHITELIST_FILE = "config/itemsoflegends/whitelist.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String COOL_STICK_KEY = "cool_stick";
    private static final String LEGACY_COOL_STICK_KEY = "custom_sword";
    private static final Map<String, Set<String>> WHITELISTS = new HashMap<>();
    private static final Set<String> VALID_ITEMS = new HashSet<>(Arrays.asList("divine_liberator", COOL_STICK_KEY, LEGACY_COOL_STICK_KEY, "immortal_shadow", "reality_fracture"));

    // Load the whitelist file on initialization
    static {
        loadWhitelist();
    }

    // Check if a player is whitelisted for an item
    public static boolean isPlayerWhitelisted(Player player, String itemName) {
        Set<String> whitelist = new HashSet<>(WHITELISTS.getOrDefault(itemName, new HashSet<>()));
        // Backward compatibility for older configs that used "custom_sword" for the Cool Stick.
        if (COOL_STICK_KEY.equals(itemName) && whitelist.isEmpty()) {
            whitelist.addAll(WHITELISTS.getOrDefault(LEGACY_COOL_STICK_KEY, new HashSet<>()));
        }
        boolean isWhitelisted = whitelist.contains(player.getName().getString());
        LOGGER.debug("Checking whitelist for item '{}': Player '{}', Whitelist: {}, Allowed: {}",
                itemName, player.getName().getString(), whitelist, isWhitelisted);
        return isWhitelisted;
    }

    // Add a player to an item's whitelist
    public static boolean addPlayerToWhitelist(String itemName, String playerName) {
        if (!VALID_ITEMS.contains(itemName)) {
            LOGGER.warn("Attempted to add player '{}' to invalid item '{}'", playerName, itemName);
            return false;
        }
        WHITELISTS.computeIfAbsent(itemName, k -> new HashSet<>()).add(playerName);
        saveWhitelist();
        LOGGER.info("Added player '{}' to whitelist for item '{}'", playerName, itemName);
        return true;
    }

    // Remove a player from an item's whitelist
    public static boolean removePlayerFromWhitelist(String itemName, String playerName) {
        if (!VALID_ITEMS.contains(itemName)) {
            LOGGER.warn("Attempted to remove player '{}' from invalid item '{}'", playerName, itemName);
            return false;
        }
        Set<String> whitelist = WHITELISTS.get(itemName);
        if (whitelist != null) {
            boolean removed = whitelist.remove(playerName);
            if (whitelist.isEmpty()) {
                WHITELISTS.remove(itemName);
            }
            saveWhitelist();
            if (removed) {
                LOGGER.info("Removed player '{}' from whitelist for item '{}'", playerName, itemName);
            } else {
                LOGGER.debug("Player '{}' was not in whitelist for item '{}'", playerName, itemName);
            }
            return removed;
        }
        LOGGER.debug("No whitelist found for item '{}'", itemName);
        return false;
    }

    // Get the list of whitelisted players for an item
    public static List<String> getWhitelist(String itemName) {
        return new ArrayList<>(WHITELISTS.getOrDefault(itemName, new HashSet<>()));
    }

    // Get the list of valid item names
    public static Set<String> getValidItems() {
        return VALID_ITEMS;
    }

    // Load the whitelist from the JSON file
    private static void loadWhitelist() {
        File file = new File(WHITELIST_FILE);
        if (!file.exists()) {
            createDefaultWhitelistFile();
            return;
        }

        try (Reader reader = Files.newBufferedReader(Paths.get(WHITELIST_FILE))) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            for (String itemName : json.keySet()) {
                if (VALID_ITEMS.contains(itemName)) {
                    Set<String> players = new HashSet<>();
                    json.getAsJsonArray(itemName).forEach(element -> players.add(element.getAsString()));
                    WHITELISTS.put(itemName, players);
                    LOGGER.debug("Loaded whitelist for item '{}': {}", itemName, players);
                }
            }
            LOGGER.info("Loaded whitelist from {}", WHITELIST_FILE);
        } catch (IOException e) {
            LOGGER.error("Failed to load whitelist file: {}", e.getMessage());
        }
    }

    // Save the whitelist to the JSON file
    private static void saveWhitelist() {
        JsonObject json = new JsonObject();
        json.addProperty("_comment", "DO NOT EDIT THIS FILE MANUALLY - Managed by Items of Legends mod");
        for (Map.Entry<String, Set<String>> entry : WHITELISTS.entrySet()) {
            json.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
        }

        try {
            Files.createDirectories(Paths.get(WHITELIST_FILE).getParent());
            try (Writer writer = Files.newBufferedWriter(Paths.get(WHITELIST_FILE))) {
                GSON.toJson(json, writer);
            }
            LOGGER.info("Saved whitelist to {}", WHITELIST_FILE);
        } catch (IOException e) {
            LOGGER.error("Failed to save whitelist file: {}", e.getMessage());
        }
    }

    // Create a default whitelist file if it doesn't exist
    private static void createDefaultWhitelistFile() {
        JsonObject json = new JsonObject();
        json.addProperty("_comment", "DO NOT EDIT THIS FILE MANUALLY - Managed by Items of Legends mod");
        try {
            Files.createDirectories(Paths.get(WHITELIST_FILE).getParent());
            try (Writer writer = Files.newBufferedWriter(Paths.get(WHITELIST_FILE))) {
                GSON.toJson(json, writer);
            }
            LOGGER.info("Created default whitelist file at {}", WHITELIST_FILE);
        } catch (IOException e) {
            LOGGER.error("Failed to create default whitelist file: {}", e.getMessage());
        }
    }
}