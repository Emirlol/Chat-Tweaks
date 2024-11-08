package me.lumiafk.chattweaks.mixin;

import me.lumiafk.chattweaks.injected.TimedVisibleChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;

@Mixin(ChatHudLine.Visible.class)
public class ChatHudLineVisibleMixin implements TimedVisibleChatHudLine {
	@Unique
	private Instant addedTime;
	@Inject(method = "<init>", at = @At("TAIL"))
	private void chatTweaks$init(CallbackInfo ci) {
		addedTime = Instant.now();
	}

	@SuppressWarnings("AddedMixinMembersNamePattern")
	@Override
	public Instant getAddedTime() {
		return addedTime;
	}
}
