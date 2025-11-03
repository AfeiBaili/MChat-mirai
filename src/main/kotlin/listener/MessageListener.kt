package online.afeibaili.listener

import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import online.afeibaili.config.ConfigManager
import online.afeibaili.socket.message.MessageManager
import online.afeibaili.socket.message.TextMessage


/**
 * 消息监听器
 *
 *@author AfeiBaili
 *@version 2025/11/3 21:49
 */

object MessageListener {
    fun load() {
        GlobalEventChannel.filter { event -> event is GroupMessageEvent && ConfigManager.groups.contains(event.group.id) }
            .subscribeAlways<GroupMessageEvent> { event ->
                val message: String = event.message.contentToString()
                val name: String = event.sender.nick
                val groupName = event.group.name
                MessageManager.sendToMC(TextMessage("$groupName $name：$message"))
            }

        GlobalEventChannel.subscribeAlways<BotOnlineEvent> { event ->
            MessageManager.bot = event.bot
        }
    }
}