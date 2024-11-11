package me.lumiafk.chattweaks.injected;

import java.time.Instant;

public interface TimedChatHudLine {
	default Instant getAddedTime() {
		return null;
	}

	default void setAddedTime(Instant addedTime) {}
}