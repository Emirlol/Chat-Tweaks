package me.lumiafk.chattweaks.chat.transformations.transformers

import com.mojang.serialization.Codec
import net.minecraft.util.StringIdentifiable

enum class ReplacementMode(val formatted: String): StringIdentifiable {
	MINI_MESSAGE("MiniMessage"),
	PLAINTEXT_REPLACEMENT("Plaintext");

	override fun asString() = name.lowercase()

	companion object {
		val CODEC: Codec<ReplacementMode> = StringIdentifiable.createBasicCodec(ReplacementMode::values)
	}
}