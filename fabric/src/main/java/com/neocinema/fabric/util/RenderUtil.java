package com.neocinema.fabric.util;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.util.math.MatrixStack;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class RenderUtil {

    public static final RenderPipeline POSITION_TEX_COLOR_NEOCINEMA_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation(Identifier.of("neocinema", "pipeline/position_tex_color"))
                    .withVertexShader("core/position_tex_color")
                    .withFragmentShader("core/position_tex_color")
                    .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                    .withSampler("Sampler0")
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .withCull(true)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .build()
    );

    public static final RenderPipeline POSITION_COLOR_NEOCINEMA_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation(Identifier.of("neocinema", "pipeline/position_color"))
                    .withVertexShader("core/position_tex_color")
                    .withFragmentShader("core/position_tex_color")
                    .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .withCull(true)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .build()
    );

    public static void fixRotation(MatrixStack matrixStack, String facing) {
        final Quaternionf rotation;

        switch (facing) {
            case "NORTH":
                rotation = new Quaternionf().rotationY((float) Math.toRadians(180));
                matrixStack.translate(0, 0, 1);
                break;
            case "WEST":
                rotation = new Quaternionf().rotationY((float) Math.toRadians(-90.0));
                matrixStack.translate(0, 0, 0);
                break;
            case "EAST":
                rotation = new Quaternionf().rotationY((float) Math.toRadians(90.0));
                matrixStack.translate(-1, 0, 1);
                break;
            default: // SOUTH or unrecognized
                rotation = new Quaternionf();
                matrixStack.translate(-1, 0, 0);
                break;
        }
        matrixStack.multiply(rotation);
    }

    public static void moveForward(MatrixStack matrixStack, String facing, float amount) {
        switch (facing) {
            case "NORTH":
                matrixStack.translate(0, 0, -amount);
                break;
            case "WEST":
                matrixStack.translate(-amount, 0, 0);
                break;
            case "EAST":
                matrixStack.translate(amount, 0, 0);
                break;
            default: // SOUTH
                matrixStack.translate(0, 0, amount);
                break;
        }
    }

    public static void moveHorizontal(MatrixStack matrixStack, String facing, float amount) {
        switch (facing) {
            case "NORTH":
                matrixStack.translate(-amount, 0, 0);
                break;
            case "WEST":
                matrixStack.translate(0, 0, amount);
                break;
            case "EAST":
                matrixStack.translate(0, 0, -amount);
                break;
            default: // SOUTH
                matrixStack.translate(amount, 0, 0);
                break;
        }
    }

    public static void moveVertical(MatrixStack matrixStack, float amount) {
        matrixStack.translate(0, amount, 0);
    }

    /**
     * Renders a textured quad.
     *
     * @param matrixStack The current MatrixStack.
     * @param tessellator The Tessellator instance.
     * @param abstractTexture The texture to render.
     */
    public static void renderTexture(MatrixStack matrixStack, Tessellator tessellator, AbstractTexture abstractTexture) {
        GpuDevice device = RenderSystem.getDevice();
        CommandEncoder encoder = device.createCommandEncoder();

        GpuTexture texture = abstractTexture.getGlTexture();

        GpuTexture mainColorTexture = MinecraftClient.getInstance().getFramebuffer().getColorAttachment();
        GpuTexture mainDepthTexture = MinecraftClient.getInstance().getFramebuffer().getDepthAttachment();

        try (RenderPass pass = encoder.createRenderPass(mainColorTexture, OptionalInt.empty(), mainDepthTexture, OptionalDouble.empty())) {
            pass.setPipeline(POSITION_TEX_COLOR_NEOCINEMA_PIPELINE);

            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
            BufferBuilder builder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            builder.vertex(matrix4f, 0.0F, -1.0F, 1.0F).color(255, 255, 255, 255).texture(0.0f, 1.0f);
            builder.vertex(matrix4f, 1.0F, -1.0F, 1.0F).color(255, 255, 255, 255).texture(1.0f, 1.0f);
            builder.vertex(matrix4f, 1.0F, 0.0F, 0.0F).color(255, 255, 255, 255).texture(1.0f, 0.0f);
            builder.vertex(matrix4f, 0, 0, 0).color(255, 255, 255, 255).texture(0.0f, 0.0f);

            BuiltBuffer buffer = builder.end();

            GpuBuffer vertexBuffer = POSITION_TEX_COLOR_NEOCINEMA_PIPELINE.getVertexFormat().uploadImmediateVertexBuffer(buffer.getBuffer());
            GpuBuffer indexBuffer;
            VertexFormat.IndexType indexType;
            if (buffer.getSortedBuffer() == null) {
                RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(buffer.getDrawParameters().mode());
                indexBuffer = shapeIndexBuffer.getIndexBuffer(buffer.getDrawParameters().indexCount());
                indexType = shapeIndexBuffer.getIndexType();
            } else {
                indexBuffer = POSITION_TEX_COLOR_NEOCINEMA_PIPELINE.getVertexFormat().uploadImmediateIndexBuffer(buffer.getSortedBuffer());
                indexType = buffer.getDrawParameters().indexType();
            }

            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indexBuffer, indexType);

            pass.bindSampler("Sampler0", texture);

            pass.drawIndexed(0, buffer.getDrawParameters().indexCount());

            vertexBuffer.close();
            indexBuffer.close();
        }
    }

    /**
     * Renders a colored quad.
     *
     * @param matrixStack The current MatrixStack.
     * @param tessellator The Tessellator instance.
     * @param r Red color component (0-255).
     * @param g Green color component (0-255).
     * @param b Blue color component (0-255).
     */
    public static void renderColor(MatrixStack matrixStack, Tessellator tessellator, int r, int g, int b) {
        GpuDevice device = RenderSystem.getDevice();
        CommandEncoder encoder = device.createCommandEncoder();

        GpuTexture mainColorTexture = MinecraftClient.getInstance().getFramebuffer().getColorAttachment();
        GpuTexture mainDepthTexture = MinecraftClient.getInstance().getFramebuffer().getDepthAttachment();

        try (RenderPass pass = encoder.createRenderPass(mainColorTexture, OptionalInt.empty(), mainDepthTexture, OptionalDouble.empty())) {
            pass.setPipeline(POSITION_COLOR_NEOCINEMA_PIPELINE);

            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
            BufferBuilder builder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            builder.vertex(matrix4f, 0.0F, -1.0F, 1.0F).color(r, g, b, 255);
            builder.vertex(matrix4f, 1.0F, -1.0F, 1.0F).color(r, g, b, 255);
            builder.vertex(matrix4f, 1.0F, 0.0F, 0.0F).color(r, g, b, 255);
            builder.vertex(matrix4f, 0, 0, 0).color(r, g, b, 255);

            BuiltBuffer buffer = builder.end();

            GpuBuffer vertexBuffer = POSITION_TEX_COLOR_NEOCINEMA_PIPELINE.getVertexFormat().uploadImmediateVertexBuffer(buffer.getBuffer());
            GpuBuffer indexBuffer;
            VertexFormat.IndexType indexType;
            if (buffer.getSortedBuffer() == null) {
                RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(buffer.getDrawParameters().mode());
                indexBuffer = shapeIndexBuffer.getIndexBuffer(buffer.getDrawParameters().indexCount());
                indexType = shapeIndexBuffer.getIndexType();
            } else {
                indexBuffer = POSITION_TEX_COLOR_NEOCINEMA_PIPELINE.getVertexFormat().uploadImmediateIndexBuffer(buffer.getSortedBuffer());
                indexType = buffer.getDrawParameters().indexType();
            }

            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indexBuffer, indexType);

            pass.drawIndexed(0, buffer.getDrawParameters().indexCount());

            vertexBuffer.close();
            indexBuffer.close();
        }
    }

    public static void renderBlack(MatrixStack matrixStack, Tessellator tessellator) {
        renderColor(matrixStack, tessellator, 0, 0, 0);
    }
}