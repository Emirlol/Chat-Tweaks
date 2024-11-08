package me.lumiafk.chattweaks.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry
import java.awt.Color

class HudConfig {
	@SerialEntry
	@JvmField
	var drawAlternatingRow = true

	@SerialEntry
	@JvmField
	var alternatingRowColor = Color(255, 255, 255, 12)

	@SerialEntry
	@JvmField
	var backgroundColor = Color(0, 0, 0, 127)

	@SerialEntry
	@JvmField
	var hideMessageIndicator = true
}