package me.lumiafk.chattweaks.mixin;

import me.lumiafk.chattweaks.config.ConfigHandler;
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
}
