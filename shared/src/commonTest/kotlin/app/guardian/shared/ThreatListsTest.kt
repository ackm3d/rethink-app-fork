package app.guardian.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThreatListsTest {
    @Test
    fun seniorGetsCoreAndScamListsButNotLockdown() {
        val tags = ThreatLists.forPersona(Persona.SENIOR).map { it.engineTag }.toSet()
        assertEquals(setOf("IKY", "THG", "GDA", "SPR"), tags)
    }

    @Test
    fun powerUserGetsEverything() {
        assertTrue(ThreatLists.forPersona(Persona.POWER_USER).size == ThreatLists.all.size)
    }
}
