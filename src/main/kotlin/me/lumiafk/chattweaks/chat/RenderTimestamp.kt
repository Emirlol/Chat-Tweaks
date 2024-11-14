package me.lumiafk.chattweaks.chat

import com.mojang.serialization.Codec
import net.minecraft.util.StringIdentifiable

//Hacky enum
sealed interface RenderTimestamp : StringIdentifiable {
	data object Right : RenderTimestamp
	data object Left : RenderTimestamp
	data object None : RenderTimestamp

	override fun asString(): String = toString().lowercase()

	companion object {
		val CODEC: Codec<RenderTimestamp> = StringIdentifiable.createBasicCodec { arrayOf(Right, Left, None) }
	}
}