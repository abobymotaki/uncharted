package me.abobymotaki.enchantments;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Missile {

    private Location start;
    private Location target;
    private int radius;
    private int trailParticleCount;
    private int explosionParticleCount;
    private Particle trailParticle;
    private Particle explosionParticle;
    private EntityType missileEntityType;
    private Projectile missileEntity;

    public Missile(Location start, Location target, int radius, Particle trailParticle, Particle explosionParticle, int trailParticleCount, int explosionParticleCount, EntityType missileEntityType) {
        this.start = start;
        this.target = target;
        this.radius = radius;
        this.trailParticle = trailParticle;
        this.explosionParticle = explosionParticle;
        this.trailParticleCount = trailParticleCount;
        this.explosionParticleCount = explosionParticleCount;
        this.missileEntityType = missileEntityType;

        this.missileEntity = (Projectile) start.getWorld().spawnEntity(start, missileEntityType);
        if (missileEntity instanceof Fireball) {
            ((Fireball) missileEntity).setIsIncendiary(false);
            ((Fireball) missileEntity).setYield(0);
        }
        missileEntity.setSilent(true);
        missileEntity.setGlowing(true);

        Vector initialVelocity = target.toVector().subtract(start.toVector()).normalize();
        missileEntity.setVelocity(initialVelocity);
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (missileEntity != null && !missileEntity.isDead()) {
                    Vector direction = target.clone().subtract(missileEntity.getLocation()).toVector().normalize();
                    missileEntity.setVelocity(direction);
                    missileEntity.getWorld().spawnParticle(trailParticle, missileEntity.getLocation(), trailParticleCount, 0.2, 0.2, 0.2, 0.01);

                    if (isCollidingWithBlock(missileEntity.getLocation())) {
                        explode();
                        missileEntity.remove();
                        cancel();
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(Enchantments.getPlugin(Enchantments.class), 0L, 1L);
    }

    private boolean isCollidingWithBlock(Location location) {
        int radius = 1;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = location.clone().add(x, y, z);
                    if (loc.getBlock().getType() != Material.AIR) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void explode() {
        Location explosionLocation = missileEntity.getLocation();
        target.getWorld().spawnParticle(explosionParticle, explosionLocation, explosionParticleCount);
        target.getWorld().createExplosion(explosionLocation, 0.0F);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = explosionLocation.clone().add(x, y, z);
                    if (loc.distance(explosionLocation) <= radius && loc.getBlock().getType() != Material.AIR) {
                        String flag = Enchantments.getInstance().getConfig().getString("regionFlag", "EDP-ENCHANTS");
                        if (Enchantments.getInstance().isBlockInRegionWithFlag(loc, flag)) {
                            loc.getBlock().setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }
}
