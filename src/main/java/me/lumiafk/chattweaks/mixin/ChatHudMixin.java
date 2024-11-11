package me.lumiafk.chattweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.lumiafk.chattweaks.OriginedOrderedText;
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
import org.spongepowered.asm.mixin.Unique;
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

	@Unique
	private ChatHudLine.Visible lastMessage = null;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"))
	private void chatTweaks$renderTimeStamps(
			DrawContext drawContext,
			int a,
			int b,
			int c,
			boolean bl,
			CallbackInfo ci,
			@Local(argsOnly = true, index = 2, ordinal = 1) int mouseX,
			@Local(argsOnly = true, index = 3, ordinal = 2) int mouseY,
			@Local(index = 10, ordinal = 5) int n,
			@Local(index = 12, ordinal = 7) int p,
			@Local(index = 6, ordinal = 3) int l,
			@Local(index = 20, ordinal = 9) int r,
			@Local(index = 23, ordinal = 12) int u,
			@Local ChatHudLine.Visible visible) {
		if (ConfigHandler.INSTANCE.getConfig().hudConfig.hideMessageIndicator) drawContext.getMatrices().translate(-4, 0f, 0f);
		drawContext.fill(-4, p - (r * u), n + 8, p - (r * (u + 1)), ConfigHandler.INSTANCE.getConfig().hudConfig.backgroundColor.getRGB());
		if (ConfigHandler.INSTANCE.getConfig().hudConfig.drawAlternatingRow && visible.isHighlighted()) {
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
	private void chatTweaks$wrapRender(DrawContext instance, int i, int j, int k, int l, int m, Operation<Void> original) {}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;indicator()Lnet/minecraft/client/gui/hud/MessageIndicator;"))
	private MessageIndicator chatTweaks$wrapRender(ChatHudLine.Visible instance, Operation<MessageIndicator> original) {
		return ConfigHandler.INSTANCE.getConfig().hudConfig.hideMessageIndicator ? null : original.call(instance);
	}

	@ModifyExpressionValue(method = "addVisibleMessage", at = @At(value = "NEW", target = "(ILnet/minecraft/text/OrderedText;Lnet/minecraft/client/gui/hud/MessageIndicator;Z)Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;"))
	private ChatHudLine.Visible chatTweaks$wrapAddVisibleMessage(ChatHudLine.Visible original) {
		if (lastMessage != null && lastMessage.comp_896() instanceof OriginedOrderedText lastOrderedText && original.comp_896() instanceof OriginedOrderedText originalOrderedText) {
			if (lastOrderedText.getOriginHashCode() == originalOrderedText.getOriginHashCode()) original.setHighlighted(lastMessage.isHighlighted());
			else original.setHighlighted(!lastMessage.isHighlighted());
		}

		lastMessage = original;
		return original;
	}
}
