package me.lumiafk.chattweaks.util

import com.mojang.serialization.Codec
import java.awt.Color

object ColorUtil {
	fun Color.multiplyOpacity(opacity: Double) = ((alpha * opacity).toInt() shl 24) or ((rgb shl 8) ushr 8)
	fun Color.multiplyOpacity(opacity: Float) = multiplyOpacity(opacity.toDouble())

	val CODEC: Codec<Color> = Codec.INT.xmap(
		{ int ->
			Color(int, true)
		},
		{ color ->
			color.rgb
		}
	)
}