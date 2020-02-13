package org.ndp.dns_query_rr.bean

data class DNSRR(
    val domain: String,
    val dnsServer: String,
    val aRecord: ArrayList<String>,
    val cName: ArrayList<String>
)