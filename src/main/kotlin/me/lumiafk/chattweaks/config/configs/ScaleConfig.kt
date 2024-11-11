package me.lumiafk.chattweaks.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry

class ScaleConfig {
	@SerialEntry
	@JvmField
	var overrideHeight = false

	@SerialEntry
	@JvmField
	var focusedHeight = 180

	@SerialEntry
	@JvmField
	var unfocusedHeight = 90

	@SerialEntry
	@JvmField
	var overrideWidth = false

	@SerialEntry
	@JvmField
	var width = 320
}