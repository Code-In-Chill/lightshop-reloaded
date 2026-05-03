# LightShop Architecture Documentation

## Overview

LightShop is a production-ready Minecraft plugin designed with thread safety in mind for both traditional Paper/Spigot servers and next-generation Folia multi-threaded servers.

## Core Architecture

### 1. **Main Plugin Class** (`LightShop.java`)

**Responsibilities:**
- Plugin initialization and lifecycle management
- Configuration loading (config.yml, shop.yml)
- Service provider setup (Vault Economy, PlaceholderAPI)
- Server type detection (Folia vs Paper/Spigot)
- Command and event listener registration

**Key Methods:**
- `onEnable()`: Initializes all systems
- `setupVaultEconomy()`: Connects to Vault economy if available
- `isFoliaServer()`: Detects server type via reflection
- `loadConfigurations()`: Loads YAML configuration files
- `reloadAllConfigs()`: Dynamic configuration reloading

**Server Type Detection:**
```java
try {
    Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
    return true; // Folia
} catch (ClassNotFoundException e) {
    return false; // Paper/Spigot
}
```

---

### 2. **Economy System** (`EconomyHandler.java`)

**Responsibilities:**
- Balance checking (Vault and Custom Economy)
- Money withdrawal and deposit
- Currency formatting
- PlaceholderAPI integration for custom economy

**Dual Economy Design:**

#### Vault Economy
- Direct API integration with `Economy` interface
- Methods: `has()`, `withdrawPlayer()`, `depositPlayer()`
- Thread-safe (Vault handles synchronization)

#### Custom Economy
- Uses PlaceholderAPI for balance checking
- Executes commands via console sender for permissions
- Formula:
  ```
  To check balance: PlaceholderAPI.setPlaceholders(player, "%placeholder%")
  To deduct money: Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "command")
  ```

**Why Console Sender?**
- Ensures proper permission levels for economy commands
- Prevents players from bypassing money deduction
- Works reliably across different economy plugins

**Key Methods:**
- `hasBalance(player, amount, type)`: Check if player can afford
- `withdrawBalance(player, amount, type)`: Deduct money
- `depositBalance(player, amount, type)`: Add money
- `getBalance(player, type)`: Get current balance
- `formatCurrency(amount, type)`: Format for display

---

### 3. **Shop GUI** (`ShopGUI.java`)

**Responsibilities:**
- Load shop items from configuration
- Create inventory-based GUI
- Item stack creation with customization
- Color code processing (& to §)

**Features:**
- Configurable GUI size (rows 1-6)
- Optional glass pane filler
- Item display with lore and custom names
- Item amount stacking

**Inventory Structure:**
```
╔═════════════════════╗
║  Diamond  Emerald   ║  <- Shop Items
║  Gold     Iron      ║
║ (Glass Panes fill remaining slots)
╚═════════════════════╝
```

**Key Methods:**
- `createShopInventory()`: Create the GUI inventory
- `loadShopItems(config)`: Load items from YAML
- `createShopItemStack(item)`: Convert ShopItem to ItemStack
- `colorize(text)`: Process & color codes
- `reloadItems()`: Dynamic item reloading

---

### 4. **Shop Item Model** (`ShopItem.java`)

**Data Class** containing:
- `id`: Unique item identifier
- `material`: Minecraft material type
- `displayName`: GUI display name (with colors)
- `lore`: Item description/lore lines
- `price`: Purchase price
- `currency`: "vault" or "custom"
- `amount`: Stack size

**Configuration Example:**
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
```

---

### 5. **Event Listener** (`ShopEventListener.java`)

**Responsibilities:**
- Handle inventory click events
- Validate shop GUI clicks
- Process purchase transactions
- Thread-safe scheduling for Folia

**Folia Thread Safety Implementation:**

Uses reflection to detect and schedule tasks appropriately:

```java
// Folia approach
Object regionizedServer = Bukkit.getServer()...
Object entityScheduler = regionizedServer.getEntityScheduler(player)
entityScheduler.execute(plugin, task, 0L)

// Paper approach (fallback)
Bukkit.getScheduler().runTask(plugin, task)
```

**Purchase Flow:**
```
1. Player clicks item in GUI
2. Event listener detects click
3. Schedule task on player's thread (Folia-safe)
4. Check balance → Deduct money → Give item → Send message
5. Log transaction
```

**Key Methods:**
- `onInventoryClick()`: Event handler
- `processPurchase()`: Perform transaction
- `schedulePlayerTask()`: Thread-safe scheduling
- `scheduleFoliaTask()`: Folia-specific scheduling

---

### 6. **Commands** 

#### ShopCommand.java
**Commands:**
- `/lightshop`, `/shop`, `/ls`: Open the shop
- `/lightshop reload` (admin): Reload configs

**Folia Compatibility:**
- Detects server type
- Uses appropriate scheduler for GUI opening
- Reflection-based for forward compatibility

#### ShopHelpCommand.java
**Commands:**
- `/shophelp`, `/shopinfo`: Show help text

---

## Data Flow

### Purchase Transaction Flow
```
┌─────────────────────────────────────────────────────────┐
│ Player clicks item in GUI                               │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ InventoryClickEvent  │
        │ Detected             │
        └──────────┬───────────┘
                   │
    ┌──────────────▼──────────────┐
    │ Schedule on player's thread │
    │ (Folia or Bukkit scheduler) │
    └──────────────┬──────────────┘
                   │
         ┌─────────▼────────────┐
         │ Check Balance        │
         │ via EconomyHandler   │
         └──┬─────────────┬──┬──┘
            │             │  └─── Not enough → Error
            │             └─ Inventory full → Error
            ▼ Sufficient
    ┌───────────────────────┐
    │ Withdraw Money        │
    │ (Vault or Custom)     │
    └──┬────────────┬───────┘
       │            └─ Failed → Error
       ▼ Success
    ┌────────────────────┐
    │ Give Item          │
    │ to Player          │
    └────────┬───────────┘
             │
    ┌────────▼──────────┐
    │ Send Success      │
    │ Message & Log     │
    └───────────────────┘
```

---

## Configuration System

### plugin.yml
- Metadata and dependencies
- Command registration with aliases
- Permission definitions
- API version specification

### config.yml
- Economy settings (Vault/Custom)
- GUI appearance configuration
- Message customization
- Debug logging

### shop.yml
- Shop items definition
- Item properties (material, price, currency)
- Display customization

---

## Thread Safety Model

### Folia (Multi-threaded)
- Uses `RegionizedServer.getEntityScheduler()`
- All player operations scheduled on player's region thread
- Prevents data races and concurrent modifications

### Paper/Spigot (Single-threaded)
- Uses `Bukkit.getScheduler().runTask()`
- Falls back gracefully
- Same thread for all operations (main thread)

**Automatic Detection:**
```java
if (plugin.isFoliaServer()) {
    // Use Folia scheduler
} else {
    // Use Bukkit scheduler
}
```

---

## Dependency Management

### Required Dependencies
- **Bukkit API** (Paper/Spigot 1.20+)
- **Vault** (for economy)

### Soft Dependencies
- **PlaceholderAPI** (for custom economy balance checking)
- **PlayerPoints** or similar (for custom economy, if used)

### Integration Points
1. **Vault**: Economy API for money operations
2. **PlaceholderAPI**: Balance placeholder parsing
3. **Bukkit Events**: InventoryClickEvent handling
4. **Bukkit Commands**: Command registration and execution

---

## Error Handling

### Graceful Degradation
```
Config Load Error
  ├─ Log warning
  ├─ Use defaults
  └─ Plugin continues

Vault Not Found
  ├─ Log warning
  ├─ Disable Vault economy
  └─ Use custom economy only

PlaceholderAPI Not Found
  ├─ Log warning
  ├─ Custom economy unavailable
  └─ Use Vault economy only

Folia Detection Failed
  ├─ Log fine
  ├─ Fallback to Bukkit scheduler
  └─ Works on Paper/Spigot
```

---

## Performance Considerations

1. **Inventory Creation**: Cached on-demand, not pre-created
2. **YAML Parsing**: Done once at startup, can be reloaded
3. **Scheduler Tasks**: Minimal overhead, async where possible
4. **Economy Checks**: Direct API calls (no database queries)
5. **PlaceholderAPI**: Cached placeholder parsing

---

## Security Features

1. **Console Sender Execution**: Money deduction always uses console for permissions
2. **Permission Validation**: All commands check permissions
3. **Thread Safety**: Prevents race conditions in multi-threaded environments
4. **Input Validation**: Configuration values validated before use
5. **Error Logging**: All errors logged for admin review

---

## Extension Points

Plugin is designed to be extended:

1. **Custom Economy Types**: Add new currency types to `EconomyHandler`
2. **GUI Customization**: Extend `ShopGUI` for custom inventory layouts
3. **Event Handling**: Add listeners for additional shop events
4. **Command Extensions**: Add more subcommands to shop commands

---

## Troubleshooting Guide

### Issue: Plugin doesn't enable
**Solution:**
- Check Java version: `java -version` (need 17+)
- Check Paper version: `/version` (need 1.20+)
- Review logs for specific errors

### Issue: GUI click doesn't work
**Solution:**
- Verify Vault is installed: `/plugins`
- Check player permission: `lightshop.use`
- Review `config.yml` for syntax errors

### Issue: Money not deducting
**Solution:**
- Verify correct economy type in `config.yml`
- Test Vault with `/eco`
- Test custom economy command manually
- Check console logs for deduction errors

### Issue: Folia compatibility issues
**Solution:**
- Plugin auto-detects Folia
- If errors occur, check scheduler tasks
- Verify player thread context in logs
