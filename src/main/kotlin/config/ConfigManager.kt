package online.afeibaili.config

import online.afeibaili.MChat.logger
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * 配置管理类
 *
 *@author AfeiBaili
 *@version 2025/11/3 21:50
 */

object ConfigManager {
    val groups: MutableSet<Long> = mutableSetOf()
    lateinit var token: String
    var port: Int = 33393

    fun load() {
        val file = File("${System.getProperty("user.dir")}/config/mchat.conf")
        file.parentFile.mkdirs()
        if (!file.exists()) {
            PrintWriter(file, StandardCharsets.UTF_8).use { out ->
                out.println("# 要监听和发送的群号，多个群聊使用英文逗号分割")
                out.println("groups=975709430")
                out.println("# 开放端口")
                out.println("port=33393")
                out.println("# 密钥（用于安全通讯，客户端密钥需和此密钥一致）")
                out.println("token=${UUID.randomUUID().toString().take(9)}")
            }
            logger.info("已生成配置文件，请重启进行配置：${file.absolutePath}")
        }
        FileReader(file).use { reader ->
            reader.readLines().forEach { line ->
                if (line.startsWith("#")) return@forEach
                val split: List<String> = line.split("=")
                if (split.size != 2) return@forEach
                val param: String = split[0]
                val value: String = split[1]
                match(param, value)
            }
        }
    }

    private fun match(param: String, value: String) = when (param) {
        "group" -> {
            val strings: List<String> = value.split(",").map { it -> it.trim() }
            val longs: List<Long> = strings.map { it.toLong() }
            groups.addAll(longs)
        }

        "port" -> {
            port = value.split(" ")[0].toInt()
        }

        "token" -> {
            token = value.split(" ")[0]
        }

        else -> Unit
    }
}