package me.lumiafk.chattweaks.config.configs

import me.lumiafk.chattweaks.config.ConfigProxy
import java.awt.Color

object HudConfig : ConfigProxy {
	val drawAlternatingRow by register(true)

	val alternatingRowColor by register(Color(255, 255, 255, 12))
}