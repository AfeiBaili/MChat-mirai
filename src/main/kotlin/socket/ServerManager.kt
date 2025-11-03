package online.afeibaili.socket

import kotlinx.coroutines.*
import online.afeibaili.MChat.logger
import online.afeibaili.MChat.scope
import online.afeibaili.config.ConfigManager
import online.afeibaili.socket.cipher.Cipher
import online.afeibaili.socket.message.Message
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors


/**
 * 套接字服务器管理
 *
 *@author AfeiBaili
 *@version 2025/11/3 21:49
 */

object ServerManager {
    lateinit var server: ServerSocket
    val cipher = Cipher(ConfigManager.token)
    lateinit var serverJob: Job
    val socketMap = mutableMapOf<Socket, Pair<Reader, PrintWriter>>()

    fun load() {
        server = ServerSocket(ConfigManager.port)
        val dispatcher: ExecutorCoroutineDispatcher =
            Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        serverJob = scope.launch(dispatcher) {
            server.soTimeout = 10000
            while (isActive) runCatching {
                val socket: Socket = server.accept()
                logger.info("客户端连接：${socket.inetAddress.hostAddress}:${socket.port}")
                socketMap[socket] = Pair(
                    Reader(socket, cipher) { socket.close() },
                    PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)
                )
            }
        }
    }

    fun unload() {
        server.close()
        serverJob.cancel()
        socketMap.forEach { (socket, pair) ->
            socket.close()
            runCatching { pair.first.close() }
            runCatching { pair.second.close() }
        }
        logger.info("已关闭MChat服务器")
    }

    fun send(message: Message) {
        val removeSet = mutableSetOf<Socket>()
        socketMap.forEach { (socket, pair) ->
            runCatching {
                if (socket.isClosed) throw RuntimeException("套接字已断开连接！")
                pair.second.println(cipher.encrypt(message.toString()))
            }.onFailure { exception ->
                socket.close()
                runCatching { pair.first.close() }
                runCatching { pair.second.close() }
                removeSet.add(socket)
            }
        }
        removeSet.forEach { socketMap.remove(it) }
    }
}