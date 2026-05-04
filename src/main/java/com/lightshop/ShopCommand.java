package com.lightshop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Main shop command handler
 */
public class ShopCommand implements CommandExecutor {
    private final LightShop plugin;
    private final ShopGUI shopGUI;

    public ShopCommand(LightShop plugin) {
        this.plugin = plugin;
        this.shopGUI = plugin.getShopGUI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can open the shop!");
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("lightshop.use")) {
            String message = plugin.getConfig().getString("messages.no-permission",
                    "&cYou don't have permission to use this!");
            player.sendMessage(ShopGUI.colorize(plugin.getConfig().getString("messages.prefix", "&e[LightShop]&r ") +
                    message));
            return true;
        }

        // Handle subcommands
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload") && player.hasPermission("lightshop.admin")) {
                reloadConfigs(player);
                return true;
            }
        }

        // Open the shop
        openShop(player);
        return true;
    }

    /**
     * Open the shop for a player
     */
    private void openShop(Player player) {
        try {
            // Schedule on player's thread for Folia safety
            if (plugin.isFoliaServer()) {
                scheduleOpenShop(player);
            } else {
                player.openInventory(shopGUI.createShopInventory());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error opening shop for " + player.getName());
            player.sendMessage(ShopGUI.colorize(plugin.getConfig().getString("messages.prefix", "&e[LightShop]&r ") +
                    "&cError opening shop!"));
        }
    }

    /**
     * Schedule shop opening for Folia
     */
    private void scheduleOpenShop(Player player) {
        try {
            Object regionizedServer = player.getServer().getClass()
                    .getMethod("getRegionizedServer")
                    .invoke(player.getServer());

            Object entityScheduler = regionizedServer.getClass()
                    .getMethod("getEntityScheduler", org.bukkit.entity.Entity.class)
                    .invoke(regionizedServer, player);

            entityScheduler.getClass()
                    .getMethod("execute", Runnable.class, Runnable.class, long.class)
                    .invoke(entityScheduler, plugin, (Runnable) () -> 
                            player.openInventory(shopGUI.createShopInventory()), 0L);
        } catch (Exception e) {
            // Fallback to sync
            player.getServer().getScheduler().runTask(plugin, () ->
                    player.openInventory(shopGUI.createShopInventory()));
        }
    }

    /**
     * Reload configurations
     */
    private void reloadConfigs(Player player) {
        try {
            plugin.reloadAllConfigs();
            shopGUI.reloadItems();
            
            String message = plugin.getConfig().getString("messages.config-reloaded",
                    "&aConfigs reloaded successfully!");
            player.sendMessage(ShopGUI.colorize(plugin.getConfig().getString("messages.prefix", "&e[LightShop]&r ") +
                    message));
            
            plugin.getLogger().info(player.getName() + " reloaded LightShop configs");
        } catch (Exception e) {
            String message = plugin.getConfig().getString("messages.config-reload-fail",
                    "&cFailed to reload configs: %error%");
            message = message.replace("%error%", e.getMessage());
            player.sendMessage(ShopGUI.colorize(plugin.getConfig().getString("messages.prefix", "&e[LightShop]&r ") +
                    message));
            plugin.getLogger().warning("Failed to reload configs: " + e.getMessage());
        }
    }
}
