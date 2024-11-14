package me.lumiafk.chattweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.lumiafk.chattweaks.config.ConfigHandler;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;setMaxLength(I)V"))
	private int chatTweaks$modifyChatWidth(int i) {
		return ConfigHandler.INSTANCE.getConfig().otherConfig.chatWidth;
	}

	//This is supposed to wrap operations for both pgup and pgdown keys
	@WrapOperation(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;scroll(I)V"))
	private void chatTweaks$wrapKeyPressed(ChatHud instance, int scroll, Operation<Void> original) {
		var chatBoxes = ConfigHandler.INSTANCE.getConfig().otherConfig.chatBoxes;
		for (var chatBox : chatBoxes) {
			chatBox.scroll(scroll < 0
			               ? chatBox.getVisibleLineCount(true) - 1
			               : -chatBox.getVisibleLineCount(true) + 1);
		}
	}

	@WrapOperation(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;scroll(I)V"))
	private void chatTweaks$wrapScroll(ChatHud instance, int scroll, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) double mouseX, @Local(argsOnly = true, ordinal = 1) double mouseY) {
		var chatBoxes = ConfigHandler.INSTANCE.getConfig().otherConfig.chatBoxes;
		for (var chatBox : chatBoxes) {
			if (chatBox.isMouseOver(mouseX, mouseY)) {
				chatBox.scroll(-scroll);
				return;
			}
		}
	}
}
