package online.afeibaili.listener

import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import online.afeibaili.config.ConfigManager
import online.afeibaili.socket.message.MessageManager


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
                val name: String = event.sender.nick
                val groupName = event.group.name
                val message: String = event.message.contentToString()
                MessageManager.parseGroupMessage(name, groupName, message)

                event.message.forEach { message ->
                    if (message is Image) MessageManager.sendImageToMc(message.queryUrl())
                }
            }

        GlobalEventChannel.subscribeAlways<BotOnlineEvent> { event ->
            MessageManager.bot = event.bot
        }
    }
}