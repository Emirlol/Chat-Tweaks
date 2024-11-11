package me.lumiafk.chattweaks

import net.minecraft.text.CharacterVisitor
import net.minecraft.text.OrderedText

class OriginedOrderedText(private val core: OrderedText, val originHashCode: Int) : OrderedText {
	override fun accept(characterVisitor: CharacterVisitor) = core.accept(characterVisitor)
}