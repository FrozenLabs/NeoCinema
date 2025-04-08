package com.neocinema.fabric.block.render;

import com.neocinema.fabric.NeoCinemaClient;
import com.neocinema.fabric.block.ScreenBlockEntity;
import com.neocinema.fabric.screen.Screen;
import com.neocinema.fabric.screen.ScreenManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class ScreenBlockEntityRenderer implements BlockEntityRenderer<ScreenBlockEntity> {

    public ScreenBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(ScreenBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ScreenManager screenManager = NeoCinemaClient.getInstance().getScreenManager();
        Screen screen = screenManager.getScreen(entity.getPos());
        if (screen == null || !screen.isVisible()) return;
        RenderSystem.enableDepthTest();
        Tessellator tessellator = Tessellator.getInstance();
        renderScreenTexture(screen, matrices, tessellator);
        RenderSystem.disableDepthTest();
    }

    private static void renderScreenTexture(Screen screen, MatrixStack matrices, Tessellator tessellator) {
        matrices.push();
        matrices.translate(1, 1, 0);
        RenderUtil.moveForward(matrices, screen.getFacing(), 0.008f);
        RenderUtil.fixRotation(matrices, screen.getFacing());
        matrices.scale(screen.getWidth(), screen.getHeight(), 0);
        if (screen.hasBrowser()) {
            int glId = screen.getBrowser().renderer.getTextureID();
            RenderUtil.renderTexture(matrices, tessellator, glId);
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
