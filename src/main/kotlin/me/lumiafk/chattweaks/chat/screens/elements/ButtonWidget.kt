package me.lumiafk.chattweaks.chat.screens.elements

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.text.MutableText
import net.minecraft.text.Text

typealias PressAction = (ButtonWidget) -> Unit
typealias NarrationSupplier = (() -> MutableText) -> MutableText

@Environment(EnvType.CLIENT)
open class ButtonWidget protected constructor(x: Int, y: Int, width: Int, height: Int, message: Text?, protected val pressAction: PressAction, protected val narrationSupplier: NarrationSupplier) : PressableWidget(x, y, width, height, message) {
	override fun onPress() {
		pressAction(this)
	}

	override fun getNarrationMessage(): MutableText {
		return narrationSupplier { super.getNarrationMessage() }
	}

	@Environment(EnvType.CLIENT)
	class Builder(private val message: Text, private val onPress: PressAction) {
		private var tooltip: Tooltip? = null
		private var x = 0
		private var y = 0
		private var width = DEFAULT_WIDTH
		private var height = DEFAULT_HEIGHT
		private var narrationSupplier = DEFAULT_NARRATION_SUPPLIER

		fun position(x: Int, y: Int): Builder {
			this.x = x
			this.y = y
			return this
		}

		fun width(width: Int): Builder {
			this.width = width
			return this
		}

		fun size(width: Int, height: Int): Builder {
			this.width = width
			this.height = height
			return this
		}

		fun dimensions(x: Int, y: Int, width: Int, height: Int): Builder {
			return position(x, y).size(width, height)
		}

		fun tooltip(tooltip: Tooltip): Builder {
			this.tooltip = tooltip
			return this
		}

		fun narrationSupplier(narrationSupplier: NarrationSupplier): Builder {
			this.narrationSupplier = narrationSupplier
			return this
		}

		fun build(): ButtonWidget {
			val buttonWidget = ButtonWidget(this.x, this.y, this.width, this.height, this.message, this.onPress, this.narrationSupplier)
			buttonWidget.tooltip = tooltip
			return buttonWidget
		}
	}

	companion object {
		const val DEFAULT_WIDTH_SMALL: Int = 120
		const val DEFAULT_WIDTH: Int = 150
		const val field_49479: Int = 200
		const val DEFAULT_HEIGHT: Int = 20
		const val field_46856: Int = 8
		protected val DEFAULT_NARRATION_SUPPLIER: NarrationSupplier = { textSupplier -> textSupplier() }
		fun builder(message: Text, onPress: PressAction) = Builder(message, onPress)
	}
}
