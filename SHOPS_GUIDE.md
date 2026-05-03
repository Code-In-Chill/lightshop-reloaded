# LightShop - Shops Folder Structure Guide

## Cấu Trúc Thư Mục Mới

Từ phiên bản này, LightShop sử dụng cấu trúc thư mục mới để tổ chức vật phẩm cửa hàng theo từng loại:

```
plugins/LightShop/
├── config.yml              # Main configuration
├── shop.yml                # Legacy file (reference only)
└── shops/                  # NEW: Shop categories folder
    ├── materials.yml       # Materials shop
    ├── weapons.yml         # Weapons shop
    ├── armor.yml           # Armor shop
    ├── blocks.yml          # Blocks shop
    └── food.yml            # Food shop
```

## Lợi Ích của Cấu Trúc Mới

✅ **Tổ chức tốt hơn** - Mỗi loại vật phẩm trong file riêng
✅ **Dễ bảo trì** - Tìm kiếm và chỉnh sửa vật phẩm nhanh hơn
✅ **Khả năng mở rộng** - Dễ dàng thêm loại cửa hàng mới
✅ **Hot-reload** - Reload config mà không cần restart
✅ **Tránh file quá lớn** - Không có file khổng lồ chứa tất cả vật phẩm

## File Cửa Hàng Mặc Định

### 1. materials.yml (Vật Liệu)
```
- diamond      → $100 (Vault)
- emerald      → $80 (Vault)
- gold_ingot   → 50 Points (Custom)
- iron_ingot   → $40 (Vault)
- copper_ingot → $35 (Vault)
- lapis_lazuli → $60 (Vault)
- coal         → $20 (Vault)
```

### 2. weapons.yml (Vũ Khí)
```
- enchanted_diamond_sword  → $500
- iron_sword               → $150
- netherite_sword          → 800 Points
- bow                      → $200
- crossbow                 → $300
- diamond_pickaxe          → $400
```

### 3. armor.yml (Giáp)
```
- diamond_helmet       → $350
- diamond_chestplate   → $450
- diamond_leggings     → $350
- diamond_boots        → $300
- netherite_helmet     → 600 Points
- iron_helmet          → $100
- shield               → $120
```

### 4. blocks.yml (Khối)
```
- netherite_block   → 1000 Points
- diamond_block     → $800
- gold_block        → $300
- iron_block        → $200
- emerald_block     → $400
- obsidian          → $150
- bedrock           → 2000 Points
```

### 5. food.yml (Thức Ăn)
```
- golden_apple             → $250
- enchanted_golden_apple   → 500 Points
- cooked_beef              → $25
- cooked_chicken           → $15
- cake                     → $50
- pumpkin_pie              → $30
- potion_healing           → $80
```

## Tạo Shop Loại Mới

### Bước 1: Tạo File Mới
Tạo file mới trong thư mục `plugins/LightShop/shops/`
Ví dụ: `custom_items.yml`

### Bước 2: Thêm Vật Phẩm
```yaml
# ========================================
# CUSTOM ITEMS SHOP
# ========================================

my_custom_item:
  material: DIAMOND_AXE
  name: "&5Legendary Axe"
  lore:
    - "&7A powerful custom item"
    - "&7Made with love"
    - "&eCost: $999"
  price: 999.0
  currency: "vault"
  amount: 1

another_item:
  material: BOOK
  name: "&bMagic Book"
  lore:
    - "&7Contains ancient knowledge"
    - "&eCost: 100 Points"
  price: 100.0
  currency: "custom"
  amount: 1
```

### Bước 3: Reload
```
/lightshop reload
```

**Hoàn tất!** Plugin sẽ tự động tải vật phẩm từ file mới.

## Format Vật Phẩm Chi Tiết

```yaml
item_id:                         # Unique identifier (không dấu cách)
  material: MINECRAFT_MATERIAL   # Material name (viết hoa)
  name: "&cDisplay Name"         # Tên hiển thị (hỗ trợ color code)
  lore:                          # Mô tả (danh sách)
    - "&7Line 1"
    - "&7Line 2"
  price: 100.0                   # Giá (số thực)
  currency: "vault"              # "vault" hoặc "custom"
  amount: 1                      # Số lượng mỗi lần mua (tùy chọn)
```

## Color Codes

Sử dụng `&` để đặt màu:

```
&0 = Black        &8 = Dark Gray
&1 = Dark Blue    &9 = Blue
&2 = Dark Green   &a = Green
&3 = Dark Cyan    &b = Cyan
&4 = Dark Red     &c = Red
&5 = Purple       &d = Magenta
&6 = Gold         &e = Yellow
&7 = Gray         &f = White

&k = Magic
&l = Bold
&m = Strikethrough
&n = Underline
&o = Italic
&r = Reset
```

**Ví dụ:**
```yaml
name: "&e&lGolden Sword"          # Yellow, Bold
lore:
  - "&7Normal text"                # Gray
  - "&c&nRed Underline"            # Red, Underline
  - "&a✓ Available"                # Green
```

## Material Codes

Để tìm tên Material Minecraft, xem danh sách:
- `DIAMOND`, `EMERALD`, `GOLD_INGOT`, `IRON_INGOT`
- `DIAMOND_SWORD`, `IRON_SWORD`, `NETHERITE_SWORD`
- `DIAMOND_HELMET`, `DIAMOND_CHESTPLATE`, `DIAMOND_LEGGINGS`, `DIAMOND_BOOTS`
- `DIAMOND_PICKAXE`, `DIAMOND_AXE`, `DIAMOND_SHOVEL`
- `BOW`, `CROSSBOW`, `SHIELD`
- Xem full list: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html

## Quản Lý Shop

### Thêm Vật Phẩm Mới
1. Mở file YAML trong `shops/` folder
2. Thêm item vào cuối file
3. `/lightshop reload`

### Chỉnh Sửa Vật Phẩm Hiện Tại
1. Mở file YAML
2. Sửa giá, tên, lore
3. `/lightshop reload`

### Xóa Vật Phẩm
1. Xóa section item từ file YAML
2. `/lightshop reload`

### Tạo Shop Mới
1. Tạo file `.yml` mới trong `shops/`
2. Thêm vật phẩm
3. `/lightshop reload`

## Troubleshooting

### Shop items không load
```
Kiểm tra:
1. File .yml có trong shops/ folder?
2. File có lỗi YAML syntax?
3. Check logs: tail -f logs/latest.log
```

### Item hiển thị sai giá
```
Kiểm tra:
1. Currency đúng? ("vault" hoặc "custom")
2. Giá là số? (100.0 đúng, "100" sai)
3. Reload config: /lightshop reload
```

### File không được load
```
1. Đặt tên file có dấu cách sẽ không load
2. Sử dụng: my_items.yml (tốt)
3. Tránh: my items.yml (xấu)
```

### YAML Syntax Error
Kiểm tra:
- Indentation (2 spaces, không tab)
- Colon sau property name
- List items bắt đầu với `-`

```yaml
# ✓ Đúng
item:
  material: DIAMOND
  price: 100.0
  
# ✗ Sai
item
  material: DIAMOND
  price: 100.0
```

## Performance Notes

- Plugin tự động cache tất cả vật phẩm khi load
- Reload config có thể mất vài giây nếu nhiều file
- Tối ưu: Giữ mỗi file ~50-100 items

## Migration từ shop.yml cũ

Nếu bạn đã dùng phiên bản cũ:

1. **Backup** file `shop.yml` cũ
2. **Tạo** thư mục `shops/`
3. **Copy** vật phẩm vào các file theo loại
4. **Delete** nội dung `shop.yml` (giữ lại file reference)
5. `/lightshop reload`

Hoặc plugin sẽ tự động extract files mặc định lần đầu chạy.

## Ví Dụ Hoàn Chỉnh

**File: shops/gaming.yml**
```yaml
# ========================================
# GAMING ITEMS SHOP
# ========================================

gaming_pc:
  material: REDSTONE
  name: "&6&lGaming PC"
  lore:
    - "&7High-end computer"
    - "&7For ultimate gaming"
    - "&7&o✦ Rare Item ✦"
    - "&eCost: $5000"
  price: 5000.0
  currency: "vault"
  amount: 1

gaming_chair:
  material: OAK_LOG
  name: "&c&lGaming Chair"
  lore:
    - "&7Ergonomic design"
    - "&7RGB lighting"
    - "&eCost: $2000"
  price: 2000.0
  currency: "vault"
  amount: 1

vip_pass:
  material: NETHER_STAR
  name: "&5&lVIP Pass"
  lore:
    - "&7Exclusive access"
    - "&7Premium features"
    - "&eCost: 1000 Points"
  price: 1000.0
  currency: "custom"
  amount: 1
```

Reload: `/lightshop reload` ✓

---

**Version:** 1.0.0+
**Tính năng:** Multi-file shop categories
**Hỗ trợ:** Unlimited shop files
