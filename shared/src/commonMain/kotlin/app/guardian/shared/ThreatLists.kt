package app.guardian.shared

/**
 * A blocklist the engine can load on the device. [engineTag] and [engineValue] are the list's
 * "uname" and "value" in the RethinkDNS blocklist catalogue (app/src/main/assets/filetag.json);
 * other platforms map them to their own format.
 */
data class ThreatList(
    val engineTag: String,
    val engineValue: Int,
    val name: String,
    val module: Module,
    /** Licence as last checked; null means not yet verified. */
    val licence: String?,
)

object ThreatLists {
    val HAGEZI_TIF = ThreatList("IKY", 68, "HaGeZi Threat Intelligence Feeds", Module.CORE, "GPL-3.0")
    val URLHAUS = ThreatList("THG", 57, "URLhaus (abuse.ch)", Module.CORE, null)
    val PHISHING_ARMY = ThreatList("GDA", 62, "Phishing Army", Module.SCAM_SHIELD, null)
    val GLOBAL_ANTI_SCAM = ThreatList("SPR", 85, "Global Anti-Scam Org", Module.SCAM_SHIELD, null)
    val HAGEZI_BYPASS = ThreatList("MTF", 0, "HaGeZi DoH/VPN/proxy bypass", Module.LOCKDOWN, "GPL-3.0")

    val all = listOf(HAGEZI_TIF, URLHAUS, PHISHING_ARMY, GLOBAL_ANTI_SCAM, HAGEZI_BYPASS)

    /** Lists switched on for a persona. */
    fun forPersona(persona: Persona): List<ThreatList> = all.filter { it.module in persona.modules }
}
