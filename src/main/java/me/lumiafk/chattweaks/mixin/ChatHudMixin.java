package me.lumiafk.chattweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.lumiafk.chattweaks.UtilKt;
import me.lumiafk.chattweaks.config.ConfigHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Mixin(ChatHud.class)
public class ChatHudMixin {
	@Final
	@Shadow
	private List<ChatHudLine.Visible> visibleMessages;

	@Final
	@Shadow
	private MinecraftClient client;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"))
	private void chatTweaks$renderTimeStamps(
			DrawContext drawContext,
			int a,
			int b,
			int c,
			boolean bl,
			CallbackInfo ci,
			@Local(argsOnly = true, ordinal = 1) int mouseX,
			@Local(argsOnly = true, ordinal = 2) int mouseY,
			@Local(name = "n") int n,
			@Local(name = "p") int p,
			@Local(name = "l") int l,
			@Local(name = "r") int r,
			@Local(name = "u") int u,
			@Local(name = "y") int y,
			@Local(name = "v") int v,
			@Local(name = "f") float f,
			@Local ChatHudLine.Visible visible) {
		if (ConfigHandler.INSTANCE.getConfig().hudConfig.hideMessageIndicator) drawContext.getMatrices().translate(-4, 0f, 0f);
		if (ConfigHandler.INSTANCE.getConfig().hudConfig.drawAlternatingRow &&
				v % 2 == 0) {
			drawContext.fill(-4, p - (r * u), n + 8, p - (r * (u + 1)), ConfigHandler.INSTANCE.getConfig().hudConfig.alternatingRowColor.getRGB());
		}

		if (ConfigHandler.INSTANCE.getConfig().timeStampConfig.enabled && (
				ConfigHandler.INSTANCE.getConfig().timeStampConfig.alwaysShow ||
						UtilKt.isPointIn(mouseX, mouseY, 0, p - (Math.min(visibleMessages.size(), l) * r), n + 12 - 4, p)
		)) {
			String formatted = ConfigHandler.INSTANCE.getConfig().timeStampConfig.dateTimeFormatter.format(LocalDateTime.ofInstant(visible.getAddedTime(), ZoneId.systemDefault()));
			int length = client.textRenderer.getWidth(formatted);
			drawContext.fill(n + 10, p - (r * u), n + 10 + length + 3, p - (r * (u + 1)), ConfigHandler.INSTANCE.getConfig().timeStampConfig.backgroundColor.getRGB());
			drawContext.drawText(client.textRenderer, formatted, n + 12, p - (r * u) - 8, ConfigHandler.INSTANCE.getConfig().timeStampConfig.textColor.getRGB(), false);
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 0))
	private void chatTweaks$wrapRender(DrawContext instance, int i, int j, int k, int l, int m, Operation<Void> original) {
		if (ConfigHandler.INSTANCE.getConfig().hudConfig.hideMessageIndicator) {
			i -= 4;
			k -= 4;
		}
		int color = ConfigHandler.INSTANCE.getConfig().hudConfig.backgroundColor.getRGB();
		//If no changes have been made, don't use the custom color as the custom color's alpha might be different from the user's chat background opacity setting in minecraft settings
		if (color == ConfigHandler.INSTANCE.getDefault().hudConfig.backgroundColor.getRGB()) original.call(instance, i, j, k, l, m);
		original.call(instance, i, j, k, l, color);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;indicator()Lnet/minecraft/client/gui/hud/MessageIndicator;"))
	private MessageIndicator chatTweaks$wrapRender(ChatHudLine.Visible instance, Operation<MessageIndicator> original) {
		return ConfigHandler.INSTANCE.getConfig().hudConfig.hideMessageIndicator ? null : original.call(instance);
	}
}
