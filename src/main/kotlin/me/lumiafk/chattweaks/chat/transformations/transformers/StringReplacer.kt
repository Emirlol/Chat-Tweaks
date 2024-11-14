package me.lumiafk.chattweaks.chat.transformations.transformers

import me.lumiafk.chattweaks.chat.transformations.ChatTransformer
import me.lumiafk.chattweaks.chat.transformations.TransformerContext
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.text.Text

object StringReplacer : ChatTransformer {
	context(TransformerContext)
	override fun transform(input: Text): Text = MinecraftClientAudiences.of().asNative(
		MinecraftClientAudiences.of().asAdventure(input).replaceText {
			it.matchLiteral(stringFilter).replacement { result, builder ->
				if (result.hasMatch())  {
					if (replacementMode == ReplacementMode.PLAINTEXT_REPLACEMENT) builder.content(replacement) else return@replacement MiniMessage.miniMessage().deserialize(replacement)
				} else builder.build()
			}
		}
	)
}