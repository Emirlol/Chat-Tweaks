package me.lumiafk.chattweaks.chat.screens

interface Initializable {
	/**
	 * Similar to [Screen.init][net.minecraft.client.gui.screen.Screen.init], this is meant to be called when the screen is initialized or resized.
	 * @see [me.lumiafk.chattweaks.util.initChildren]
	 */
	fun init()

	/**
	 * This is meant for saving elements to fields, so they are not initialized every time the screen is resized.
	 *
	 * This approach also helps with the state of the element being saved on screen resize and a child screen closing back to the parent instance.
	 */
	fun recalculate() {}
}