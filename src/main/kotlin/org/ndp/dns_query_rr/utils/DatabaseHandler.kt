package org.ndp.dns_query_rr.utils

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import org.ndp.dns_query_rr.bean.BatchInsertDomainIP
import org.ndp.dns_query_rr.bean.Task
import org.ndp.dns_query_rr.table.DomainIP
import org.ndp.dns_query_rr.utils.Logger.logger
import java.nio.ByteBuffer
import java.security.MessageDigest
import org.ndp.dns_query_rr.table.Task as TableTask

object DatabaseHandler {
    private val dbUrl = Settings.setting["dbUrl"] as String
    private val dbDriver = Settings.setting["dbDriver"] as String
    private val dbUser = Settings.setting["dbUser"] as String
    private val dbPassword = Settings.setting["dbPassword"] as String
    private val database: Database


    init {
        database = Database.Companion.connect(
            dbUrl,
            dbDriver,
            dbUser,
            dbPassword
        )
    }

    fun batchUpdateTaskStatus(updateTasks: List<Task>) {
        logger.debug("task size: ${updateTasks.size}")
        TableTask.batchUpdate {
            for (task in updateTasks) {
                item {
                    it.taskStatus to task.status
                    it.desc to task.desc
                    where {
                        TableTask.id eq task.id
                    }
                }
            }
        }
    }

    fun findDomain2IP(domain: String, ip: String): Boolean {
        val hashKey = OtherTools.digestMD5(domain)
        val ipInt = OtherTools.iNetString2Number(ip)

        val check = database.useConnection { conn ->
            val sql = """
                select id from domain_ip 
                use index(domain_ip_domain_hash_index, domain_ip_ip_id_index) 
                where ip_id = ? and domain_hash = ? and domain = ?
            """
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, ipInt)
                stmt.setLong(2, hashKey)
                stmt.setString(3, domain)

                stmt.executeQuery().iterable().map { it.getInt(1) }
            }
        }

        return check.isEmpty()
    }

    fun batchInsertDomainIP(insertDomainIP: List<BatchInsertDomainIP>) {
        logger.debug("domain ip records: ${insertDomainIP.size}")
        DomainIP.batchInsert {
            for (i in insertDomainIP) {
                item {
                    it.domain to i.domain
                    it.domainHash to i.domainHash
                    it.ipID to i.ipID
                }
            }
        }
    }
}