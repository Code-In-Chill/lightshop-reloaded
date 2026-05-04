package com.lightshop;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Level;

/**
 * Event listener for shop interactions
 * Handles GUI clicks and purchase logic with Folia support
 */
public class ShopEventListener implements Listener {
    private final LightShop plugin;
    private final EconomyHandler economyHandler;
    private final ShopGUI shopGUI;

    public ShopEventListener(LightShop plugin) {
        this.plugin = plugin;
        this.economyHandler = plugin.getEconomyHandler();
        this.shopGUI = plugin.getShopGUI();
    }

    /**
     * Handle inventory click events
     * Uses scheduler to ensure thread safety on Folia
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Handle Main Shop GUI
        if (title.equals(shopGUI.guiTitle)) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();

            if (slot >= event.getView().getTopInventory().getSize()) {
                return;
            }

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) {
                return;
            }

            ShopItem shopItem = shopGUI.getShopItemByIndex(slot);
            if (shopItem == null) {
                return;
            }

            // Open Transaction GUI
            schedulePlayerTask(player, () -> {
                player.openInventory(plugin.getTransactionGUI().createGUI(shopItem));
            });
            return;
        }

        // Handle Transaction GUI
        if (title.startsWith(TransactionGUI.GUI_TITLE)) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();

            if (slot >= event.getView().getTopInventory().getSize()) {
                return;
            }

            ItemStack displayItem = event.getView().getTopInventory().getItem(13);
            if (displayItem == null || !displayItem.hasItemMeta()) {
                return;
            }

            ShopItem targetItem = null;
            NamespacedKey key = new NamespacedKey(plugin, "shop_item_id");
            if (displayItem.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                String itemId = displayItem.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
                targetItem = shopGUI.getShopItemById(itemId);
            } else {
                // Fallback for older items or if PDC failed
                String displayItemName = displayItem.getItemMeta().getDisplayName();
                for (ShopItem item : shopGUI.getAllItems()) {
                    if (ShopGUI.colorize(item.getDisplayName()).equals(displayItemName)) {
                        targetItem = item;
                        break;
                    }
                }
            }

            if (targetItem == null) {
                return;
            }

            if (slot == 26) {
                schedulePlayerTask(player, () -> player.openInventory(shopGUI.createShopInventory()));
                return;
            }

            int amount = 0;
            if (slot == 10) amount = 1;
            else if (slot == 11) amount = 16;
            else if (slot == 12) amount = 64;

            if (amount > 0) {
                final ShopItem finalItem = targetItem;
                final int finalAmount = amount;
                schedulePlayerTask(player, () -> processPurchase(player, finalItem, finalAmount));
            }
        }
    }

    /**
     * Handle inventory close events
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Can add logic here if needed
    }

    /**
     * Process a purchase transaction
     */
    private void processPurchase(Player player, ShopItem shopItem, int multiplier) {
        try {
            // Check if player has enough balance
            double price = shopItem.getPrice() * multiplier;
            String currency = shopItem.getCurrency();

            if (!economyHandler.hasBalance(player, price, currency)) {
                double balance = economyHandler.getBalance(player, currency);
                String message = plugin.getConfig().getString("messages.no-money",
                        "&cYou don't have enough money!");
                message = message.replace("%price%", economyHandler.formatCurrency(price, currency))
                        .replace("%balance%", economyHandler.formatCurrency(balance, currency));
                
                player.sendMessage(ShopGUI.colorize(plugin.getConfig().getString("messages.prefix", "&e[LightShop]&r ") + message));
                return;
            }

            // Check if inventory has space
            int totalItemsToGive = shopItem.getAmount() * multiplier;
            ItemStack baseItem = shopGUI.createShopItemStack(shopItem);
            int maxStackSize = baseItem.getMaxStackSize();
            if (maxStackSize <= 0) maxStackSize = 64;
            int requiredSlots = (int) Math.ceil((double) totalItemsToGive / maxStackSize);

            int freeSlots = 0;
            for (ItemStack item : player.getInventory().getStorageContents()) {
                if (item == null || item.getType().isAir()) {
                    freeSlots++;
                }
            }

            if (freeSlots < requiredSlots) {
                String message = plugin.getConfig().getString("messages.inventory-full",
                        "&cYour inventory is full!");
                player.sendMessage(ShopGUI.colorize(plugin.getConfig().getString("messages.prefix", "&e[LightShop]&r ") + message));
                return;
            }

            // Deduct money from player
            if (!economyHandler.withdrawBalance(player, price, currency)) {
                String message = plugin.getConfig().getString("messages.buy-fail",
                        "&cFailed to purchase %item%!");
                message = message.replace("%item%", shopItem.getDisplayName());
                player.sendMessage(ShopGUI.colorize(plugin.getConfig().getString("messages.prefix", "&e[LightShop]&r ") + message));
                return;
            }

            // Give item to player
            baseItem.setAmount(1);
            int remaining = totalItemsToGive;
            while (remaining > 0) {
                int amount = Math.min(remaining, maxStackSize);
                ItemStack stack = baseItem.clone();
                stack.setAmount(amount);
                player.getInventory().addItem(stack);
                remaining -= amount;
            }

            // Send success message
            String message = plugin.getConfig().getString("messages.buy-success",
                    "&aYou have purchased %item% for %price%!");
            message = message.replace("%item%", shopItem.getDisplayName())
                    .replace("%price%", economyHandler.formatCurrency(price, currency));
            
            player.sendMessage(ShopGUI.colorize(plugin.getConfig().getString("messages.prefix", "&e[LightShop]&r ") + message));

            plugin.getLogger().info(player.getName() + " purchased " + shopItem.getId() + 
                    " for " + price + " " + currency);

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error processing purchase for " + player.getName(), e);
            player.sendMessage(ShopGUI.colorize(plugin.getConfig().getString("messages.prefix", "&e[LightShop]&r ") +
                    "&cAn error occurred while processing your purchase!"));
        }
    }

    /**
     * Schedule a task on player's thread for Folia compatibility
     * Automatically detects server type and uses appropriate scheduler
     */
    private void schedulePlayerTask(Player player, Runnable task) {
        try {
            if (plugin.isFoliaServer()) {
                // Use Folia's region scheduler
                scheduleFoliaTask(player, task);
            } else {
                // Use Bukkit scheduler for Paper/Spigot
                plugin.getServer().getScheduler().runTask(plugin, task);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error scheduling player task", e);
            // Fallback to sync task
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Schedule task for Folia using reflection
     * This ensures compatibility without direct Folia dependency
     */
    private void scheduleFoliaTask(Player player, Runnable task) {
        try {
            // Get the RegionizedServer instance
            Object regionizedServer = Bukkit.getServer().getClass()
                    .getMethod("getRegionizedServer")
                    .invoke(Bukkit.getServer());

            // Get the EntityScheduler for the player
            Object entityScheduler = regionizedServer.getClass()
                    .getMethod("getEntityScheduler", org.bukkit.entity.Entity.class)
                    .invoke(regionizedServer, player);

            // Execute the task
            entityScheduler.getClass()
                    .getMethod("execute", Runnable.class, Runnable.class, long.class)
                    .invoke(entityScheduler, plugin, task, 0L);
        } catch (Exception e) {
            // Fallback to sync scheduler
            plugin.getLogger().log(Level.FINE, "Folia scheduler not available, using sync scheduler", e);
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }
}
