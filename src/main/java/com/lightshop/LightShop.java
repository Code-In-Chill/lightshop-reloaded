package com.lightshop;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;

import java.io.File;
import java.util.logging.Level;

/**
 * Main LightShop Plugin Class
 * Handles initialization, configuration loading, and service registration
 */
public class LightShop extends JavaPlugin {

    private static LightShop instance;
    private EconomyHandler economyHandler;
    private ShopGUI shopGUI;
    private TransactionGUI transactionGUI;
    private Economy vaultEconomy;
    private YamlConfiguration shopConfig;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config if it doesn't exist
        saveDefaultConfig();

        try {
            // Load configurations
            loadConfigurations();

            // Setup Vault economy
            if (getConfig().getBoolean("economy.use-vault")) {
                if (!setupVaultEconomy()) {
                    getLogger().log(Level.WARNING, "Vault not found, Vault economy disabled!");
                }
            }

            // Initialize economy handler
            economyHandler = new EconomyHandler(this);
            getLogger().info("Economy system initialized");

            // Initialize shop GUI
            shopGUI = new ShopGUI(this);
            transactionGUI = new TransactionGUI(this);
            getLogger().info("Shop GUI system initialized");

            // Register event listener
            getServer().getPluginManager().registerEvents(
                    new ShopEventListener(this), this
            );

            // Register commands
            getCommand("shop").setExecutor(new ShopCommand(this));
            getCommand("shophelp").setExecutor(new ShopHelpCommand(this));

            ConsoleCommandSender console = Bukkit.getConsoleSender();
            console.sendMessage("§e  _     _       _     _   _____ _                 ");
            console.sendMessage("§e | |   (_)     | |   | | /  ___| |                ");
            console.sendMessage("§e | |    _  __ _| |__ | |_\\ `--.| |__   ___  _ __  ");
            console.sendMessage("§e | |   | |/ _` | '_ \\| __|`--. \\ '_ \\ / _ \\| '_ \\ ");
            console.sendMessage("§e | |___| | (_| | | | | |_/\\__/ / | | | (_) | |_) |");
            console.sendMessage("§e \\_____/_|\\__, |_| |_|\\__\\____/|_| |_|\\___/| .__/ ");
            console.sendMessage("§e           __/ |                           | |    ");
            console.sendMessage("§e          |___/                            |_|    ");
            console.sendMessage("");
            console.sendMessage("§e §6Reloaded Version");
            console.sendMessage("");
            console.sendMessage("§8==================================================");
            console.sendMessage("§8| §fPlugin: §eLightShop v" + getDescription().getVersion());
            console.sendMessage("§8| §fAuthor: §bHarariMc");
            console.sendMessage("§8| §fPlatform: §d" + (isFoliaServer() ? "Folia" : "Paper/Spigot"));
            console.sendMessage("§8| §fStatus: §a✔ SUCCESSFULLY LOADED");
            console.sendMessage("§8==================================================");

            // Print community version log
            printCommunityVersionLog(console);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable LightShop", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("LightShop plugin disabled");
    }

    /**
     * Load all configuration files
     */
    private void loadConfigurations() throws Exception {
        // Load main config
        reloadConfig();

        // Load shop config (legacy support - only if file exists)
        File shopConfigFile = new File(getDataFolder(), "shop.yml");
        if (shopConfigFile.exists()) {
            shopConfig = YamlConfiguration.loadConfiguration(shopConfigFile);
        } else {
            shopConfig = null;
        }

        // Create shops directory if doesn't exist
        createShopsDirectory();

        getLogger().info("Configurations loaded successfully");
    }

    /**
     * Create default shops directory with example files
     */
    private void createShopsDirectory() {
        File shopsDir = new File(getDataFolder(), "shops");
        if (!shopsDir.exists()) {
            shopsDir.mkdirs();
            getLogger().info("Created shops directory");

            // Save default shop files
            try {
                saveDefaultShopFile("shops/materials.yml");
                saveDefaultShopFile("shops/weapons.yml");
                saveDefaultShopFile("shops/armor.yml");
                saveDefaultShopFile("shops/blocks.yml");
                saveDefaultShopFile("shops/food.yml");
                getLogger().info("Extracted default shop files to shops/ directory");
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to extract default shop files", e);
            }
        }
    }

    /**
     * Save a shop file from resources
     */
    private void saveDefaultShopFile(String resourcePath) {
        File outFile = new File(getDataFolder(), resourcePath);
        if (!outFile.exists()) {
            saveResource(resourcePath, false);
        }
    }

    /**
     * Setup Vault economy integration
     */
    private boolean setupVaultEconomy() {
        try {
            RegisteredServiceProvider<Economy> rsp =
                    getServer().getServicesManager().getRegistration(Economy.class);

            if (rsp == null) {
                return false;
            }

            vaultEconomy = rsp.getProvider();
            return vaultEconomy != null;
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Error setting up Vault economy", e);
            return false;
        }
    }

    /**
     * Check if server is running Folia
     */
    public boolean isFoliaServer() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Get shop configuration
     */
    public YamlConfiguration getShopConfig() {
        return shopConfig;
    }

    /**
     * Get Vault economy instance
     */
    public Economy getVaultEconomy() {
        return vaultEconomy;
    }

    /**
     * Get economy handler
     */
    public EconomyHandler getEconomyHandler() {
        return economyHandler;
    }

    /**
     * Get shop GUI handler
     */
    public ShopGUI getShopGUI() {
        return shopGUI;
    }

    /**
     * Get transaction GUI handler
     */
    public TransactionGUI getTransactionGUI() {
        return transactionGUI;
    }

    /**
     * Get plugin instance
     */
    public static LightShop getInstance() {
        return instance;
    }

    /**
     * Reload all configurations
     */
    public void reloadAllConfigs() throws Exception {
        loadConfigurations();
        if (getConfig().getBoolean("economy.use-vault")) {
            setupVaultEconomy();
        }
    }

    /**
     * Print community version information
     */
    private void printCommunityVersionLog(org.bukkit.command.ConsoleCommandSender console) {
        String[] lines = {
            "Community version made by leminhbao308",
            "https://github.com/Code-In-Chill/lightshop-reloaded/releases"
        };
        int maxLen = 0;
        for (String l : lines) {
            maxLen = Math.max(maxLen, l.length());
        }
        String border = "§8+" + "=".repeat(maxLen + 4) + "+";
        console.sendMessage(border);
        for (String l : lines) {
            int pad = maxLen - l.length();
            console.sendMessage("§8| §7" + l + " ".repeat(pad) + "§8 |");
        }
        console.sendMessage(border);
    }
}
