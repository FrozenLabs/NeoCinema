package com.neocinema.fabric.mixins.client;

import com.neocinema.fabric.NeoCinemaClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(at = @At("HEAD"), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V")
    private void disconnect(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
        NeoCinemaClient.getInstance().getScreenManager().unloadAll();
        NeoCinemaClient.getInstance().getPreviewScreenManager().unloadAll();
        NeoCinemaClient.getInstance().getVideoServiceManager().unregisterAll();
        NeoCinemaClient.getInstance().getVideoListManager().reset();
        NeoCinemaClient.getInstance().getVideoQueue().clear();
    }
}
