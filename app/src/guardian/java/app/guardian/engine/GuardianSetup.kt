package app.guardian.engine

import android.content.Context
import app.guardian.shared.Persona
import app.guardian.shared.ThreatLists
import com.celzero.bravedns.BuildConfig
import com.celzero.bravedns.data.AppConfig
import com.celzero.bravedns.download.AppDownloadManager
import com.celzero.bravedns.service.PersistentState
import com.celzero.bravedns.util.Logger
import com.celzero.bravedns.util.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Applies a persona's protection settings once protection is on:
 * Quad9 as the upstream DNS, on-device threat lists, and (debug builds only) a test scam list.
 * Safe to call repeatedly; each step checks whether it is already done.
 */
object GuardianSetup : KoinComponent {

    private const val TAG = "GuardianSetup"
    private const val QUAD9_DOH = "https://dns.quad9.net/dns-query"

    private val persistentState by inject<PersistentState>()
    private val appConfig by inject<AppConfig>()
    private val downloads by inject<AppDownloadManager>()

    /** Spike-only: live phishing domains missing from the bundled lists, to prove our own feed works. */
    private val testScamDomains = listOf("allmart-ai.com")

    /** Runs on a background thread: the engine's settings live in a database Room won't touch on the main thread. */
    suspend fun apply(context: Context, persona: Persona = Persona.SENIOR) = withContext(Dispatchers.IO) {
        useQuad9()
        if (!ensureListsDownloaded(context)) return@withContext
        enableLists(persona)
        if (BuildConfig.DEBUG) testScamDomains.forEach { GuardianEngine.blockDomain(it) }
    }

    private suspend fun useQuad9() {
        val current = appConfig.getDOHDetails()
        if (appConfig.getDnsType() == AppConfig.DnsType.DOH && current?.dohURL == QUAD9_DOH) return
        val quad9 = appConfig.getAllDefaultDoHEndpoints().firstOrNull { it.dohURL == QUAD9_DOH }
        if (quad9 == null) {
            Logger.w(TAG, "Quad9 DoH endpoint missing from the database")
            return
        }
        quad9.isSelected = true
        appConfig.handleDoHChanges(quad9)
    }

    /** Lists come from the upstream download server until we host our own (see spike report). */
    private suspend fun ensureListsDownloaded(context: Context): Boolean {
        if (hasLists(context)) return true
        downloads.downloadLocalBlocklist(persistentState.localBlocklistTimestamp, isRedownload = false, forceInApp = true)
        repeat(60) { // up to ~2 minutes
            delay(2_000)
            if (hasLists(context)) return true
        }
        Logger.w(TAG, "on-device lists not available after download attempt")
        return false
    }

    private fun hasLists(context: Context): Boolean {
        val ts = persistentState.localBlocklistTimestamp
        return ts > 0 && Utilities.hasLocalBlocklists(context, ts)
    }

    private suspend fun enableLists(persona: Persona) {
        // the engine loads the files when this flips on; the stamp encoder needs them loaded
        persistentState.blocklistEnabled = true
        val lists = ThreatLists.forPersona(persona)
        repeat(10) {
            if (GuardianEngine.useLists(lists)) return
            delay(1_000)
        }
        Logger.w(TAG, "could not apply on-device lists for $persona")
    }
}
