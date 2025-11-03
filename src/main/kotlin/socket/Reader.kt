package online.afeibaili.socket

import kotlinx.coroutines.*
import online.afeibaili.MChat.scope
import online.afeibaili.socket.cipher.Cipher
import online.afeibaili.socket.message.MessageManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.util.concurrent.Executors


/**
 * 读取器类
 *
 *@author AfeiBaili
 *@version 2025/11/3 23:22
 */

class Reader(socket: Socket, cipher: Cipher, catch: () -> Unit) {
    val job: Job

    init {
        val dispatcher: ExecutorCoroutineDispatcher =
            Executors.newSingleThreadExecutor().asCoroutineDispatcher()

        job = scope.launch(dispatcher) {
            runCatching {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                reader.use { reader ->
                    while (isActive) {
                        val readLine: String = reader.readLine()
                        MessageManager.parseMessage(cipher.decrypt(readLine))
                    }
                }
            }.onFailure { catch.invoke() }
        }
    }

    fun close() {
        job.cancel()
    }
}