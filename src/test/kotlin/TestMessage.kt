import online.afeibaili.config.ConfigManager
import online.afeibaili.listener.MessageListener
import online.afeibaili.socket.ServerManager
import kotlin.test.Test

/**
 * 测试消息
 *
 *@author AfeiBaili
 *@version 2025/12/27 22:20
 */

class TestMessage {
    @Test
    fun test(){
        ConfigManager.load()
        MessageListener.load()
        ServerManager.load()
        Runtime.getRuntime().addShutdownHook(Thread { ServerManager.unload() })
    }
}