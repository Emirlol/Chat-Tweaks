package me.lumiafk.chattweaks.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {
	protected ChatScreenMixin(Text text) {
		super(text);
	}

	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;setMaxLength(I)V"))
	private int modifyChatHeight(int height) {
		return 10000;
	}
}
