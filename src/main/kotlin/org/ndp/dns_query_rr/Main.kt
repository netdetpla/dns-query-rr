package org.ndp.dns_query_rr

import org.ndp.dns_query_rr.bean.BatchInsertDomainIP
import org.ndp.dns_query_rr.bean.Task
import org.ndp.dns_query_rr.utils.DatabaseHandler
import org.ndp.dns_query_rr.utils.Logger.logger
import org.ndp.dns_query_rr.utils.OtherTools
import org.ndp.dns_query_rr.utils.RedisHandler

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("start result recycling...")
        val results = RedisHandler.consumeResult(RedisHandler.generateNonce(5))
        RedisHandler.returnACK()
        val updateTasks = ArrayList<Task>()
        val insertDomainIPs = ArrayList<BatchInsertDomainIP>()
        for (r in results) {
            // task status update
            if (r.status == 1) {
                updateTasks.add(Task(r.taskID, 21000, r.desc))
                continue
            }
            updateTasks.add(Task(r.taskID, 20030, ""))
            // domain ip
            for (rr in r.result) {
                for (a in rr.aRecord) {
                    if (!DatabaseHandler.findDomain2IP(rr.domain, a)) {
                        insertDomainIPs.add(
                            BatchInsertDomainIP(
                                rr.domain,
                                OtherTools.digestMD5(rr.domain),
                                OtherTools.iNetString2Number(a)
                            )
                        )
                    }
                }
            }
            DatabaseHandler.batchInsertDomainIP(insertDomainIPs)
            DatabaseHandler.batchUpdateTaskStatus(updateTasks)
        }
    }
}