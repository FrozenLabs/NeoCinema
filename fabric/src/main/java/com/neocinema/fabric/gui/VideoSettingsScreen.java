package com.neocinema.fabric.gui;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.NeoCinemaClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.ButtonWidget.Builder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static net.minecraft.client.render.RenderPhase.*;

public class VideoSettingsScreen extends Screen {

    protected static final Identifier TEXTURE = Identifier.of(NeoCinema.MODID, "textures/gui/menu-transparent.png");
    private boolean shouldReloadScreen;

    public VideoSettingsScreen() {
        super(Text.translatable("gui.neocinema.video-settings.title"));
    }

    private static CheckboxWidget checkboxWidget(int x, int y, Text text, boolean checked, CheckboxWidget.Callback callback) {
        CheckboxWidget widget = CheckboxWidget.builder(text, MinecraftClient.getInstance().textRenderer)
                .pos(x, y)
                .checked(checked)
                .callback(callback)
                .build();
        widget.setWidth(196);
        widget.setHeight(20);
        return widget;
    }

    @Override
    protected void init() {
        addDrawableChild(new SliderWidget(method_31362() + 23, 78, 196, 20, Text.translatable("gui.neocinema.video-settings.volume"),
                NeoCinemaClient.getInstance().getVideoSettings().getVolume()) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                for (com.neocinema.fabric.screen.Screen screen : NeoCinemaClient.getInstance().getScreenManager().getScreens())
                    screen.setVideoVolume((float) value);
                NeoCinemaClient.getInstance().getVideoSettings().setVolume((float) value);
            }
        });
        addDrawableChild(checkboxWidget(method_31362() + 23, 110, Text.translatable("gui.neocinema.video-settings.mute"),
                NeoCinemaClient.getInstance().getVideoSettings().isMuteWhenAltTabbed(),
                (checkbox, checked) -> NeoCinemaClient.getInstance().getVideoSettings().setMuteWhenAltTabbed(checked)
        ));
        addDrawableChild(checkboxWidget(method_31362() + 23, 142, Text.translatable("gui.neocinema.video-settings.cross-hair"),
                NeoCinemaClient.getInstance().getVideoSettings().isHideCrosshair(),
                (checkbox, checked) -> NeoCinemaClient.getInstance().getVideoSettings().setHideCrosshair(checked)
        ));

        ButtonWidget screenResolutionBuilder = createScreenResolutionDrawable();
        ButtonWidget browserRefreshRateBuilder = createBrowserRefreshRateDrawable();

        addDrawableChild(screenResolutionBuilder);
        addDrawableChild(browserRefreshRateBuilder);
    }

    private @NotNull ButtonWidget createBrowserRefreshRateDrawable() {
        Builder browserRefreshRateBuilder = new Builder(
                Text.translatable("gui.neocinema.video-settings.refresh-rate", NeoCinemaClient.getInstance().getVideoSettings().getBrowserRefreshRate(), "fps"),
                button ->
                {
                    NeoCinemaClient.getInstance().getVideoSettings().setNextBrowserRefreshRate();
                    button.setMessage(Text.translatable("gui.neocinema.video-settings.refresh-rate", NeoCinemaClient.getInstance().getVideoSettings().getBrowserRefreshRate(), "fps"));
                    shouldReloadScreen = true;
                });
        browserRefreshRateBuilder.dimensions(method_31362() + 23, 142 + 32 + 32, 196, 20);
        return browserRefreshRateBuilder.build();
    }

    private @NotNull ButtonWidget createScreenResolutionDrawable() {
        Builder screenResolutionBuilder = new Builder(
            Text.translatable("gui.neocinema.video-settings.resolution", NeoCinemaClient.getInstance().getVideoSettings().getBrowserResolution(), "p"),
             button ->
        {
            NeoCinemaClient.getInstance().getVideoSettings().setNextBrowserResolution();
            button.setMessage(Text.translatable("gui.neocinema.video-settings.resolution", NeoCinemaClient.getInstance().getVideoSettings().getBrowserResolution(), "p"));
            shouldReloadScreen = true;
        });
        screenResolutionBuilder.dimensions(method_31362() + 23, 142 + 32, 196, 20);
        return screenResolutionBuilder.build();
    }

    private int method_31359() {
        return Math.max(52, this.height - 128 - 16);
    }

    private int method_31360() {
        return this.method_31359() / 16;
    }

    private int method_31362() {
        return (this.width - 238) / 2;
    }

    public void renderBackground(DrawContext context) {
        //Create a Function<Identifier, RenderLayer> GUI_TEXTURED from RenderLayer class
        Function<Identifier, RenderLayer> GUI_TEXTURED = Util.memoize((texture) -> RenderLayer.of("gui_textured_overlay", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS, 1536, RenderLayer.MultiPhaseParameters.builder().texture(new Texture(texture, TriState.DEFAULT, false)).program(POSITION_TEXTURE_COLOR_PROGRAM).transparency(TRANSLUCENT_TRANSPARENCY).depthTest(ALWAYS_DEPTH_TEST).writeMaskState(COLOR_MASK).build(false)));
        int i = this.method_31362() + 3;
        context.drawTexture(GUI_TEXTURED,TEXTURE, i, 64, 1, 1, 236, 8, 256,256);
        int j = this.method_31360();
        for (int k = 0; k < j; ++k)
            context.drawTexture(GUI_TEXTURED,TEXTURE, i, 72 + 16 * k, 1, 10, 236, 16, 256,256);
        context.drawTexture(GUI_TEXTURED,TEXTURE, i, 72 + 16 * j, 1, 27, 236, 8, 256,256);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.renderBackground(context);
        if (this.client != null)
            context.drawCenteredTextWithShadow(this.client.textRenderer, Text.translatable("gui.neocinema.video-settings.title"), this.width / 2, 64 - 10, -1);
    }

    @Override
    public void close() {
        super.close();
        NeoCinemaClient.getInstance().getVideoSettings().saveAsync();
        if (shouldReloadScreen) {
            for (com.neocinema.fabric.screen.Screen screen : NeoCinemaClient.getInstance().getScreenManager().getScreens()) {
                if (screen.hasBrowser()) {
                    screen.reload();
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.client != null && client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

}
