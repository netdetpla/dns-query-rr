package org.ndp.dns_query_rr.utils

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.lettuce.core.RedisClient
import io.lettuce.core.XReadArgs
import io.lettuce.core.api.sync.RedisCommands
import org.ndp.dns_query_rr.bean.MQResult
import org.ndp.dns_query_rr.utils.Logger.logger

object RedisHandler {
    private val commands: RedisCommands<String, String>
    private val mqResultAdapter: JsonAdapter<MQResult>
    private val consumedIDs = ArrayList<String>()

    init {
        val client = RedisClient.create(Settings.setting["redis.url"] as String)
        val connection = client.connect()
        commands = connection.sync()

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        mqResultAdapter = moshi.adapter(MQResult::class.java)
    }

    fun generateNonce(size: Int): String {
        val nonceScope = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val scopeSize = nonceScope.length
        val nonceItem: (Int) -> Char = { nonceScope[(scopeSize * Math.random()).toInt()] }
        return Array(size, nonceItem).joinToString("")
    }

    fun consumeResult(name: String): List<MQResult> {
        val consumer = io.lettuce.core.Consumer.from(
            Settings.setting["group"] as String, name
        )
        val content = commands.xreadgroup(
            consumer,
            XReadArgs.StreamOffset.lastConsumed(Settings.setting["key.result"] as String)
        )
        val results = ArrayList<MQResult>()
        logger.debug("result size: ${results.size}")
        for (c in content) {
            results.add(
                mqResultAdapter.fromJson(c.body["result"]!!)!!
            )
            consumedIDs.add(c.id)
        }
        return results
    }

    fun returnACK() {
        if (consumedIDs.isEmpty()) return
        commands.xack(
            Settings.setting["key.result"] as String,
            Settings.setting["group"] as String,
            *consumedIDs.toArray(arrayOf(""))
        )
    }
}