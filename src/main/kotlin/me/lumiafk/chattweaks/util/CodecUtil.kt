package me.lumiafk.chattweaks.util

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.awt.Color
import java.util.regex.Pattern

object Codecs {
	val PATTERN: Codec<Pattern> = RecordCodecBuilder.create { instance ->
		instance.group(
			Codec.STRING.fieldOf("pattern").forGetter { it.pattern() },
			Codec.INT.fieldOf("flags").forGetter { it.flags() }
		).apply(instance, Pattern::compile)
	}

	val COLOR: Codec<Color> = Codec.INT.xmap(
		{ int ->
			Color(int, true)
		},
		{ color ->
			color.rgb
		}
	)

	fun <T> Codec<T>.mutableList(minSize: Int = 0, maxSize: Int = Int.MAX_VALUE) = MutableListCodec<T>(this, minSize, maxSize)
}