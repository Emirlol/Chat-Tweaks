package me.lumiafk.chattweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.lumiafk.chattweaks.chat.ChatBox;
import me.lumiafk.chattweaks.config.ConfigHandler;
import me.lumiafk.chattweaks.util.ChatHudUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.util.profiler.Profilers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
	@Final
	@Shadow
	private MinecraftClient client;

	@Shadow
	public abstract boolean isChatFocused();

	@Unique
	public boolean previousFocused = false;

	@Shadow
	@Final
	private List<ChatHudLine.Visible> visibleMessages;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatHidden()Z", shift = At.Shift.AFTER), cancellable = true)
	private void chatTweaks$render(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
		var profiler = Profilers.get();
		profiler.push("chat");
		var chatBoxes = ConfigHandler.INSTANCE.getConfig().otherConfig.chatBoxes;
		var tickDelta = client.getRenderTickCounter().getTickDelta(true);
		for (var chatBox : chatBoxes) {
			if (previousFocused != focused) chatBox.onFocusChange(focused);
			chatBox.render(context, tickDelta, currentTick, mouseX, mouseY, focused);
		}
		profiler.pop();
		previousFocused = focused;
		ci.cancel();
	}

	@WrapMethod(method = "addVisibleMessage")
	private void chatTweaks$addVisibleMessage(ChatHudLine message, Operation<Void> original) {
		var chatBoxes = ConfigHandler.INSTANCE.getConfig().otherConfig.chatBoxes;
		var isChatFocused = isChatFocused();
		for (var chatBox : chatBoxes) {
			chatBox.addVisibleMessage(message, isChatFocused);
		}
	}

	@Inject(method = "refresh", at = @At("HEAD"))
	private void chatTweaks$clearVisibleMessages(CallbackInfo ci) {
		var chatBoxes = ConfigHandler.INSTANCE.getConfig().otherConfig.chatBoxes;
		for (ChatBox chatBox : chatBoxes) {
			chatBox.getVisibleMessages().clear();
		}
	}

	@WrapMethod(method = "isChatFocused")
	private boolean chatTweaks$wrapIsChatFocused(Operation<Boolean> original) {
		return ChatHudUtil.INSTANCE.isChatFocused(false) || original.call();
	}
}
