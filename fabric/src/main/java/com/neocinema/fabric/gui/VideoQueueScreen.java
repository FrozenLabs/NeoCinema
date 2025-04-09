package com.neocinema.fabric.gui;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.NeoCinemaClient;
import com.neocinema.fabric.gui.widget.VideoQueueWidget;
import com.neocinema.fabric.util.NetworkUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.Builder;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.function.Function;

import static net.minecraft.client.render.RenderPhase.*;
import static net.minecraft.client.render.RenderPhase.COLOR_MASK;

public class VideoQueueScreen extends Screen {

    protected static final Identifier TEXTURE = Identifier.of(NeoCinema.MODID, "textures/gui/menu-transparent.png");
    protected static KeyBinding keyBinding;

    public VideoQueueWidget videoQueueWidget;

    public VideoQueueScreen() {
        super(Text.translatable("gui.neocinema.videoqueuetitle"));
    }

    @Override
    protected void init() {
        videoQueueWidget = new VideoQueueWidget(this, client, this.width, this.height, 68, this.method_31361(), 19);
        ButtonWidget.Builder videoSettingsBuilder = new Builder(Text.translatable("gui.neocinema.videosettingstitle"), button -> {
            client.setScreen(new VideoSettingsScreen());
        });

        videoSettingsBuilder.dimensions(method_31362() + 23, method_31359() + 78, 196, 20);

        addDrawableChild(videoSettingsBuilder.build());
    }

    private int method_31359() {
        return Math.max(52, this.height - 128 - 16);
    }

    private int method_31360() {
        return this.method_31359() / 16;
    }

    private int method_31361() {
        return 80 + this.method_31360() * 16 - 8;
    }

    private int method_31362() {
        return (this.width - 238) / 2;
    }

    private static final Function<Identifier, RenderLayer> GUI_TEXTURED = null;

    public void renderBackground(DrawContext context) {
        //Create a Function<Identifier, RenderLayer> GUI_TEXTURED from RenderLayer class
        Function<Identifier, RenderLayer> GUI_TEXTURED = Util.memoize((texture) -> {
            return RenderLayer.of("gui_textured_overlay", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS, 1536, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, TriState.DEFAULT, false)).program(POSITION_TEXTURE_COLOR_PROGRAM).transparency(TRANSLUCENT_TRANSPARENCY).depthTest(ALWAYS_DEPTH_TEST).writeMaskState(COLOR_MASK).build(false));
        });
        int i = this.method_31362() + 3;
        context.drawTexture(GUI_TEXTURED, TEXTURE, i, 64, 1, 1, 236, 8, 256, 256);
        int j = this.method_31360();
        for (int k = 0; k < j; ++k)
            context.drawTexture(GUI_TEXTURED,TEXTURE, i, 72 + 16 * k, 1, 10, 236, 16, 256, 256);
        context.drawTexture(GUI_TEXTURED,TEXTURE, i, 72 + 16 * j, 1, 27, 236, 8, 256, 256);
        context.drawCenteredTextWithShadow(this.client.textRenderer, Text.translatable("gui.neocinema.videoqueueentries", videoQueueWidget.children().size()), this.width / 2, 64 - 10, -1);
        if (videoQueueWidget.children().isEmpty()) {
            context.drawCenteredTextWithShadow(this.client.textRenderer, Text.translatable("gui.neocinema.videoqueuenovideos"), this.width / 2, (56 + this.method_31361()) / 2, -1);
        } else {
            //not found getScrollAmount()
//            if (videoQueueWidget.getScrollAmount() == 0f) {
//                context.drawCenteredTextWithShadow(this.client.textRenderer, Text.translatable("gui.neocinema.videoqueueupnext", " ->"), -158 + this.width / 2, 64 + 12, -1);
//            }
            context.drawCenteredTextWithShadow(this.client.textRenderer, Text.translatable("gui.neocinema.videoqueueupnext", " ->"), -158 + this.width / 2, 64 + 12, -1);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.renderBackground(context);
        videoQueueWidget.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        super.close();
        NetworkUtil.sendShowVideoTimelinePacket();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button) || videoQueueWidget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyBinding.matchesKey(keyCode, scanCode)) {
            close();
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double verticalAmount) {
        videoQueueWidget.mouseScrolled(mouseX, mouseY, amount, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, amount, verticalAmount);
    }

    public static void registerKeyInput() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neocinema.openvideoqueue",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.neocinema.keybinds"
        ));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!NeoCinemaClient.getInstance().getScreenManager().hasActiveScreen()) return;

            if (keyBinding.wasPressed()) {
                client.setScreen(new VideoQueueScreen());
            }
        });
    }

}
