package com.lightshop;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Data model representing a shop item
 */
public class ShopItem {
    private String id;
    private String material;
    private String displayName;
    private String[] lore;
    private double price;
    private String currency;
    private int amount;

    public ShopItem(String id, ConfigurationSection config) {
        this.id = id;
        this.material = config.getString("material", "DIAMOND");
        this.displayName = config.getString("name", "&6" + id);
        this.lore = config.getStringList("lore").toArray(new String[0]);
        this.price = config.getDouble("price", 0.0);
        this.currency = config.getString("currency", "vault");
        this.amount = config.getInt("amount", 1);
    }

    // Getters
    public String getId() { return id; }
    public String getMaterial() { return material; }
    public String getDisplayName() { return displayName; }
    public String[] getLore() { return lore; }
    public double getPrice() { return price; }
    public String getCurrency() { return currency; }
    public int getAmount() { return amount; }
}
