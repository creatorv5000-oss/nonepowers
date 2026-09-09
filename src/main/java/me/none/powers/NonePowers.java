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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NonePowers extends JavaPlugin implements Listener, CommandExecutor {

    private static final String OWNER = "none";
    private static final String GUI = ChatColor.DARK_PURPLE + "NONE'S 300 POWERS";

    private final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        if (getCommand("nonepowers") != null) {
            getCommand("nonepowers").setExecutor(this);
        }

        getLogger().info("NonePowers enabled - 300 powers loaded!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only a player can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.getName().equalsIgnoreCase(OWNER)) {
            player.sendMessage(ChatColor.RED + "You are not allowed to use this.");
            return true;
        }

        openMenu(player, 0);
        return true;
    }

    private void openMenu(Player player, int page) {

        Inventory inventory = Bukkit.createInventory(
                null,
                45,
                GUI + ChatColor.GRAY + " | Page " + (page + 1)
        );

        int start = page * 36;

        for (int i = 0; i < 36; i++) {

            int power = start + i + 1;

            if (power > 300) {
                break;
            }

            ItemStack item;

            if (power <= 100) {
                item = new ItemStack(Material.ENDER_PEARL);
            } else if (power <= 200) {
                item = new ItemStack(Material.DIAMOND_SWORD);
            } else {
                item = new ItemStack(Material.NETHER_STAR);
            }

            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(
                        ChatColor.LIGHT_PURPLE + "Power #" + power
                );

                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + getPowerName(power),
                        "",
                        ChatColor.YELLOW + "Click to activate!"
                ));

                item.setItemMeta(meta);
            }

            inventory.setItem(i, item);
        }

        if (page > 0) {
            ItemStack previous = new ItemStack(Material.ARROW);
            ItemMeta meta = previous.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "Previous Page");
                previous.setItemMeta(meta);
            }

            inventory.setItem(36, previous);
        }

        if (page < 8) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "Next Page");
                next.setItemMeta(meta);
            }

            inventory.setItem(44, next);
        }

        player.openInventory(inventory);
    }

    private String getPowerName(int power) {

        if (power <= 100) {
            return "TV Warp Power";
        }

        if (power <= 200) {
            return "Titan Strike Power";
        }

        return "None Artifact Power";
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (!player.getName().equalsIgnoreCase(OWNER)) {
            return;
        }

        if (!event.getView().getTitle().startsWith(GUI)) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slot < 0 || slot >= 45) {
            return;
        }

        String title = event.getView().getTitle();

        int page = 0;

        if (title.contains("Page 2")) {
            page = 1;
        } else if (title.contains("Page 3")) {
            page = 2;
        } else if (title.contains("Page 4")) {
            page = 3;
        } else if (title.contains("Page 5")) {
            page = 4;
        } else if (title.contains("Page 6")) {
            page = 5;
        } else if (title.contains("Page 7")) {
            page = 6;
        } else if (title.contains("Page 8")) {
            page = 7;
        } else if (title.contains("Page 9")) {
            page = 8;
        }

        if (slot == 36 && page > 0) {
            openMenu(player, page - 1);
            return;
        }

        if (slot == 44 && page < 8) {
            openMenu(player, page + 1);
            return;
        }

        if (slot >= 36) {
            return;
        }

        int power = (page * 36) + slot + 1;

        if (power < 1 || power > 300) {
            return;
        }

        activatePower(player, power);
    }

    private void activatePower(Player player, int power) {

        if (!player.getName().equalsIgnoreCase(OWNER)) {
            return;
        }

        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();
        Long last = cooldowns.get(uuid);

        if (last != null && now - last < 1500) {
            return;
        }

        cooldowns.put(uuid, now);

        if (power <= 100) {
            tvWarp(player, power);
        } else if (power <= 200) {
            titanStrike(player, power);
        } else {
            artifactPower(player, power);
        }
    }

    private void tvWarp(Player player, int power) {

        World world = player.getWorld();

        Location location = player.getLocation().clone();

        double distance = 5 + (power % 20);

        Vector direction = location.getDirection().normalize();

        location.add(direction.multiply(distance));

        location.setY(location.getY() + 1);

        player.teleport(location);

        world.spawnParticle(
                Particle.PORTAL,
                location,
                80,
                1,
                1,
                1,
                0.5
        );

        world.playSound(
                location,
                Sound.ENDERMAN_TELEPORT,
                1.0f,
                1.0f
        );
    }

    private void titanStrike(Player player, int power) {

        Location location = player.getLocation();

        double radius = 4 + (power % 6);

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {

            if (!(entity instanceof LivingEntity)) {
                continue;
            }

            if (entity.equals(player)) {
                continue;
            }

            LivingEntity target = (LivingEntity) entity;

            double damage = 6 + (power % 15);

            target.damage(damage, player);

            Vector knockback = target.getLocation()
                    .toVector()
                    .subtract(player.getLocation().toVector())
                    .normalize()
                    .multiply(1.5);

            knockback.setY(0.8);

            target.setVelocity(knockback);
        }

        player.getWorld().spawnParticle(
                Particle.EXPLOSION_HUGE,
                location,
                1
        );

        player.getWorld().playSound(
                location,
                Sound.ENTITY_GENERIC_EXPLODE,
                1.0f,
                0.8f
        );
    }

    private void artifactPower(Player player, int power) {

        int type = power % 5;

        if (type == 0) {

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.SPEED,
                            20 * 15,
                            3
                    )
            );

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.JUMP,
                            20 * 15,
                            3
                    )
            );

        } else if (type == 1) {

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.DAMAGE_RESISTANCE,
                            20 * 15,
                            4
                    )
            );

        } else if (type == 2) {

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.INCREASE_DAMAGE,
                            20 * 15,
                            3
                    )
            );

        } else if (type == 3) {

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.REGENERATION,
                            20 * 10,
                            4
                    )
            );

        } else {

            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
        }

        player.getWorld().spawnParticle(
                Particle.TOTEM,
                player.getLocation().add(0, 1, 0),
                50,
                1,
                1,
                1,
                0.5
        );

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ITEM_TOTEM_USE,
                1.0f,
                1.0f
        );
    }
}
