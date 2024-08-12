package me.abobymotaki.enchantments;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Enchantments extends JavaPlugin {

    private FileConfiguration config;
    private static Enchantments instance;

    @Override
    public void onEnable() {
        instance = this;
        // Saves the default config.yml if it does not exist
        this.saveDefaultConfig();
        this.config = this.getConfig();

        // Register the command
        this.getCommand("missiles").setExecutor(new MissileCommand(this));

        getLogger().info("Enchantments plugin has been enabled!");
    }

    public static Enchantments getInstance() {
        return instance;
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }

    public void launchMissile(Player player) {
        int radius = config.getInt("explosionRadius");
        Particle trailParticle = Particle.valueOf(config.getString("missileParticle", "SMOKE_NORMAL"));
        Particle explosionParticle = Particle.valueOf(config.getString("explosionParticle", "EXPLOSION_LARGE"));
        int trailParticleCount = config.getInt("missileParticleCount", 10);
        int explosionParticleCount = config.getInt("explosionParticleCount", 1);
        boolean fireballEnabled = config.getBoolean("fireballEnabled", true);
        EntityType missileEntityType = EntityType.valueOf(config.getString("missileEntity", "FIREBALL").toUpperCase());
        Location target = player.getLocation().subtract(0, config.getInt("targetYOffset", 5), 0); // Adjust target location

        int spawnDistanceX = config.getInt("spawnDistance.x", 40);
        int spawnDistanceY = config.getInt("spawnDistance.y", 20);
        int spawnDistanceZ = config.getInt("spawnDistance.z", 40);

        for (int i = 0; i < 5; i++) {
            Location missileStart = target.clone().add((Math.random() - 0.5) * spawnDistanceX, spawnDistanceY, (Math.random() - 0.5) * spawnDistanceZ);
            int finalI = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    new Missile(missileStart, target, radius, trailParticle, explosionParticle, trailParticleCount, explosionParticleCount, missileEntityType).start();
                }
            }.runTaskLater(this, finalI * 10); // Interval of 0.5 seconds (10 ticks)
        }
    }

    public boolean isBlockInRegionWithFlag(Location location, String flag) {
        // WorldGuardPlugin wgPlugin = WorldGuardPlugin.inst();
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager == null) {
            return false;
        }

        ApplicableRegionSet regions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        for (ProtectedRegion region : regions) {
            if (region.getFlags().containsKey(flag)) {
                return true;
            }
        }
        return false;
    }
}