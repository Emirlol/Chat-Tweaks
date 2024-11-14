package me.lumiafk.chattweaks.chat

import com.mojang.serialization.Codec

//Hacky enum
sealed interface RenderTimestamp {
	data object Left : RenderTimestamp
	data object Right : RenderTimestamp
	data object None : RenderTimestamp

	companion object {
		val CODEC: Codec<RenderTimestamp> = Codec.stringResolver(RenderTimestamp::toString, RenderTimestamp::fromString)
		val entries = setOf(Left, Right, None)
		fun fromString(string: String) = when (string) {
			"Left" -> Left
			"Right" -> Right
			else -> None
		}
	}
}