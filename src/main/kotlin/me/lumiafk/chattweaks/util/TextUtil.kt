package me.lumiafk.chattweaks.util

import net.minecraft.text.MutableText
import net.minecraft.text.StringVisitable
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

object TextUtil {
	fun text(init: TextBuilder.() -> Unit): MutableText = TextBuilder().apply(init).build()

	class TextBuilder {
		private var mainText: MutableText = Text.empty()

		/**
		 * @return the added text
		 */
		operator fun MutableText.unaryPlus(): MutableText {
			mainText.append(this)
			return this
		}

		/**
		 * @return the added text after turning the string into a literal
		 */
		operator fun String.unaryPlus(): MutableText = this.literal.unaryPlus()

		/**
		 * @return the added text after turning the object into a string and then into a literal
		 */
		operator fun Any?.unaryPlus(): MutableText = this.toString().unaryPlus()

		fun build(): MutableText = mainText
	}

	fun Text.removeFormatting(): Text {
		val text = Formatting.strip(this.string)!!.literal
		text.style = this.style
		// The string type doesn't really matter since we're just returning an empty optional each time
		text.visit(StringVisitable.StyledVisitor<String> { style, string ->
			text.append(Formatting.strip(string)!!.literal(style))
			Optional.empty()
		}, Style.EMPTY)

		return text
	}
}