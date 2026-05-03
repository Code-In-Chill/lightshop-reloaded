package com.lightshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TransactionGUI {

    public static final String GUI_TITLE = "§8Giao dịch: ";

    private final LightShop plugin;

    public TransactionGUI(LightShop plugin) {
        this.plugin = plugin;
    }

    public Inventory createGUI(ShopItem shopItem) {
        // Create 3-row inventory
        String title = GUI_TITLE + ShopGUI.colorize(shopItem.getDisplayName());
        // Max title length is 32 in some versions, but 1.20 allows longer.
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        // Fill background
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        // The item to buy (Middle)
        inventory.setItem(13, plugin.getShopGUI().createShopItemStack(shopItem));

        // Buy Options
        double price = shopItem.getPrice();
        String currency = shopItem.getCurrency();
        EconomyHandler eco = plugin.getEconomyHandler();

        // Buy 1
        List<String> lore1 = new ArrayList<>();
        lore1.add("§7Số lượng: §f1");
        lore1.add("§7Tổng giá: §e" + eco.formatCurrency(price, currency));
        lore1.add("");
        lore1.add("§aNhấn để mua!");
        inventory.setItem(10, createItem(Material.LIME_STAINED_GLASS_PANE, "§a§lMUA 1", lore1));

        // Buy 16
        List<String> lore16 = new ArrayList<>();
        lore16.add("§7Số lượng: §f16");
        lore16.add("§7Tổng giá: §e" + eco.formatCurrency(price * 16, currency));
        lore16.add("");
        lore16.add("§aNhấn để mua!");
        inventory.setItem(11, createItem(Material.LIME_STAINED_GLASS_PANE, "§a§lMUA 16", lore16));

        // Buy 64
        List<String> lore64 = new ArrayList<>();
        lore64.add("§7Số lượng: §f64");
        lore64.add("§7Tổng giá: §e" + eco.formatCurrency(price * 64, currency));
        lore64.add("");
        lore64.add("§aNhấn để mua!");
        inventory.setItem(12, createItem(Material.LIME_STAINED_GLASS_PANE, "§a§lMUA 64", lore64));

        // Back button
        List<String> backLore = new ArrayList<>();
        backLore.add("§7Quay lại cửa hàng");
        inventory.setItem(26, createItem(Material.BARRIER, "§c§lQUAY LẠI", backLore));

        return inventory;
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
