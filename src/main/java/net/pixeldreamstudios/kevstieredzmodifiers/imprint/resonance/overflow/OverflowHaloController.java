package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.overflow;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellEvents;
import net.spell_engine.entity.SpellProjectile;
import net.spell_engine.internals.SpellHelper;

public final class OverflowHaloController {

    public static final String K_ORBIT_RADIUS  = "kevs-tieredz-modifiers:overflow_orbit_radius";
    public static final String K_ORBIT_HEIGHT  = "kevs-tieredz-modifiers:overflow_orbit_height";
    public static final String K_ORBIT_SPEED   = "kevs-tieredz-modifiers:overflow_orbit_speed";
    public static final String K_RELEASE_RANGE = "kevs-tieredz-modifiers:overflow_release_range";
    public static final String K_LAUNCH_SPEED  = "kevs-tieredz-modifiers:overflow_launch_speed";

    public static final float DEF_ORBIT_RADIUS  = 1.6f;
    public static final float DEF_ORBIT_HEIGHT  = 2.2f;
    public static final float DEF_ORBIT_SPEED   = 0.08f;
    public static final float DEF_RELEASE_RANGE = 10.0f;
    public static final float DEF_LAUNCH_SPEED  = 1.4f;

    private static final int MIN_ORBIT_TICKS = 16;

    private OverflowHaloController() {
    }

    public static void spawnHalo(SpellEvents.ProjectileLaunchEvent event, int count, int lifeTicks) {
        LivingEntity caster = event.caster();
        if (caster == null || !(caster.getWorld() instanceof ServerWorld world)) return;
        RegistryEntry<Spell> spellEntry = event.spellEntry();
        SpellHelper.ImpactContext context = event.context();
        Spell.ProjectileData.Perks perks = event.projectile() != null ? event.projectile().mutablePerks() : null;

        float height = orbitHeight(caster);
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count;
            float radius = orbitRadius(caster);

            double sx = caster.getX() + Math.cos(angle) * radius;
            double sz = caster.getZ() + Math.sin(angle) * radius;
            SpellProjectile p = new SpellProjectile(world, caster,
                    sx, caster.getY() + height, sz,
                    SpellProjectile.Behaviour.FLY, spellEntry, context, perks);

            p.setVelocity(Vec3d.ZERO);
            OverflowHalo halo = (OverflowHalo) p;
            halo.kevs$setHalo(true);
            halo.kevs$setHaloOwner(caster.getUuid());
            halo.kevs$setHaloAngle((float) angle);
            halo.kevs$setHaloLife(lifeTicks);
            world.spawnEntity(p);
        }
    }

    public static boolean tickHalo(SpellProjectile projectile) {
        OverflowHalo halo = (OverflowHalo) projectile;
        if (!(projectile.getWorld() instanceof ServerWorld world)) return true;

        LivingEntity owner = owner(world, halo);
        int life = halo.kevs$haloLife() - 1;
        halo.kevs$setHaloLife(life);
        if (owner == null || life <= 0) {
            projectile.discard();
            return true;
        }

        LivingEntity attacker = projectile.age >= MIN_ORBIT_TICKS ? nearestAttacker(world, owner) : null;
        if (attacker != null) {
            release(projectile, halo, attacker, owner);
            return false;
        }

        float angle = halo.kevs$haloAngle() + orbitSpeed(owner);
        halo.kevs$setHaloAngle(angle);
        float radius = orbitRadius(owner);
        float height = orbitHeight(owner);
        double x = owner.getX() + Math.cos(angle) * radius;
        double z = owner.getZ() + Math.sin(angle) * radius;
        double y = owner.getY() + height;

        double tx = -Math.sin(angle);
        double tz = Math.cos(angle);
        float yaw = (float) (Math.toDegrees(Math.atan2(tz, tx)) - 90.0);
        projectile.setYaw(yaw);
        projectile.setPitch(0f);
        projectile.prevYaw = yaw;
        projectile.prevPitch = 0f;
        projectile.refreshPositionAfterTeleport(x, y, z);
        projectile.setVelocity(Vec3d.ZERO);
        world.spawnParticles(ParticleTypes.ENCHANT, x, y, z, 1, 0.05, 0.05, 0.05, 0.01);
        return true;
    }

    private static void release(SpellProjectile projectile, OverflowHalo halo, LivingEntity attacker, LivingEntity owner) {
        halo.kevs$setHalo(false);

        Vec3d from = projectile.getPos();
        Vec3d to = attacker.getBoundingBox().getCenter();
        Vec3d dir = to.subtract(from).normalize();
        float speed = owner instanceof PlayerEntity p
                ? OverflowSpellPower.stashedF(p, K_LAUNCH_SPEED, DEF_LAUNCH_SPEED)
                : DEF_LAUNCH_SPEED;

        float yaw = (float) (Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0);
        float pitch = (float) Math.toDegrees(-Math.asin(dir.y));
        projectile.setYaw(yaw);
        projectile.setPitch(pitch);
        projectile.prevYaw = yaw;
        projectile.prevPitch = pitch;
        projectile.setVelocity(dir.x, dir.y, dir.z, speed, 0f);
        projectile.setFollowedTarget(attacker);
        if (projectile.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(ParticleTypes.WITCH, projectile.getX(), projectile.getY(), projectile.getZ(),
                    8, 0.15, 0.15, 0.15, 0.05);
        }
    }

    private static LivingEntity owner(ServerWorld world, OverflowHalo halo) {
        if (halo.kevs$haloOwner() == null) return null;
        Entity e = world.getEntity(halo.kevs$haloOwner());
        return e instanceof LivingEntity l && l.isAlive() ? l : null;
    }

    private static LivingEntity nearestAttacker(ServerWorld world, LivingEntity owner) {
        float range = owner instanceof PlayerEntity p
                ? OverflowSpellPower.stashedF(p, K_RELEASE_RANGE, DEF_RELEASE_RANGE)
                : DEF_RELEASE_RANGE;
        Box box = owner.getBoundingBox().expand(range);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : world.getOtherEntities(owner, box,
                x -> x instanceof MobEntity m && m.getTarget() == owner)) {
            double d = e.squaredDistanceTo(owner);
            if (d < bestDist) { bestDist = d; best = (LivingEntity) e; }
        }
        return best;
    }

    private static float orbitRadius(LivingEntity owner) {
        return owner instanceof PlayerEntity p
                ? OverflowSpellPower.stashedF(p, K_ORBIT_RADIUS, DEF_ORBIT_RADIUS) : DEF_ORBIT_RADIUS;
    }

    private static float orbitHeight(LivingEntity owner) {
        return owner instanceof PlayerEntity p
                ? OverflowSpellPower.stashedF(p, K_ORBIT_HEIGHT, DEF_ORBIT_HEIGHT) : DEF_ORBIT_HEIGHT;
    }

    private static float orbitSpeed(LivingEntity owner) {
        return owner instanceof PlayerEntity p
                ? OverflowSpellPower.stashedF(p, K_ORBIT_SPEED, DEF_ORBIT_SPEED) : DEF_ORBIT_SPEED;
    }
}
