package com.lightshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

/**
 * Handles shop GUI creation and management
 * Thread-safe for Folia usage
 */
public class ShopGUI {
    private final LightShop plugin;
    private final Map<String, ShopItem> shopItems;
    public final String guiTitle;
    private final int guiRows;
    private final boolean fillEmpty;
    private final String glassType;

    public ShopGUI(LightShop plugin) {
        this.plugin = plugin;
        this.shopItems = new HashMap<>();
        
        ConfigurationSection shopConfig = plugin.getShopConfig();
        this.guiTitle = colorize(plugin.getConfig().getString("shop.title", "&e&lLightShop"));
        this.guiRows = plugin.getConfig().getInt("shop.rows", 3);
        this.fillEmpty = plugin.getConfig().getBoolean("shop.fill-empty", false);
        this.glassType = plugin.getConfig().getString("shop.glass-material", "GRAY_STAINED_GLASS_PANE");
        
        loadShopItems(shopConfig);
    }

    /**
     * Load all shop items from configuration directory
     * Supports loading from shops/ directory with multiple .yml files
     */
    private void loadShopItems(ConfigurationSection shopConfig) {
        // First, load legacy shop.yml if it exists
        if (shopConfig != null) {
            loadItemsFromConfig(shopConfig);
        }
        
        // Then load all .yml files from shops/ directory
        loadItemsFromDirectory();
        
        plugin.getLogger().info("Loaded " + shopItems.size() + " shop items");
    }
    
    /**
     * Load items from a configuration section
     */
    private void loadItemsFromConfig(ConfigurationSection config) {
        for (String key : config.getKeys(false)) {
            try {
                ConfigurationSection itemConfig = config.getConfigurationSection(key);
                if (itemConfig != null) {
                    ShopItem shopItem = new ShopItem(key, itemConfig);
                    shopItems.put(key, shopItem);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                        "Failed to load shop item: " + key, e);
            }
        }
    }
    
    /**
     * Load all items from shops/ directory
     */
    private void loadItemsFromDirectory() {
        try {
            java.io.File shopsDir = new java.io.File(plugin.getDataFolder(), "shops");
            
            // Create shops directory if it doesn't exist
            if (!shopsDir.exists()) {
                shopsDir.mkdirs();
                plugin.getLogger().info("Created shops directory");
            }
            
            // Load all .yml files from shops directory
            java.io.File[] files = shopsDir.listFiles((dir, name) -> name.endsWith(".yml"));
            
            if (files == null || files.length == 0) {
                plugin.getLogger().warning("No shop files found in shops/ directory");
                return;
            }
            
            for (java.io.File file : files) {
                try {
                    org.bukkit.configuration.file.YamlConfiguration shopYaml = 
                            org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
                    
                    loadItemsFromConfig(shopYaml);
                    plugin.getLogger().info("Loaded shop category: " + file.getName());
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, 
                            "Failed to load shop file: " + file.getName(), e);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error loading shops directory", e);
        }
    }

    /**
     * Create the shop inventory for a player
     * Safe to call from any thread in Folia
     */
    public Inventory createShopInventory() {
        Inventory inventory = Bukkit.createInventory(
                null, 
                guiRows * 9,
                guiTitle
        );

        // Add shop items
        int slot = 0;
        for (ShopItem item : shopItems.values()) {
            if (slot < guiRows * 9) {
                ItemStack itemStack = createShopItemStack(item);
                inventory.setItem(slot, itemStack);
                slot++;
            }
        }

        // Fill empty slots with glass panes
        if (fillEmpty) {
            Material glassMaterial = Material.getMaterial(glassType);
            if (glassMaterial == null) {
                glassMaterial = Material.GRAY_STAINED_GLASS_PANE;
            }
            
            ItemStack glassPane = new ItemStack(glassMaterial);
            ItemMeta glassMeta = glassPane.getItemMeta();
            if (glassMeta != null) {
                glassMeta.setDisplayName(" ");
                glassPane.setItemMeta(glassMeta);
            }
            
            for (int i = 0; i < guiRows * 9; i++) {
                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                    inventory.setItem(i, glassPane);
                }
            }
        }

        return inventory;
    }

    /**
     * Create an ItemStack for a shop item
     */
    public ItemStack createShopItemStack(ShopItem shopItem) {
        Material material = Material.getMaterial(shopItem.getMaterial());
        if (material == null) {
            material = Material.DIAMOND;
        }

        ItemStack itemStack = new ItemStack(material, shopItem.getAmount());
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(colorize(shopItem.getDisplayName()));
            
            List<String> lore = new ArrayList<>();
            for (String line : shopItem.getLore()) {
                lore.add(colorize(line));
            }
            meta.setLore(lore);
            
            // Store item ID in PersistentDataContainer for fast O(1) lookup
            NamespacedKey key = new NamespacedKey(plugin, "shop_item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, shopItem.getId());
            
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    /**
     * Get shop item by index
     */
    public ShopItem getShopItemByIndex(int index) {
        int i = 0;
        for (ShopItem item : shopItems.values()) {
            if (i == index) {
                return item;
            }
            i++;
        }
        return null;
    }

    /**
     * Get shop item by ID
     */
    public ShopItem getShopItemById(String id) {
        return shopItems.get(id);
    }

    /**
     * Colorize strings with & color codes
     */
    public static String colorize(String text) {
        if (text == null) return "";
        return text.replaceAll("&([0-9a-fA-FkK-oO-rR])", "§$1");
    }

    /**
     * Get all shop items
     */
    public Collection<ShopItem> getAllItems() {
        return shopItems.values();
    }

    /**
     * Reload shop items from configuration
     */
    public void reloadItems() {
        shopItems.clear();
        loadShopItems(plugin.getShopConfig());
    }
}
