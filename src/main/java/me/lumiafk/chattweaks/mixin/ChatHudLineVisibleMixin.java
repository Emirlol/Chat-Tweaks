package me.lumiafk.chattweaks.mixin;

import me.lumiafk.chattweaks.injected.CustomVisibleChatHudLine;
import me.lumiafk.chattweaks.injected.TimedChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.time.Instant;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ChatHudLine.Visible.class)
public class ChatHudLineVisibleMixin implements TimedChatHudLine, CustomVisibleChatHudLine {
	@Unique
	private Instant addedTime;
	@Unique
	private boolean shouldShowTime;
	@Unique
	private boolean highlighted;

	@Override
	public void setShouldShowTime(boolean showTime) {
		this.shouldShowTime = showTime;
	}

	@Override
	public boolean shouldShowTime() {
		return shouldShowTime;
	}

	@Override
	public void setAddedTime(Instant addedTime) {
		this.addedTime = addedTime;
	}

	@Override
	public Instant getAddedTime() {
		return addedTime;
	}

	@Override
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	@Override
	public boolean isHighlighted() {
		return highlighted;
	}
}
