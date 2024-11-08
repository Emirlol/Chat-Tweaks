package me.lumiafk.chattweaks.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry
import java.awt.Color
import java.time.format.DateTimeFormatter

class TimeStampConfig {
	@SerialEntry
	@JvmField
	var enabled = true

	@SerialEntry
	@JvmField
	var alwaysShow = false

	@SerialEntry
	@JvmField
	var textColor = Color(203, 166, 247, 255);

	@SerialEntry
	@JvmField
	var backgroundColor = Color(0, 0, 0, 127); //Default background color for chat, with the default (configurable) background opacity of 50%

	@SerialEntry
	@JvmField
	var dateTimeFormat = "HH:mm:ss"

	@JvmField
	var dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat)!!
}