@file:Suppress("UnstableApiUsage")

package me.lumiafk.chattweaks.config

import com.mojang.serialization.Codec
import dev.isxander.yacl3.config.v3.CodecConfig
import dev.isxander.yacl3.config.v3.ConfigEntry
import dev.isxander.yacl3.config.v3.EntryAddable
import dev.isxander.yacl3.config.v3.ReadonlyConfigEntry
import me.lumiafk.chattweaks.util.Codecs
import java.awt.Color
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

interface ConfigProxy : EntryAddable {
	override fun <T : Any?> register(fieldName: String?, defaultValue: T, codec: Codec<T>?): ConfigEntry<T> = ConfigHandler.register(fieldName, defaultValue, codec)

	override fun <T : CodecConfig<T>?> register(fieldName: String?, configInstance: T): ReadonlyConfigEntry<T> = ConfigHandler.register(fieldName, configInstance)

	fun register(default: Byte) = PropertyDelegateProvider<EntryAddable, ReadOnlyProperty<EntryAddable, ConfigEntry<Byte>>> { thisRef, property ->
		val entry = thisRef.register(property.name, default, Codec.BYTE)
		ReadOnlyProperty { _, _ -> entry }
	}

	fun register(default: Short) = PropertyDelegateProvider<EntryAddable, ReadOnlyProperty<EntryAddable, ConfigEntry<Short>>> { thisRef, property ->
		val entry = thisRef.register(property.name, default, Codec.SHORT)
		ReadOnlyProperty { _, _ -> entry }
	}

	fun register(default: Int) = PropertyDelegateProvider<EntryAddable, ReadOnlyProperty<EntryAddable, ConfigEntry<Int>>> { thisRef, property ->
		val entry = thisRef.register(property.name, default, Codec.INT)
		ReadOnlyProperty { _, _ -> entry }
	}

	fun register(default: Long) = PropertyDelegateProvider<EntryAddable, ReadOnlyProperty<EntryAddable, ConfigEntry<Long>>> { thisRef, property ->
		val entry = thisRef.register(property.name, default, Codec.LONG)
		ReadOnlyProperty { _, _ -> entry }
	}

	fun register(default: Float) = PropertyDelegateProvider<EntryAddable, ReadOnlyProperty<EntryAddable, ConfigEntry<Float>>> { thisRef, property ->
		val entry = thisRef.register(property.name, default, Codec.FLOAT)
		ReadOnlyProperty { _, _ -> entry }
	}

	fun register(default: Double) = PropertyDelegateProvider<EntryAddable, ReadOnlyProperty<EntryAddable, ConfigEntry<Double>>> { thisRef, property ->
		val entry = thisRef.register(property.name, default, Codec.DOUBLE)
		ReadOnlyProperty { _, _ -> entry }
	}

	fun register(default: String) = PropertyDelegateProvider<EntryAddable, ReadOnlyProperty<EntryAddable, ConfigEntry<String>>> { thisRef, property ->
		val entry = thisRef.register(property.name, default, Codec.STRING)
		ReadOnlyProperty { _, _ -> entry }
	}

	fun register(default: Boolean) = PropertyDelegateProvider<EntryAddable, ReadOnlyProperty<EntryAddable, ConfigEntry<Boolean>>> { thisRef, property ->
		val entry = thisRef.register(property.name, default, Codec.BOOL)
		ReadOnlyProperty { _, _ -> entry }
	}

	fun register(default: Color) = PropertyDelegateProvider<EntryAddable, ReadOnlyProperty<EntryAddable, ConfigEntry<Color>>> { thisRef, property ->
		val entry = thisRef.register(property.name, default, Codecs.COLOR)
		ReadOnlyProperty { _, _ -> entry }
	}
}