package net.pixeldreamstudios.kevstieredzmodifiers.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.TargetFilter;

public class AftershockEntity extends Entity {

    public static EntityType<AftershockEntity> TYPE;

    private int waves = 1;
    private int waveInterval = 20;
    private float[] waveRadii = {4f};
    private float[] waveDamageFraction = {0.5f};
    private float sourceDamage = 0f;
    private UUID ownerUuid;
    private int timeToLive = 80;
    private int nextWave = 0;

    private TargetFilter targetFilter = TargetFilter.LEGACY;
    private boolean hitboxMode = false;
    private float waveHeight = 2f;
    private boolean bypassIframes = false;
    private boolean healingVariant = false;

    private static final TrackedData<Integer> SHAKE_COUNT =
            DataTracker.registerData(AftershockEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> WAVE_RADIUS =
            DataTracker.registerData(AftershockEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private static final TrackedData<Float> CONE_DIR =
            DataTracker.registerData(AftershockEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> CONE_HALF =
            DataTracker.registerData(AftershockEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private final List<ShakingBlock> shakingBlocks = new ArrayList<>();
    private PlayerEntity cachedOwner;

    public AftershockEntity(EntityType<? extends Entity> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    public void configure(PlayerEntity owner, float sourceDamage, int waves, int waveInterval,
            float[] radii, float[] damageFractions) {
        this.cachedOwner = owner;
        this.ownerUuid = owner.getUuid();
        this.sourceDamage = sourceDamage;
        this.waves = Math.max(1, waves);
        this.waveInterval = Math.max(1, waveInterval);
        this.waveRadii = radii;
        this.waveDamageFraction = damageFractions;

        this.timeToLive = waveInterval * this.waves + 70;
    }

    /** Extended configure: target filter (who can be hit), hitbox-mode + wave height, i-frame bypass. */
    public void configureBehavior(TargetFilter filter,
            boolean hitboxMode, float waveHeight, boolean bypassIframes) {
        configureBehavior(filter, hitboxMode, waveHeight, bypassIframes, false);
    }

    public void configureBehavior(TargetFilter filter,
            boolean hitboxMode, float waveHeight, boolean bypassIframes, boolean healingVariant) {
        if (filter != null) this.targetFilter = filter;
        this.hitboxMode = hitboxMode;
        if (waveHeight > 0f) this.waveHeight = waveHeight;
        this.bypassIframes = bypassIframes;
        this.healingVariant = healingVariant;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(SHAKE_COUNT, 0);
        builder.add(WAVE_RADIUS, 0f);
        builder.add(CONE_DIR, 0f);
        builder.add(CONE_HALF, 0f);
    }

    public void asCone(Vec3d dir, float halfAngleDeg) {
        float yaw = (float) Math.atan2(dir.z, dir.x);
        this.dataTracker.set(CONE_DIR, yaw);
        this.dataTracker.set(CONE_HALF, (float) Math.toRadians(halfAngleDeg));
    }

    private boolean inCone(double dx, double dz) {
        float half = this.dataTracker.get(CONE_HALF);
        if (half <= 0f) return true;
        float dir = this.dataTracker.get(CONE_DIR);
        double ang = Math.atan2(dz, dx);
        double diff = Math.abs(Math.atan2(Math.sin(ang - dir), Math.cos(ang - dir)));
        return diff <= half;
    }

    @Override
    public void tick() {
        super.tick();
        World world = this.getWorld();

        if (world.isClient()) {

            if (this.dataTracker.get(SHAKE_COUNT) > shakingBlocks.size()) {
                float r = this.dataTracker.get(WAVE_RADIUS);
                if (r > 0f) sampleRing(r, false);
            }
            return;
        }

        if (this.age > this.timeToLive) {
            this.discard();
            return;
        }

        if (nextWave < waves && this.age >= nextWave * waveInterval) {
            fireWave(nextWave);
            nextWave++;
        }
    }

    private void fireWave(int wave) {
        if (!(this.getWorld() instanceof ServerWorld world)) return;
        float radius = wave < waveRadii.length ? waveRadii[wave] : waveRadii[waveRadii.length - 1];
        float frac = wave < waveDamageFraction.length ? waveDamageFraction[wave]
                : waveDamageFraction[waveDamageFraction.length - 1];

        sampleRing(radius, false);
        this.dataTracker.set(WAVE_RADIUS, radius);
        this.dataTracker.set(SHAKE_COUNT, shakingBlocks.size());

        float inner = wave == 0 ? 0f : waveRadii[Math.max(0, wave - 1)];
        float waveDamage = sourceDamage * frac;
        PlayerEntity owner = owner();
        DamageSource src = owner != null ? owner.getDamageSources().playerAttack(owner)
                : world.getDamageSources().generic();
        float boxH = hitboxMode ? Math.max(4f, waveHeight * 2f) : 4f;
        Box box = Box.of(this.getPos(), radius * 2, boxH, radius * 2);
        double innerSq = inner * inner;
        double radiusSq = radius * radius;
        for (Entity e : world.getOtherEntities(this, box)) {
            if (!(e instanceof LivingEntity living) || e == owner) continue;
            if (!targetFilter.canHit(living, owner)) continue;
            double dx = e.getX() - this.getX();
            double dz = e.getZ() - this.getZ();
            double distSq = dx * dx + dz * dz;
            if (distSq < innerSq || distSq > radiusSq) continue;
            if (!inCone(dx, dz)) continue;
            if (hitboxMode && Math.abs(e.getY() - this.getY()) > waveHeight) continue;
            double dist = Math.sqrt(distSq);
            if (waveDamage > 0f) {
                if (bypassIframes) living.hurtTime = 0;
                if (bypassIframes) living.timeUntilRegen = 0;
                living.damage(src, waveDamage);
            }
            double kb = 0.6;
            living.addVelocity(dx / (dist + 0.01) * kb, 0.35, dz / (dist + 0.01) * kb);
            living.velocityModified = true;
        }

        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                SoundCategory.PLAYERS, 0.6f, 0.6f + wave * 0.1f);
        spawnWaveBurst(world, radius, wave);
    }

    // Ring burst around the wave radius + central emitter on the first wave. Distinct palette for the healing variant.
    private void spawnWaveBurst(ServerWorld world, float radius, int wave) {
        double cx = this.getX(), cy = this.getY() + 0.2, cz = this.getZ();
        int points = Math.max(8, (int) (radius * 6));
        for (int i = 0; i < points; i++) {
            double a = (Math.PI * 2 * i) / points;
            double px = cx + Math.cos(a) * radius;
            double pz = cz + Math.sin(a) * radius;
            if (healingVariant) {
                world.spawnParticles(ParticleTypes.END_ROD, px, cy, pz, 1, 0.05, 0.15, 0.05, 0.01);
                world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, px, cy + 0.2, pz, 1, 0.1, 0.1, 0.1, 0.0);
            } else {
                world.spawnParticles(ParticleTypes.EXPLOSION, px, cy, pz, 1, 0.0, 0.0, 0.0, 0.0);
                world.spawnParticles(ParticleTypes.LARGE_SMOKE, px, cy + 0.1, pz, 1, 0.1, 0.05, 0.1, 0.02);
            }
        }
        if (wave == 0) {
            world.spawnParticles(healingVariant ? ParticleTypes.GLOW : ParticleTypes.EXPLOSION_EMITTER,
                    cx, cy + 0.1, cz, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void sampleRing(float radius, boolean clientReplace) {
        if (clientReplace) shakingBlocks.clear();
        BlockPos center = this.getBlockPos();
        World world = this.getWorld();
        int r = (int) Math.ceil(radius);
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                double dx = x, dz = z;
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > radius || dist < radius - 1.5) continue;
                if (!inCone(dx, dz)) continue;

                boolean addedGround = false;
                for (int dy = 4; dy >= -4; dy--) {
                    BlockPos p = center.add(x, dy, z);
                    BlockState s = world.getBlockState(p);
                    if (!s.isAir() && s.isSolidBlock(world, p) && world.getBlockState(p.up()).isAir()) {
                        float height = 0.15f + this.getRandom().nextFloat() * 0.35f;
                        float phase = this.getRandom().nextFloat() * (float) Math.PI * 2f;
                        float speed = 0.1f + this.getRandom().nextFloat() * 0.2f;

                        shakingBlocks.add(new ShakingBlock(p, s, height, phase, speed, this.age, 0f, 0f));
                        addedGround = true;
                        break;
                    }
                }
                if (addedGround) continue;

                for (int dy = 3; dy >= -1; dy--) {
                    BlockPos p = center.add(x, dy, z);
                    BlockState s = world.getBlockState(p);
                    if (s.isAir() || !s.isSolidBlock(world, p)) continue;

                    double len = dist + 0.0001;
                    int ix = (int) Math.round(-dx / len);
                    int iz = (int) Math.round(-dz / len);
                    BlockPos inward = p.add(ix, 0, iz);
                    if (!world.getBlockState(inward).isAir()) continue;
                    float height = 0.15f + this.getRandom().nextFloat() * 0.25f;
                    float phase = this.getRandom().nextFloat() * (float) Math.PI * 2f;
                    float speed = 0.1f + this.getRandom().nextFloat() * 0.2f;

                    float outX = (float) (dx / len);
                    float outZ = (float) (dz / len);
                    shakingBlocks.add(new ShakingBlock(p, s, height, phase, speed, this.age, outX, outZ));
                    break;
                }
            }
        }
        if (world instanceof ServerWorld sw) {
            sw.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.DIRT.getDefaultState()),
                    this.getX(), this.getY() + 0.1, this.getZ(), 30, radius * 0.4, 0.2, radius * 0.4, 0.05);
        }
    }

    private PlayerEntity owner() {
        if (cachedOwner != null && cachedOwner.isAlive()) return cachedOwner;
        if (ownerUuid != null && this.getWorld() instanceof ServerWorld sw) {
            Entity e = sw.getEntity(ownerUuid);
            if (e instanceof PlayerEntity p) { cachedOwner = p; return p; }
        }
        return null;
    }

    public List<ShakingBlock> getShakingBlocks() {
        return shakingBlocks;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.timeToLive = nbt.getInt("ttl");
        if (nbt.containsUuid("owner")) this.ownerUuid = nbt.getUuid("owner");
        if (nbt.contains("radii")) {
            int[] packed = nbt.getIntArray("radii");
            this.waveRadii = new float[packed.length];
            for (int i = 0; i < packed.length; i++) this.waveRadii[i] = Float.intBitsToFloat(packed[i]);
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("ttl", this.timeToLive);
        if (ownerUuid != null) nbt.putUuid("owner", ownerUuid);
        int[] packed = new int[waveRadii.length];
        for (int i = 0; i < waveRadii.length; i++) packed[i] = Float.floatToRawIntBits(waveRadii[i]);
        nbt.putIntArray("radii", packed);
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 96 * 96;
    }

    public record ShakingBlock(BlockPos pos, BlockState state, float height, float phase, float speed, int spawnAge,
                               float outDirX, float outDirZ) {
        public boolean isWall() { return outDirX != 0f || outDirZ != 0f; }
    }
}
