# LightShop Plugin

A high-performance Minecraft shop plugin with full support for **Folia** (multi-threaded servers) and dual economy systems (Vault + Custom Economy).

## Features

✅ **Folia Support**: Thread-safe implementation using Region Scheduler for Folia servers
✅ **Dual Economy System**: 
  - Vault Economy (default)
  - Custom Economy with PlaceholderAPI
✅ **GUI Shop**: Intuitive inventory-based shop interface
✅ **Configurable**: Fully customizable items, prices, and messages
✅ **Console Command Execution**: Secure money deduction using console sender for permissions

## Requirements

- **Java 17+**
- **Paper/Spigot 1.20+** or **Folia**
- **Vault** (for Vault economy)
- **PlaceholderAPI** (for custom economy balance checking - optional but recommended)

## Installation

1. **Build the plugin**:
   ```bash
   mvn clean package
   ```
   The compiled JAR will be in `target/LightShop-1.0.0.jar`

2. **Place JAR in server's plugins folder**:
   ```bash
   cp target/LightShop-1.0.0.jar /path/to/server/plugins/
   ```

3. **Restart your server**

4. **Configure** the plugin using the generated `config.yml` and `shop.yml`

## Configuration

### config.yml

```yaml
economy:
  use-vault: true
  custom-eco:
    enabled: true
    balance-placeholder: "%playerpoints_points%"
    take-command: "points take %player% %amount%"
    give-command: "points give %player% %amount%"

shop:
  title: "&e&lLightShop"
  rows: 3
  fill-empty: false
  glass-material: "GRAY_STAINED_GLASS_PANE"

messages:
  prefix: "&e[LightShop]&r "
  no-money: "&cYou don't have enough money! Need: %price%, You have: %balance%"
  buy-success: "&aYou have purchased %item% for %price%!"
  buy-fail: "&cFailed to purchase %item%, please try again!"
  inventory-full: "&cYour inventory is full!"
```

### shop.yml

Add items to sell:

```yaml
diamond:
  material: DIAMOND
  name: "&bDiamond"
  lore:
    - "&7A precious gem"
    - "&eCost: $100"
  price: 100.0
  currency: "vault"
  amount: 1

gold_ingot:
  material: GOLD_INGOT
  name: "&6Gold Ingot"
  lore:
    - "&7Valuable trading item"
    - "&eCost: 50 Points"
  price: 50.0
  currency: "custom"
  amount: 1
```

## Commands

### Player Commands
- `/lightshop` or `/shop` or `/ls` - Open the shop
- `/shophelp` or `/shopinfo` - Display help information

### Admin Commands
- `/lightshop reload` - Reload all configurations (requires `lightshop.admin` permission)

## Permissions

| Permission | Description | Default |
|-----------|-------------|---------|
| `lightshop.use` | Use the shop | Everyone |
| `lightshop.admin` | Admin commands | OP |
| `lightshop.admin.economy` | Economy admin | OP |

## Folia Compatibility

### How it Works

The plugin detects the server type at startup:
- **On Folia**: Uses Entity Scheduler via reflection for thread-safe player operations
- **On Paper/Spigot**: Uses standard Bukkit Scheduler

This ensures:
- ✅ Safe GUI operations on player threads
- ✅ Proper money deductions without threading errors
- ✅ Item delivery without conflicts
- ✅ No Folia-specific dependency needed

### Technical Implementation

The event listener uses reflection to detect and use Folia's `RegionizedServer`:

```java
Object regionizedServer = Bukkit.getServer().getClass()
    .getMethod("getRegionizedServer")
    .invoke(Bukkit.getServer());

Object entityScheduler = regionizedServer.getClass()
    .getMethod("getEntityScheduler", Entity.class)
    .invoke(regionizedServer, player);

entityScheduler.getClass()
    .getMethod("execute", Runnable.class, Runnable.class, long.class)
    .invoke(entityScheduler, plugin, task, 0L);
```

## Economy System

### Vault Economy

- Standard economy from Vault
- Money deducted via `Economy.withdrawPlayer(player, amount)`
- Works on both Folia and Paper

### Custom Economy

- Uses PlaceholderAPI for balance checking
- Executes commands via console sender for permissions
- Example: `PlayerPoints` plugin
  ```yaml
  balance-placeholder: "%playerpoints_points%"
  take-command: "points take %player% %amount%"
  give-command: "points give %player% %amount%"
  ```

## Troubleshooting

### Plugin doesn't enable?
- Check if Java 17+ is installed: `java -version`
- Ensure Paper/Spigot 1.20+ or Folia is installed
- Check server logs for errors

### Shop GUI not opening?
- Verify Vault is installed: `/plugins` command
- Check player has `lightshop.use` permission
- Review server logs

### Money not deducting (Custom Economy)?
- Verify PlaceholderAPI is installed: `/papi info`
- Check placeholder is correct: `/papi parse me %balance%`
- Verify commands work manually: `/points take PlayerName 100`
- Ensure commands use console sender (not player sender)

### "Folia not found" warning?
- Normal if running on Paper/Spigot
- Plugin still works correctly
- Safe fallback to standard scheduler

## Development

### Project Structure
```
lightshop/
├── pom.xml
├── src/main/
│   ├── java/com/lightshop/
│   │   ├── LightShop.java              (Main plugin class)
│   │   ├── EconomyHandler.java         (Economy logic)
│   │   ├── ShopGUI.java                (GUI creation)
│   │   ├── ShopItem.java               (Data model)
│   │   ├── ShopCommand.java            (Commands)
│   │   ├── ShopHelpCommand.java        (Help command)
│   │   └── ShopEventListener.java      (Event handling)
│   └── resources/
│       ├── plugin.yml
│       ├── config.yml
│       └── shop.yml
```

### Building from Source

```bash
# Clone/download the project
cd lightshop

# Build with Maven
mvn clean package

# Output: target/LightShop-1.0.0.jar
```

## License

This plugin is provided as-is for educational and personal use.

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review server logs
3. Verify configuration files
4. Check Vault and PlaceholderAPI are installed (if needed)
