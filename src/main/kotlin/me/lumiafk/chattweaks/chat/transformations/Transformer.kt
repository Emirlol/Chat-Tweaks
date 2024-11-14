package me.lumiafk.chattweaks.chat.transformations

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class Transformer(var type: TransformerType, val context: TransformerContext) {
	companion object {
		val CODEC: Codec<Transformer> = RecordCodecBuilder.create { instance ->
			instance.group(
				TransformerType.CODEC.fieldOf("type").forGetter { it.type },
				TransformerContext.CODEC.fieldOf("context").forGetter { it.context }
			).apply(instance, ::Transformer)
		}
	}
}