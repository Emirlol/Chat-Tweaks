package me.lumiafk.chattweaks.config

import me.lumiafk.chattweaks.config.configs.HudConfig
import me.lumiafk.chattweaks.config.configs.OtherConfig
import me.lumiafk.chattweaks.config.configs.TimeStampConfig

class Config {
	@JvmField
	var timeStampConfig = TimeStampConfig()

	@JvmField
	var hudConfig = HudConfig()

	@JvmField
	var otherConfig = OtherConfig()
}