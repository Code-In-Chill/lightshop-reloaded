# LightShop Quick Start Guide

## 5-Minute Setup

### 1. Build the Plugin
```bash
cd lightshop
mvn clean package
```

### 2. Install on Server
```bash
cp target/LightShop-1.0.0.jar /path/to/server/plugins/
```

### 3. Restart Server
```bash
# Stop and restart your server
./start.sh  # or your startup command
```

### 4. First Test
```
/lightshop    # Opens the shop for player
/shophelp     # Shows help information
```

---

## Essential Configuration Changes

### Using Vault Economy (Default)

**config.yml:**
```yaml
economy:
  use-vault: true
  custom-eco:
    enabled: false
```

✅ Works out of the box if Vault is installed

---

### Using Custom Economy (PlayerPoints Example)

**Prerequisites:**
- PlaceholderAPI installed
- PlayerPoints plugin installed

**config.yml:**
```yaml
economy:
  use-vault: false
  custom-eco:
    enabled: true
    balance-placeholder: "%playerpoints_points%"
    take-command: "points take %player% %amount%"
    give-command: "points give %player% %amount%"
```

**Verify setup:**
```
/papi parse @s %playerpoints_points%   # Should show your current points
```

---

### Adding Shop Items

Edit **shop.yml** to add items:

```yaml
# Simple item - $100 from Vault
diamond:
  material: DIAMOND
  name: "&bDiamond"
  lore:
    - "&7A precious gem"
  price: 100.0
  currency: "vault"
  amount: 1

# Custom economy item - 50 points
gold_ingot:
  material: GOLD_INGOT
  name: "&6Gold Ingot"
  lore:
    - "&7Worth 50 points"
  price: 50.0
  currency: "custom"
  amount: 1
```

**Reload without restart:**
```
/lightshop reload
```

---

## Common Issues & Solutions

### "Vault not found" warning
- Install Vault plugin
- Or use custom economy instead
- Plugin still works, just can't use Vault economy

### Shop doesn't open
```
✓ Check /plugins - LightShop should be listed
✓ Check player permission: lightshop.use
✓ Check server logs for errors
```

### Custom economy balance not showing
```
/papi parse @s %playerpoints_points%

Should show a number, not "unknown placeholder"
If unknown: your plugin or placeholder is wrong
```

### Money not deducting
```
✓ Verify command works manually: /points take PlayerName 100
✓ Check config.yml has correct command
✓ Restart server after config changes
✓ Check console logs for errors
```

---

## File Locations

After installation, these files will be created:

```
server/plugins/
├── LightShop.jar          (Main plugin)
├── LightShop/             (Data folder)
│   ├── config.yml         (Main config)
│   ├── shop.yml           (Item definitions)
│   └── (other files auto-created)
└── logs/                  (Server logs with plugin output)
```

**Edit config:**
```bash
nano plugins/LightShop/config.yml
```

**Reload without restart:**
```
/lightshop reload
```

---

## Development Quick Start

### Clone and Build
```bash
git clone <repo>
cd lightshop
mvn clean install
```

### Add Your First Item
1. Open `src/main/resources/shop.yml`
2. Add new item section:
   ```yaml
   my_item:
     material: EMERALD
     name: "&aMyItem"
     price: 50.0
     currency: "vault"
     amount: 1
   ```
3. Rebuild: `mvn clean package`

### Test on Server
```bash
# Copy to test server
cp target/LightShop-1.0.0.jar /path/to/test/server/plugins/

# Restart server and test
/lightshop
```

---

## Understanding the Flow

### What happens when you do `/lightshop`:

1. **Command Handler** → `ShopCommand.java`
2. **Scheduler** → Determines Folia or Paper
3. **GUI Creator** → `ShopGUI.createShopInventory()`
4. **Load Items** → From `shop.yml`
5. **Create ItemStacks** → With colors and lore
6. **Open Inventory** → Player sees GUI

### What happens when you click an item:

1. **Event** → `InventoryClickEvent`
2. **Scheduler** → Safe thread context
3. **Economy Check** → `EconomyHandler.hasBalance()`
4. **Deduct Money** → Via Vault or command
5. **Give Item** → Add to inventory
6. **Message** → Show success/error

---

## Monitoring & Debugging

### Enable Debug Mode

**config.yml:**
```yaml
debug: true
```

Restart server. Now you'll see detailed logs:
```
[LightShop] Debug: Player clicked diamond
[LightShop] Debug: Checking balance...
[LightShop] Debug: Balance sufficient, deducting 100.0
[LightShop] Debug: Item given to player
```

### Check Logs
```bash
tail -f logs/latest.log | grep LightShop
```

### Verify Setup
```
/plugins              # See LightShop listed
/lightshop           # Should open GUI
/papi debug <player> # Check placeholder values
```

---

## Next Steps

1. ✅ Install plugin
2. ✅ Configure economy (Vault or Custom)
3. ✅ Add items to shop.yml
4. ✅ Set prices and messages
5. ✅ Test with `/lightshop`
6. ✅ Configure permissions if needed
7. ✅ Monitor logs and adjust

---

## Need Help?

- **Plugin won't load**: Check Java version and Paper version in logs
- **Shop doesn't open**: Check permissions with `/perm` command
- **Money issues**: Verify economy plugin and commands manually
- **PlaceholderAPI issues**: Use `/papi parse` to test placeholders
- **Folia issues**: Enable debug mode to see scheduler decisions

See `README.md` for full documentation.
