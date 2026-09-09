```java
package me.none.powers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NonePowers extends JavaPlugin implements Listener, CommandExecutor {

    private static final String OWNER = "none";
    private static final String GUI = ChatColor.DARK_PURPLE + "NONE'S 300 POWERS";

    private final Map<String, Long> cooldowns = new HashMap<String, Long>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        if (getCommand("nonepowers") != null) {
            getCommand("nonepowers").setExecutor(this);
        }

        getLogger().info("NonePowers enabled - 300 powers loaded!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only a player can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.getName().equalsIgnoreCase(OWNER)) {
            player.sendMessage(ChatColor.RED +
                    "You are not allowed to use this command.");
            return true;
        }

        openMenu(player, 0);
        return true;
    }

    private void openMenu(Player player, int page) {

        Inventory inv = Bukkit.createInventory(
                null,
                54,
                GUI + ChatColor.GRAY + " - Page " + (page + 1)
        );

        int start = page * 45;

        for (int slot = 0; slot < 45; slot++) {

            int power = start + slot + 1;

            if (power > 300) {
                break;
            }

            inv.setItem(slot, createPowerItem(power));
        }

        if (page > 0) {
            inv.setItem(48, makeItem(
                    Material.ARROW,
                    ChatColor.YELLOW + "Previous Page"
            ));
        }

        inv.setItem(49, makeItem(
                Material.NETHER_STAR,
                ChatColor.LIGHT_PURPLE + "NONE - 300 POWERS"
        ));

        if ((page + 1) * 45 < 300) {
            inv.setItem(50, makeItem(
                    Material.ARROW,
                    ChatColor.YELLOW + "Next Page"
            ));
        }

        player.openInventory(inv);
    }

    private ItemStack createPowerItem(int number) {

        Material material;
        ChatColor color;
        String type;

        if (number <= 100) {
            material = Material.ENDER_PEARL;
            color = ChatColor.AQUA;
            type = "Titan TV Man Teleport";
        } else if (number <= 200) {
            material = Material.DIAMOND_SWORD;
            color = ChatColor.RED;
            type = "Hit / Attack";
        } else {
            material = Material.NETHER_STAR;
            color = ChatColor.LIGHT_PURPLE;
            type = "Custom Item";
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(
                color.toString() + ChatColor.BOLD +
                        getPowerName(number)
        );

        List<String> lore = new ArrayList<String>();

        lore.add(ChatColor.GRAY + "Power #" + number);
        lore.add(ChatColor.GRAY + type);
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to activate!");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private String getPowerName(int number) {

        if (number <= 100) {
            return "TV Warp " + number;
        }

        if (number <= 200) {
            return "Titan Strike " + (number - 100);
        }

        return "None Artifact " + (number - 200);
    }

    private ItemStack makeItem(Material material, String name) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (!event.getView().getTitle().startsWith(GUI)) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        if (!player.getName().equalsIgnoreCase(OWNER)) {
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();

        if (slot == 48) {
            int page = getPage(event.getView().getTitle());

            if (page > 0) {
                openMenu(player, page - 1);
            }

            return;
        }

        if (slot == 50) {
            int page = getPage(event.getView().getTitle());

            if ((page + 1) * 45 < 300) {
                openMenu(player, page + 1);
            }

            return;
        }

        if (slot < 0 || slot >= 45) {
            return;
        }

        int page = getPage(event.getView().getTitle());
        int power = page * 45 + slot + 1;

        if (power > 300) {
            return;
        }

        player.closeInventory();
        activatePower(player, power);
    }

    private int getPage(String title) {

        try {
            return Integer.parseInt(
                    title.substring(title.indexOf("Page ") + 5)
            ) - 1;
        } catch (Exception e) {
            return 0;
        }
    }

    private void activatePower(Player player, int power) {

        if (!player.getName().equalsIgnoreCase(OWNER)) {
            return;
        }

        if (cooldown(player, power)) {
            return;
        }

        setCooldown(player, power, 1500);

        if (power <= 100) {
            teleportPower(player, power);
        } else if (power <= 200) {
            attackPower(player, power - 100);
        } else {
            artifactPower(player, power - 200);
        }
    }

    private void teleportPower(Player player, int id) {

        Location old = player.getLocation().clone();

        Vector direction = old.getDirection().normalize();

        double distance = 5 + (id % 10) * 2;

        if (id >= 90) {
            distance += 20;
        }

        Location destination = old.clone().add(
                direction.multiply(distance)
        );

        if (!safe(destination)) {
            destination = findSafe(old, direction, distance);
        }

        player.teleport(destination);

        effect(old);
        effect(destination);

        player.playSound(
                destination,
                Sound.ENTITY_ENDERMEN_TELEPORT,
                1.0F,
                0.8F
        );

        if (id >= 25) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED,
                    100,
                    1
            ));
        }

        if (id >= 50) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.JUMP,
                    100,
                    2
            ));
        }

        if (id >= 75) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.NIGHT_VISION,
                    200,
                    0
            ));
        }

        player.sendMessage(
                ChatColor.AQUA + "TV Warp " + id +
                        ChatColor.WHITE + " activated!"
        );
    }

    private boolean safe(Location location) {

        return !location.getBlock().getType().isSolid()
                && !location.clone().add(0, 1, 0)
                .getBlock().getType().isSolid();
    }

    private Location findSafe(
            Location start,
            Vector direction,
            double distance) {

        for (int i = (int) distance; i >= 1; i--) {

            Location test = start.clone().add(
                    direction.clone().multiply(i)
            );

            if (safe(test)) {
                return test;
            }
        }

        return start;
    }

    private void effect(Location location) {

        World world = location.getWorld();

        world.spawnParticle(
                Particle.PORTAL,
                location,
                60,
                0.5,
                1,
                0.5,
                0.2
        );
    }

    private void attackPower(Player player, int id) {

        double radius = 3 + (id % 10);

        if (id >= 80) {
            radius += 5;
        }

        List<Entity> entities =
                player.getNearbyEntities(radius, radius, radius);

        int hits = 0;

        for (Entity entity : entities) {

            if (!(entity instanceof LivingEntity)) {
                continue;
            }

            if (entity == player) {
                continue;
            }

            LivingEntity target = (LivingEntity) entity;

            double damage = 4 + (id % 10);

            if (id >= 90) {
                damage += 10;
            }

            target.damage(damage, player);

            Vector knockback =
                    target.getLocation().toVector()
                            .subtract(player.getLocation().toVector());

            if (knockback.length() > 0) {
                knockback.normalize();
            }

            target.setVelocity(
                    knockback.multiply(0.6).setY(0.5)
            );

            target.getWorld().spawnParticle(
                    Particle.CRIT,
                    target.getLocation().add(0, 1, 0),
                    20,
                    0.4,
                    0.5,
                    0.4,
                    0.1
            );

            hits++;
        }

        player.getWorld().spawnParticle(
                Particle.EXPLOSION_LARGE,
                player.getLocation(),
                id >= 90 ? 8 : 2,
                1,
                0.5,
                1,
                0
        );

        player.playSound(
                player.getLocation(),
                Sound.ENTITY_IRONGOLEM_ATTACK,
                1.0F,
                0.7F
        );

        player.sendMessage(
                ChatColor.RED + "Titan Strike " + id +
                        ChatColor.WHITE + " hit " +
                        hits + " target(s)!"
        );

        if (id >= 20) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INCREASE_DAMAGE,
                    100,
                    id / 25
            ));
        }

        if (id >= 40) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE,
                    80,
                    1
            ));
        }

        if (id >= 80) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.FIRE_RESISTANCE,
                    200,
                    0
            ));
        }

        if (id == 100) {

            for (Entity entity : entities) {

                if (!(entity instanceof LivingEntity)
                        || entity == player) {
                    continue;
                }

                LivingEntity target =
                        (LivingEntity) entity;

                target.setFireTicks(100);

                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOW,
                        100,
                        3
                ));
            }

            player.sendMessage(
                    ChatColor.DARK_RED.toString() +
                            ChatColor.BOLD +
                            "ULTIMATE TITAN STRIKE!"
            );
        }
    }

    private void artifactPower(Player player, int id) {

        Material material;
        String name;

        switch (id % 10) {

            case 0:
                material = Material.NETHER_STAR;
                name = "None's Reality Core";
                break;

            case 1:
                material = Material.BLAZE_ROD;
                name = "Titan Power Rod";
                break;

            case 2:
                material = Material.ENDER_PEARL;
                name = "TV Teleport Core";
                break;

            case 3:
                material = Material.DIAMOND;
                name = "Titan Energy Crystal";
                break;

            case 4:
                material = Material.GOLD_INGOT;
                name = "Golden TV Core";
                break;

            case 5:
                material = Material.REDSTONE;
                name = "Red Screen Core";
                break;

            case 6:
                material = Material.EMERALD;
                name = "Power Emerald";
                break;

            case 7:
                material = Material.OBSIDIAN;
                name = "Dimension Core";
                break;

            case 8:
                material = Material.IRON_BLOCK;
                name = "Titan Armor Core";
                break;

            default:
                material = Material.DIAMOND_BLOCK;
                name = "Ultimate Core";
                break;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(
                ChatColor.LIGHT_PURPLE +
                        name + " #" + id
        );

        List<String> lore = new ArrayList<String>();

        lore.add(ChatColor.GRAY +
                "None Artifact #" + id);

        lore.add("");

        lore.add(ChatColor.YELLOW +
                "Special Power:");

        lore.add(ChatColor.WHITE +
                getArtifactDescription(id));

        meta.setLore(lore);
        item.setItemMeta(meta);

        player.getInventory().addItem(item);

        player.getWorld().spawnParticle(
                Particle.ENCHANTMENT_TABLE,
                player.getLocation().add(0, 1, 0),
                40,
                1,
                1,
                1,
                0.5
        );

        player.playSound(
                player.getLocation(),
                Sound.ENTITY_PLAYER_LEVELUP,
                1.0F,
                1.5F
        );

        player.sendMessage(
                ChatColor.LIGHT_PURPLE +
                        "You received None Artifact #" + id + "!"
        );
    }

    private String getArtifactDescription(int id) {

        switch (id % 10) {

            case 0:
                return "Reality manipulation.";

            case 1:
                return "Titan-strength energy.";

            case 2:
                return "Instant teleportation.";

            case 3:
                return "Massive energy storage.";

            case 4:
                return "Enhanced power output.";

            case 5:
                return "Devastating screen energy.";

            case 6:
                return "Regenerative power.";

            case 7:
                return "Dimensional energy.";

            case 8:
                return "Titan defensive armor.";

            default:
                return "Ultimate None energy.";
        }
    }

    private String key(Player player, int power) {
        return player.getUniqueId().toString() + ":" + power;
    }

    private boolean cooldown(Player player, int power) {

        String key = key(player, power);

        Long end = cooldowns.get(key);

        if (end == null) {
            return false;
        }

        long remaining = end - System.currentTimeMillis();

        if (remaining <= 0) {
            cooldowns.remove(key);
            return false;
        }

        player.sendMessage(
                ChatColor.RED +
                        "That power is on cooldown for " +
                        (remaining / 1000.0) +
                        " seconds."
        );

        return true;
    }

    private void setCooldown(
            Player player,
            int power,
            long milliseconds) {

        cooldowns.put(
                key(player, power),
                System.currentTimeMillis() + milliseconds
        );
    }
}
```

And make sure your **`plugin.yml`** has this:

```yaml
name: NonePowers
version: 1.0
main: me.none.powers.NonePowers
api-version: 1.12
author: None
description: None's 300 Powers plugin

commands:
  nonepowers:
    description: Open None's 300 Powers menu
    usage: /nonepowers
```

Then rebuild the JAR and upload the new one.

**Test command:**

```text
/nonepowers
```

And yes, the `none` username restriction is still there.
