package online.afeibaili.socket.cipher

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.math.min


/**
 * 密码工具类
 *
 *@author AfeiBaili
 *@version 2025/11/3 14:37
 */

typealias C = Cipher

class Cipher(val token: String) {

    private val keySpec = SecretKeySpec(fixTokenLength(16), "AES")

    fun encrypt(content: String): String {
        val cipher = C.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(C.ENCRYPT_MODE, keySpec)
        val final: ByteArray = cipher.doFinal(content.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(final)
    }

    fun decrypt(encrypted: String): String {
        val cipher = C.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(C.DECRYPT_MODE, keySpec)
        val final: ByteArray = cipher.doFinal(Base64.getDecoder().decode(encrypted))
        return String(final, StandardCharsets.UTF_8)
    }

    fun fixTokenLength(length: Int): ByteArray {
        val bytes: ByteArray = MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray(StandardCharsets.UTF_8))
        val length = min(length, 32)
        return bytes.copyByLength(length)
    }

    fun ByteArray.copyByLength(length: Int): ByteArray {
        val byteArray = ByteArray(length)
        (0 until min(length, this.size))
            .forEachIndexed { i, _ -> byteArray[i] = this[i] }

        return byteArray
    }
}