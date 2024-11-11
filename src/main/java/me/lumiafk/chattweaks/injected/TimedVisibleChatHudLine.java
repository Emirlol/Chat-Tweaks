package me.lumiafk.chattweaks.injected;

import java.time.Instant;

public interface TimedVisibleChatHudLine {
	default Instant getAddedTime() {
		return null;
	}

	default void setShouldShowTime(boolean showTime) {}

	default boolean shouldShowTime() {
		return false;
	}

	default void setHighlighted(boolean highlighted) {
	}

	default boolean isHighlighted() {
		return false;
	}
}