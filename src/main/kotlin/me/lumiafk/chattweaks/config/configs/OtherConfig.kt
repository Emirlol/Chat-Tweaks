package me.lumiafk.chattweaks.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry

class OtherConfig {
	@SerialEntry
	@JvmField
	var chatWidth = 1024

	@SerialEntry
	@JvmField
	var chatAlwaysVisible = false
}