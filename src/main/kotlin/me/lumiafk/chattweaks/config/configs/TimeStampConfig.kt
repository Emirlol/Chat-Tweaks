package me.lumiafk.chattweaks.config.configs

import dev.isxander.yacl3.config.v3.ConfigEntry
import dev.isxander.yacl3.config.v3.value
import me.lumiafk.chattweaks.config.ConfigProxy
import me.lumiafk.chattweaks.util.ColorPalette
import java.awt.Color
import java.time.format.DateTimeFormatter

@Suppress("UnstableApiUsage")
object TimeStampConfig : ConfigProxy {
	val enabled by register(true)

	val alwaysShow by register(false)

	val groupingMillis by register(1000L)

	val textColor by register(ColorPalette.MAUVE)

	val backgroundColor by register(Color(0, 0, 0, 127))

	val dateTimeFormat: ConfigEntry<String> by register("HH:mm:ss")

	var dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat.value)

	init {
		dateTimeFormat.onSet {
			dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat.value)
		}
	}
}