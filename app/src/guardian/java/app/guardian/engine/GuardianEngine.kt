package app.guardian.engine

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.LiveData
import app.guardian.shared.ThreatList
import com.celzero.bravedns.service.BraveVPNService
import com.celzero.bravedns.service.DomainRulesManager
import com.celzero.bravedns.service.FirewallManager
import com.celzero.bravedns.service.PersistentState
import com.celzero.bravedns.service.RethinkBlocklistManager
import com.celzero.bravedns.service.VpnController
import com.celzero.bravedns.util.Constants.Companion.UID_EVERYBODY
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The only door from Guardian code into the RethinkDNS engine. Guardian UI and features call
 * this and never the engine classes directly, so upstream changes land in one file.
 */
object GuardianEngine : KoinComponent {

    private val persistentState by inject<PersistentState>()

    enum class Protection { OFF, STARTING, ON, PAUSED }

    /** Non-null means the system VPN consent dialog must be shown first (startActivityForResult). */
    fun consentIntent(context: Context): Intent? = VpnService.prepare(context)

    fun start(context: Context) = VpnController.start(context)

    /** Helper-only in the senior persona. */
    fun stop(context: Context, reason: String) =
        VpnController.stop(reason, context, userInitiated = true)

    fun protection(): Protection {
        val s = VpnController.state()
        return when {
            !s.on && s.activationRequested -> Protection.STARTING
            !s.on -> Protection.OFF
            s.connectionState == BraveVPNService.State.PAUSED -> Protection.PAUSED
            else -> Protection.ON
        }
    }

    /** Fires on every engine state change; call [protection] to read the new state. */
    val changes: LiveData<BraveVPNService.State?> get() = VpnController.connectionStatus

    /**
     * Turns on exactly these on-device lists. The engine applies the change live.
     * Needs the list files downloaded first, and the VPN running (the encoder lives in the engine).
     * Returns false if the engine could not encode the selection.
     */
    suspend fun useLists(lists: Collection<ThreatList>): Boolean {
        val values = lists.map { it.engineValue }.toSet()
        if (values.isEmpty()) {
            persistentState.blocklistEnabled = false
            persistentState.localBlocklistStamp = ""
            persistentState.numberOfLocalBlocklists = 0
            return true
        }
        val stamp = RethinkBlocklistManager.getStamp(
            values, RethinkBlocklistManager.RethinkBlocklistType.LOCAL
        )
        if (stamp.isBlank()) return false
        persistentState.blocklistEnabled = true
        persistentState.localBlocklistStamp = stamp
        persistentState.numberOfLocalBlocklists = values.size
        return true
    }

    /** Blocks a domain in every app, e.g. a scam domain pushed by our own feed or the helper. */
    suspend fun blockDomain(domain: String, includeSubdomains: Boolean = true) =
        DomainRulesManager.addDomainRule(domain, DomainRulesManager.Status.BLOCK, domainType(includeSubdomains), UID_EVERYBODY)

    /** Unblocks a domain in every app, e.g. after the helper approves an unblock request. */
    suspend fun trustDomain(domain: String, includeSubdomains: Boolean = false) =
        DomainRulesManager.addDomainRule(domain, DomainRulesManager.Status.TRUST, domainType(includeSubdomains), UID_EVERYBODY)

    /** Lets one app through the firewall for a while, e.g. a helper-approved request. */
    suspend fun allowAppFor(uid: Int, minutes: Int) =
        FirewallManager.updateTempAllowStatus(uid, minutes)

    private fun domainType(includeSubdomains: Boolean) =
        if (includeSubdomains) DomainRulesManager.DomainType.WILDCARD else DomainRulesManager.DomainType.DOMAIN
}
