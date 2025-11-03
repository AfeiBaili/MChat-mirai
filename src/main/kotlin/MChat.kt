package online.afeibaili

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import online.afeibaili.config.ConfigManager
import online.afeibaili.listener.MessageListener
import online.afeibaili.socket.ServerManager

object MChat : KotlinPlugin(
    JvmPluginDescription(
        id = "online.afeibaili.mchat",
        name = "MChat",
        version = "3.0.0",
    ) {
        author("AfeiBaili")
    }
) {
    val scope = CoroutineScope(Dispatchers.Default)

    override fun onEnable() {
        ConfigManager.load()
        MessageListener.load()
        ServerManager.load()
        Runtime.getRuntime().addShutdownHook(Thread { ServerManager.unload() })
    }
}