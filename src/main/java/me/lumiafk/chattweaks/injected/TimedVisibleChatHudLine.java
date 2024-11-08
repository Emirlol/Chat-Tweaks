package me.lumiafk.chattweaks.injected;

import java.time.Instant;

public interface TimedVisibleChatHudLine {
	default Instant getAddedTime() {
		return null;
	}
}