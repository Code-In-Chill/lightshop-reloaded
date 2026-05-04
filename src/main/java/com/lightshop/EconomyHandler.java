package com.lightshop;

import net.milkbowl.vault.economy.Economy;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Handles all economy operations for both Vault and Custom Economy
 */
public class EconomyHandler {
    private final LightShop plugin;
    private final Economy vaultEconomy;
    private final boolean useVault;
    private final boolean useCustomEco;
    private final String balancePlaceholder;
    private final String takeCommand;
    private final String giveCommand;
    private final boolean hasPlaceholderAPI;

    public EconomyHandler(LightShop plugin) {
        this.plugin = plugin;
        this.useVault = plugin.getConfig().getBoolean("economy.use-vault", true);
        this.vaultEconomy = plugin.getVaultEconomy();
        
        this.useCustomEco = plugin.getConfig().getBoolean("economy.custom-eco.enabled", true);
        this.balancePlaceholder = plugin.getConfig()
                .getString("economy.custom-eco.balance-placeholder", "%playerpoints_points%");
        this.takeCommand = plugin.getConfig()
                .getString("economy.custom-eco.take-command", "points take %player% %amount%");
        this.giveCommand = plugin.getConfig()
                .getString("economy.custom-eco.give-command", "points give %player% %amount%");
        
        this.hasPlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /**
     * Check if player has enough balance for a purchase
     */
    public boolean hasBalance(Player player, double amount, String currencyType) {
        if (currencyType.equalsIgnoreCase("vault")) {
            return hasVaultBalance(player, amount);
        } else if (currencyType.equalsIgnoreCase("custom")) {
            return hasCustomBalance(player, amount);
        }
        return false;
    }

    /**
     * Check Vault economy balance
     */
    private boolean hasVaultBalance(Player player, double amount) {
        if (vaultEconomy == null) {
            plugin.getLogger().log(Level.WARNING, "Vault economy not available");
            return false;
        }
        return vaultEconomy.has(player, amount);
    }

    /**
     * Check custom economy balance using PlaceholderAPI
     */
    private boolean hasCustomBalance(Player player, double amount) {
        if (!hasPlaceholderAPI) {
            plugin.getLogger().log(Level.WARNING, "PlaceholderAPI not found for custom economy");
            return false;
        }

        try {
            String balanceString = PlaceholderAPI.setPlaceholders(player, balancePlaceholder);
            double balance = Double.parseDouble(balanceString);
            return balance >= amount;
        } catch (NumberFormatException e) {
            plugin.getLogger().log(Level.WARNING, 
                    "Failed to parse balance placeholder: " + balanceString, e);
            return false;
        }
    }

    /**
     * Get player's current balance
     */
    public double getBalance(Player player, String currencyType) {
        if (currencyType.equalsIgnoreCase("vault")) {
            return getVaultBalance(player);
        } else if (currencyType.equalsIgnoreCase("custom")) {
            return getCustomBalance(player);
        }
        return 0.0;
    }

    /**
     * Get Vault economy balance
     */
    private double getVaultBalance(Player player) {
        if (vaultEconomy == null) {
            return 0.0;
        }
        return vaultEconomy.getBalance(player);
    }

    /**
     * Get custom economy balance using PlaceholderAPI
     */
    private double getCustomBalance(Player player) {
        if (!hasPlaceholderAPI) {
            return 0.0;
        }

        try {
            String balanceString = PlaceholderAPI.setPlaceholders(player, balancePlaceholder);
            return Double.parseDouble(balanceString);
        } catch (NumberFormatException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to parse balance: " + balanceString, e);
            return 0.0;
        }
    }

    /**
     * Deduct money from player
     */
    public boolean withdrawBalance(Player player, double amount, String currencyType) {
        if (currencyType.equalsIgnoreCase("vault")) {
            return withdrawVaultBalance(player, amount);
        } else if (currencyType.equalsIgnoreCase("custom")) {
            return withdrawCustomBalance(player, amount);
        }
        return false;
    }

    /**
     * Withdraw from Vault economy
     */
    private boolean withdrawVaultBalance(Player player, double amount) {
        if (vaultEconomy == null) {
            return false;
        }
        
        try {
            if (vaultEconomy.has(player, amount)) {
                vaultEconomy.withdrawPlayer(player, amount);
                return true;
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error withdrawing Vault balance", e);
            return false;
        }
    }

    /**
     * Withdraw from custom economy using command
     * Uses console sender to ensure proper permissions
     */
    private boolean withdrawCustomBalance(Player player, double amount) {
        try {
            // Replace placeholders in take-command
            String command = takeCommand
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf((int) amount));
            
            // Execute command as console to ensure proper permissions
            boolean result = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            
            if (!result) {
                plugin.getLogger().log(Level.WARNING, 
                        "Failed to execute take-command: " + command);
            }
            
            return result;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error withdrawing custom economy balance", e);
            return false;
        }
    }

    /**
     * Deposit money to player
     */
    public boolean depositBalance(Player player, double amount, String currencyType) {
        if (currencyType.equalsIgnoreCase("vault")) {
            return depositVaultBalance(player, amount);
        } else if (currencyType.equalsIgnoreCase("custom")) {
            return depositCustomBalance(player, amount);
        }
        return false;
    }

    /**
     * Deposit to Vault economy
     */
    private boolean depositVaultBalance(Player player, double amount) {
        if (vaultEconomy == null) {
            return false;
        }
        
        try {
            vaultEconomy.depositPlayer(player, amount);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error depositing Vault balance", e);
            return false;
        }
    }

    /**
     * Deposit to custom economy using command
     * Uses console sender to ensure proper permissions
     */
    private boolean depositCustomBalance(Player player, double amount) {
        try {
            // Replace placeholders in give-command
            String command = giveCommand
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf((int) amount));
            
            // Execute command as console to ensure proper permissions
            boolean result = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            
            if (!result) {
                plugin.getLogger().log(Level.WARNING, 
                        "Failed to execute give-command: " + command);
            }
            
            return result;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error depositing custom economy balance", e);
            return false;
        }
    }

    /**
     * Get formatted currency display string
     */
    public String formatCurrency(double amount, String currencyType) {
        if (currencyType.equalsIgnoreCase("vault")) {
            if (vaultEconomy != null) {
                return vaultEconomy.format(amount);
            }
            return "$" + amount;
        } else if (currencyType.equalsIgnoreCase("custom")) {
            return amount + " Points";
        }
        return amount + "";
    }
}
