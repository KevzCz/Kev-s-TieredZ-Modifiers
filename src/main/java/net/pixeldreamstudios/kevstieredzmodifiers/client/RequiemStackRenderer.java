package net.pixeldreamstudios.kevstieredzmodifiers.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** Draws a small orbiting stack-count number over each Requiem-stacked entity (healer-only). */
@Environment(EnvType.CLIENT)
public final class RequiemStackRenderer {

    private RequiemStackRenderer() {
    }

    public static void render(WorldRenderContext ctx) {
        if (ClientRequiemStacks.isEmpty()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Vec3d cam = ctx.camera().getPos();
        MatrixStack matrices = new MatrixStack();
        VertexConsumerProvider.Immediate vc = mc.getBufferBuilders().getEntityVertexConsumers();
        TextRenderer tr = mc.textRenderer;
        float time = (mc.world.getTime() + ctx.tickCounter().getTickDelta(false)) * 0.08f;

        for (var e : ClientRequiemStacks.all().entrySet()) {
            Entity target = mc.world.getEntityById(e.getKey());
            if (target == null) continue;
            int stacks = e.getValue();
            if (stacks <= 0) continue;

            double orbit = 0.45;
            double ox = Math.cos(time) * orbit;
            double oz = Math.sin(time) * orbit;
            double x = target.getX() + ox - cam.x;
            double y = target.getY() + target.getHeight() + 0.4 - cam.y;
            double z = target.getZ() + oz - cam.z;

            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
            matrices.scale(-0.02f, -0.02f, 0.02f);
            Matrix4f m = matrices.peek().getPositionMatrix();
            String s = String.valueOf(stacks);
            float w = -tr.getWidth(s) / 2f;
            tr.draw(s, w, 0, 0xFFF1C40F, false, m, vc,
                    TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            matrices.pop();
        }
        vc.draw();
    }
}
