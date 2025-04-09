package com.neocinema.fabric.mixins.client.render;

import com.neocinema.fabric.cef.CefUtil;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "render", at = @At("HEAD"))
    public void render(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (CefUtil.isInit()) {
            CefUtil.getCefApp().N_DoMessageLoopWork();
        }
    }
}