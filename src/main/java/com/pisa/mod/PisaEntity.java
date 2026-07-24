package com.pisa.mod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PisaEntity extends Entity {
    private static final TrackedData<Boolean> FLYING_MODE = DataTracker.registerData(PisaEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public PisaEntity(EntityType<? extends PisaEntity> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(FLYING_MODE, false);
    }

    public boolean isFlyingMode() {
        return this.dataTracker.get(FLYING_MODE);
    }

    public void toggleMode() {
        boolean newMode = !isFlyingMode();
        this.dataTracker.set(FLYING_MODE, newMode);
        if (getControllingPassenger() instanceof PlayerEntity player) {
            player.sendMessage(Text.literal("§eРежим: " + (newMode ? "§b[ПОЛЕТ (Скорость Элитры)]" : "§a[ЕЗДА]")), true);
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient) {
            player.startRiding(this);
            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    // Обязательный метод для Entity в Minecraft 1.21.4
    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity passenger = getControllingPassenger();
        if (passenger != null) {
            this.setYaw(passenger.getYaw());
            this.prevYaw = this.getYaw();
            this.setPitch(passenger.getPitch());

            if (isFlyingMode()) {
                Vec3d lookVec = passenger.getRotationVector();
                double elytraSpeed = 1.35;
                this.setVelocity(lookVec.multiply(elytraSpeed));
                this.noClip = true;
            } else {
                this.noClip = false;
                Vec3d vel = this.getVelocity();

                if (!this.isOnGround()) {
                    vel = vel.add(0, -0.08, 0);
                }

                if (passenger instanceof PlayerEntity player) {
                    if (player.sidewaysSpeed != 0 || player.forwardSpeed != 0) {
                        double forwardMultiplier = player.forwardSpeed > 0 ? 0.7 : -0.3;
                        Vec3d moveVec = Vec3d.fromPolar(0, passenger.getYaw()).multiply(forwardMultiplier);
                        vel = new Vec3d(moveVec.x, vel.y, moveVec.z);
                    }
                }
                this.setVelocity(vel);
            }

            this.move(MovementType.SELF, this.getVelocity());
        } else {
            this.noClip = false;
            if (!this.isOnGround()) {
                this.setVelocity(this.getVelocity().add(0, -0.08, 0));
                this.move(MovementType.SELF, this.getVelocity());
            }
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.dataTracker.set(FLYING_MODE, nbt.getBoolean("FlyingMode"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean("FlyingMode", isFlyingMode());
    }
}
