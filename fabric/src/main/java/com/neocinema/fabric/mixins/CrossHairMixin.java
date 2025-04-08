package com.neocinema.fabric.mixins;

import com.neocinema.fabric.NeoCinemaClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class CrossHairMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    public void renderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (NeoCinemaClient.getInstance().getScreenManager().hasActiveScreen()
                && NeoCinemaClient.getInstance().getVideoSettings().isHideCrosshair()) {
            ci.cancel();
        }
    }

}
