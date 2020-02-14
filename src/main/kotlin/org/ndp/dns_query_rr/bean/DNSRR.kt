package org.ndp.dns_query_rr.bean

data class DNSRR(
    val domain: String,
    val dnsServer: String,
    val aRecord: List<String>,
    val cName: List<String>
)