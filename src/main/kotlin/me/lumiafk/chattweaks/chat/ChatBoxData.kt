package me.lumiafk.chattweaks.chat

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.lumiafk.chattweaks.util.Codecs
import java.awt.Color

class ChatBoxData(
	var x: Int,
	/**
	 * This is named y, but it's actually offset from max screen height.
	 * This is done to keep the chat box position relatively stable when window is resized,
	 * and the default chat position won't need a callback on resize.
	 */
	var y: Int,
	var width: Int,
	var focusedHeight: Int,
	var unfocusedHeight: Int,
	var scale: Float,
	var opacity: Float,
	var backgroundColor: Color,
	var renderTimestamp: RenderTimestamp
) : Cloneable {
	public override fun clone() = super.clone() as ChatBoxData

	companion object {
		val DEFAULT = ChatBoxData(0, 40, 328, 180, 90, 1.0f, 1.0f, Color(0x7F000000.toInt(), true), RenderTimestamp.Right)

		val CODEC: Codec<ChatBoxData> = RecordCodecBuilder.create { instance ->
			instance.group(
				PrimitiveCodec.INT.fieldOf("x").forGetter { it.x },
				PrimitiveCodec.INT.fieldOf("y").forGetter { it.y },
				PrimitiveCodec.INT.fieldOf("width").forGetter { it.width },
				PrimitiveCodec.INT.fieldOf("focusedHeight").forGetter { it.focusedHeight },
				PrimitiveCodec.INT.fieldOf("unfocusedHeight").forGetter { it.unfocusedHeight },
				PrimitiveCodec.FLOAT.fieldOf("scale").forGetter { it.scale },
				PrimitiveCodec.FLOAT.fieldOf("opacity").forGetter { it.opacity },
				Codecs.COLOR.fieldOf("backgroundColor").forGetter { it.backgroundColor },
				RenderTimestamp.CODEC.fieldOf("renderTimestamp").forGetter { it.renderTimestamp }
			).apply(instance, ::ChatBoxData)
		}
	}
}