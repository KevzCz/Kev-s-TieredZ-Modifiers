package net.pixeldreamstudios.kevstieredzmodifiers.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pixeldreamstudios.kevstieredzmodifiers.entity.AftershockEntity;

@Environment(EnvType.CLIENT)
public class AftershockEntityRenderer extends EntityRenderer<AftershockEntity> {

    private static final float CYCLE_TICKS = 55f;

    private static final int OVERLAY = OverlayTexture.DEFAULT_UV;

    private final BlockRenderManager blockRenderManager;

    public AftershockEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.blockRenderManager = ctx.getBlockRenderManager();
    }

    @Override
    public Identifier getTexture(AftershockEntity entity) {
        return null;
    }

    @Override
    public void render(AftershockEntity entity, float yaw, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light) {
        var blocks = entity.getShakingBlocks();
        if (blocks.isEmpty()) return;
        Vec3d pos = entity.getPos();
        float time = entity.age + tickDelta;
        World world = entity.getWorld();

        for (AftershockEntity.ShakingBlock b : blocks) {
            BlockState state = b.state();
            if (state.getRenderType() != BlockRenderType.MODEL) continue;
            BlockPos bp = b.pos();

            float localT = (time - b.spawnAge()) / CYCLE_TICKS;
            if (localT < 0f) continue;

            if (localT >= 1f) continue;

            float lift;
            final float RISE = 0.12f;
            final float HOLD = 0.55f;
            if (localT < RISE) {
                float u = localT / RISE;
                lift = u * u;
            } else if (localT < HOLD) {
                lift = 1f;
            } else {
                float u = (localT - HOLD) / (1f - HOLD);
                lift = 1f - u * u;
            }

            float sink = localT > 0.9f ? -(localT - 0.9f) / 0.1f * 0.5f : 0f;

            matrices.push();

            matrices.translate(bp.getX() - pos.x + 0.5, bp.getY() - pos.y + 0.5, bp.getZ() - pos.z + 0.5);

            float held = Math.max(lift, localT >= RISE ? 0.6f : lift);

            if (b.isWall()) {

                float slide = lift * b.height() * 1.3f;
                matrices.translate(b.outDirX() * slide, (lift + sink) * b.height() * 0.3f, b.outDirZ() * slide);
                float tip = held * (22f + b.height() * 26f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(tip * b.outDirZ()));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-tip * b.outDirX()));
            } else {

                float up = lift * b.height() + sink;
                matrices.translate(0, up, 0);
                float tiltX = held * (14f + b.height() * 34f) * (float) Math.sin(b.phase());
                float tiltZ = held * (14f + b.height() * 34f) * (float) Math.cos(b.phase() * 1.3f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(tiltX));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(tiltZ));
            }

            matrices.translate(-0.5, -0.5, -0.5);

            int worldLight = WorldRenderer.getLightmapCoordinates(world, bp.up());
            blockRenderManager.renderBlockAsEntity(state, matrices, vertexConsumers, worldLight, OVERLAY);
            matrices.pop();
        }
    }

    @Override
    public boolean shouldRender(AftershockEntity entity, Frustum frustum,
            double x, double y, double z) {

        return frustum.isVisible(entity.getBoundingBox().expand(12.0));
    }
}
