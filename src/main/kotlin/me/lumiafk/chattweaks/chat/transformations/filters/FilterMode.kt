package me.lumiafk.chattweaks.chat.transformations.filters

import com.mojang.serialization.Codec
import net.minecraft.util.StringIdentifiable

enum class FilterMode(val formatted: String) : StringIdentifiable {
	BLACKLIST("Blacklist"),
	WHITELIST("Whitelist");

	override fun asString(): String = name.lowercase()

	companion object {
		val CODEC: Codec<FilterMode> = StringIdentifiable.createBasicCodec(FilterMode::values)
	}
}
