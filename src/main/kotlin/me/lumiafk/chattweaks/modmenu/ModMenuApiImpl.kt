package me.lumiafk.chattweaks.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.lumiafk.chattweaks.config.ConfigHandler

class ModMenuApiImpl: ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory(ConfigHandler::generateScreen)
}