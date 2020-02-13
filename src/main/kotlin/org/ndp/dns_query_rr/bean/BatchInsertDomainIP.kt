package org.ndp.dns_query_rr.bean

data class BatchInsertDomainIP(
    val domain: String,
    val domainHash: Long,
    val ipID: Long
)