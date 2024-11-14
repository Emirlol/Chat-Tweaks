package me.lumiafk.chattweaks.chat.transformations

import net.minecraft.text.Text

interface ChatTransformer {

	/**
	 * Transforms text in some way.
	 *
	 * If the transformation is a filter (i.e. it may remove the input text), it should return null if the input text should be removed.
	 */
	context(TransformerContext)
	fun transform(input: Text): Text?
}