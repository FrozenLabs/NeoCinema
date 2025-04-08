package com.neocinema.fabric.mixins;

import com.neocinema.fabric.NeoCinemaClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ItemDropKeyMixin {

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    public void dropSelectedItem(boolean dropEntireStack, CallbackInfoReturnable cbi) {
        if (NeoCinemaClient.getInstance().getScreenManager().hasActiveScreen()) {
            cbi.cancel();
        }
    }

}
