package me.lumiafk.chattweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.lumiafk.chattweaks.UtilKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@ModifyArg(method = "renderChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/DrawContext;IIIZ)V"), index = 4)
	private boolean renderChat(boolean original) {
		return UtilKt.isChatFocused(original);
	}

	@ModifyExpressionValue(method = "renderChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatFocused()Z"))
	private boolean renderChatFocused(boolean original) {
		return MinecraftClient.getInstance().currentScreen instanceof ChatScreen;
	}
}
