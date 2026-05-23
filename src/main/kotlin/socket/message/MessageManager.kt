package online.afeibaili.socket.message

import net.mamoe.mirai.Bot
import online.afeibaili.config.ConfigManager
import online.afeibaili.socket.ServerManager
import java.net.Socket


/**
 * 消息管理器
 *
 *@author AfeiBaili
 *@version 2025/11/3 21:50
 */

object MessageManager {
    lateinit var bot: Bot

    fun sendToMC(message: Message) {
        ServerManager.send(message)
    }

    suspend fun sendToGroup(message: String) {
        ConfigManager.groups.forEach { group -> bot.getGroup(group)?.sendMessage(message) }
    }

    suspend fun parseMessage(message: String, socket: Socket) {
        val ident: String = message.take(4)
        val message: String = message.drop(4)
        val msg: Message? = match(ident, message)
        //多服务器共享
        if (msg is TextMessage) ServerManager.send(msg, socket)
    }

    fun parseGroupMessage(nick: String, groupName: String, message: String) {
        if (message.startsWith("!")) {
            sendToMC(CommandMessage(message.drop(1)))
        } else {
            sendToMC(TextMessage("$groupName ${nick}：$message"))
        }
    }

    fun sendImageToMc(url: String) {
        sendToMC(ImageMessage(url))
    }

    suspend fun match(ident: String, message: String) = when (ident) {
        "txt:" -> TextMessage(message).apply {
            sendToGroup(this.message)
        }

        "cmd:" -> CommandMessage(message)
        "het:" -> HeartbeatMessage(message)
        "img" -> ImageMessage(message)
        else -> null
    }
}