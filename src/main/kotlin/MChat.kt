package online.afeibaili

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

object MChat : KotlinPlugin(
    JvmPluginDescription(
        id = "online.afeibaili.mchat",
        name = "MChat",
        version = "3.0.0",
    ) {
        author("AfeiBaili")
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
    }
}