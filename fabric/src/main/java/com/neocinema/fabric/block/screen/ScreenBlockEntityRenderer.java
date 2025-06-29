package com.neocinema.fabric.block.screen;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.neocinema.fabric.NeoCinemaClient;
import com.neocinema.fabric.screen.Screen;
import com.neocinema.fabric.screen.ScreenManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.neocinema.fabric.util.RenderUtil;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class ScreenBlockEntityRenderer implements BlockEntityRenderer<ScreenBlockEntity> {

    public ScreenBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(ScreenBlockEntity entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        ScreenManager screenManager = NeoCinemaClient.getInstance().getScreenManager();
        Screen screen = screenManager.getScreen(entity.getPos());
        if (screen == null || !screen.isVisible()) return;

        GlStateManager._enableDepthTest();
        Tessellator tessellator = Tessellator.getInstance();
        renderScreenTexture(screen, matrices, tessellator);
        GlStateManager._disableDepthTest();
    }

    private static void renderScreenTexture(Screen screen, MatrixStack matrices, Tessellator tessellator) {
        matrices.push();
        matrices.translate(1, 1, 0);
        RenderUtil.moveForward(matrices, screen.getFacing(), 0.008f);
        RenderUtil.fixRotation(matrices, screen.getFacing());
        matrices.scale(screen.getWidth(), screen.getHeight(), 0);
        if (screen.hasPlayer()) {
            screen.getPlayer().sync();
            RenderUtil.renderTexture(matrices, tessellator, screen.getPlayer().texture());
        } else {
            RenderUtil.renderBlack(matrices, tessellator);
        }
        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(ScreenBlockEntity blockEntity) {
        return true;
    }

    public static void register() {
        BlockEntityRendererFactories.register(ScreenBlockEntity.SCREEN_BLOCK_ENTITY, ScreenBlockEntityRenderer::new);
    }
}
