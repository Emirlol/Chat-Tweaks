package me.lumiafk.chattweaks.util

import com.demonwav.mcdev.annotations.Translatable
import me.lumiafk.chattweaks.ChatTweaks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier

val client: MinecraftClient get() = MinecraftClient.getInstance()
val player: ClientPlayerEntity? get() = client.player

fun ClientPlayerEntity.sendText(message: Text, overlay: Boolean = false) = sendMessage(message, overlay)

val String.text: Text get() = Text.of(this)
val String.literal: MutableText get() = Text.literal(this)

//This annotation doesn't work for kotlin yet, but I'll still keep it just in case
val @receiver:Translatable(foldMethod = true) String.translatable: MutableText get() = Text.translatable(this)

fun isPointIn(x: Int, y: Int, x1: Int, y1: Int, x2: Int, y2: Int) = x in x1..x2 && y in y1..y2

val String.identifier: Identifier get() = Identifier.of(ChatTweaks.NAMESPACE, this)
