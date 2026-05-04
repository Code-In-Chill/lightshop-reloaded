package com.lightshop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.Configuration;

/**
 * Help command handler
 */
public class ShopHelpCommand implements CommandExecutor {
    private final LightShop plugin;

    public ShopHelpCommand(LightShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Get the help message from config
        String helpMessage = plugin.getConfig().getString("messages.shop-help",
                "&e=== LightShop Help ===\n" +
                "&7/lightshop - Open the shop\n" +
                "&7/shophelp - Show this help");

        for (String line : helpMessage.split("\\n")) {
            player.sendMessage(ShopGUI.colorize(line));
        }

        return true;
    }
}
