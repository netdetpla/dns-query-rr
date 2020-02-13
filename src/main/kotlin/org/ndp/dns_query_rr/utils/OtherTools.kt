package org.ndp.dns_query_rr.utils

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*

object OtherTools {
    private val md5Instance = MessageDigest.getInstance("MD5")

    fun digestMD5(domain: String): Long {
        val domainMD5 = md5Instance.digest(domain.toByteArray())
        return ByteBuffer.wrap(domainMD5.slice(0 until 8).toByteArray()).long
    }

    fun iNetString2Number(ipStr: String): Long {
        return Arrays.stream(ipStr.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .map { java.lang.Long.parseLong(it) }
            .reduce(0L) { x, y -> (x!! shl 8) + y!! }
    }

    fun iNetNumber2String(ipLong: Long): String {
        var origin = ipLong
        val segments = ArrayList<String>()
        for (i in 0..3) {
            segments.add((origin % 256L).toString())
            origin /= 256
        }
        segments.reverse()
        return segments.joinToString(".")
    }
}