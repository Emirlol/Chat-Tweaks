package me.lumiafk.chattweaks.chat.screens.elements

import me.lumiafk.chattweaks.chat.ChatBox
import me.lumiafk.chattweaks.chat.ChatBoxData
import me.lumiafk.chattweaks.chat.screens.ChatBoxMovementScreen
import me.lumiafk.chattweaks.chat.screens.Initializable
import me.lumiafk.chattweaks.chat.screens.elements.AbstractWidget.Companion.BUTTON_HEIGHT
import me.lumiafk.chattweaks.chat.screens.elements.AbstractWidget.Companion.INNER_PADDING
import me.lumiafk.chattweaks.util.ElementUtil.buttonWidget
import me.lumiafk.chattweaks.util.ElementUtil.textFieldWidget
import me.lumiafk.chattweaks.util.client
import me.lumiafk.chattweaks.util.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.MathHelper
import java.util.regex.PatternSyntaxException

class ButtonEntryList(y: Int, width: Int, height: Int, val chatBoxes: MutableList<ChatBox>) : ElementListWidget<ButtonEntryList.ElementEntry>(MinecraftClient.getInstance(), width, height, y, BUTTON_HEIGHT + INNER_PADDING), Initializable {
	override fun init() {
		chatBoxes.map { ElementEntry(it, this) }.forEach(::addEntry)
		addEntry(ElementEntry(null, this))
		children().filterIsInstance<Initializable>().forEach(Initializable::init)
	}

	override fun getRowWidth(): Int = width
	override fun getRowLeft(): Int = this.x
	override fun getRowTop(index: Int): Int = this.y - scrollAmount.toInt() + index * this.itemHeight

	/*no-op*/
	override fun drawMenuListBackground(context: DrawContext) {}

	class ElementEntry(var chatBox: ChatBox?, val parent: ButtonEntryList) : Entry<ElementEntry>(), Initializable {
		private var children = mutableListOf<ClickablePosRenderedWidget>()

		override fun children() = children
		override fun selectableChildren() = children

		override fun init() {
			if (chatBox != null) {
				children += textFieldWidget(client.textRenderer, 3, Text.of("Chat Box Name"), chatBox!!.name.string).apply {
					placeholder = Text.literal("Click to load name").formatted(Formatting.DARK_GRAY)
					changedListener = { chatBox!!.name = it.text }
				}
				children += textFieldWidget(client.textRenderer, 10, Text.of("Chat Box Regex"), chatBox!!.pattern?.toString() ?: "").apply {
					placeholder = Text.literal("Click to load regex").formatted(Formatting.DARK_GRAY)
					maxLength = 512
					changedListener = {
						try {
							chatBox!!.pattern = if (it.isEmpty()) null else it.toPattern()
							error = ""
						} catch (e: PatternSyntaxException) {
							error = e.description
						}
					}
				}
				children += buttonWidget("Move".text, 1) {
					client.setScreen(ChatBoxMovementScreen(client.currentScreen, chatBox!!, parent.chatBoxes.toMutableList().apply { remove(chatBox!!) }))
				}
				children += buttonWidget("Delete".text, 1) {
					parent.chatBoxes.remove(chatBox)
					parent.children().removeIf { it is ElementEntry && it.chatBox == chatBox || it.chatBox == null }
					val element = ElementEntry(null, parent)
					parent.addEntry(element)
					element.init()
				}
			} else {
				children += buttonWidget("Create New Chat Box".text, 1) {
					children.clear()
					chatBox = ChatBox("New Chat Box".text, "", ChatBoxData.DEFAULT.clone())
					parent.chatBoxes.add(chatBox!!)
					init()
					val element = ElementEntry(null, parent)
					parent.addEntry(element)
					element.init()
				}
			}
		}

		override fun render(context: DrawContext, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
			val totalWidth = entryWidth - ((children.size - 1) * INNER_PADDING)
			val totalWidthOfDrawables = children.sumOf(ClickablePosRenderedWidget::getWidth) //They are supposed to have arbitrary widths and the available space will be distributed among them according to their width
			val unitWidth = totalWidth / totalWidthOfDrawables
			var excessWidth = totalWidth - (unitWidth * totalWidthOfDrawables) //There remainder of the division will be distributed among the widgets

			var drawableWidth: Int
			var drawableX = x
			for (drawable in children) {
				drawableWidth = drawable.width * unitWidth

				if (excessWidth > 0) {
					drawableWidth++
					excessWidth--
				}

				drawable.render(context, drawableX, MathHelper.lerp(tickDelta, drawable.previousY, y), drawableWidth, entryHeight, mouseX, mouseY, tickDelta)
				drawable.previousY = y
				drawableX += drawableWidth + INNER_PADDING
			}
		}
	}

	// no-op
	override fun drawHeaderAndFooterSeparators(context: DrawContext) {}
}
