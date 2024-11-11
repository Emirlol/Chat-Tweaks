package me.lumiafk.chattweaks.injected;

public interface CustomVisibleChatHudLine {
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
