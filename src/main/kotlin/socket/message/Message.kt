package online.afeibaili.socket.message


/**
 * 消息类型
 *
 *@author AfeiBaili
 *@version 2025/11/3 18:39
 */

sealed class Message(val identifier: String) {
    abstract val message: String
    override fun toString() = "$identifier$message"
}

class TextMessage(override val message: String) : Message("txt:")
class CommandMessage(override val message: String) : Message("cmd:")
class HeartbeatMessage(override val message: String) : Message("het:")