package app.guardian.shared

/** Add-on modules on top of core protection. */
enum class Module {
    CORE,
    SCAM_SHIELD,
    KIDS_FILTER,
    PRIVACY,
    GUARDIAN_LINK,
    LOCKDOWN,
    POWER_TOOLS,
}

/** Picked with one question at setup; decides which modules are on and how much UI is shown. */
enum class Persona(val modules: Set<Module>, val showSettings: Boolean) {
    CHILD(setOf(Module.CORE, Module.KIDS_FILTER, Module.GUARDIAN_LINK, Module.LOCKDOWN), showSettings = false),
    SENIOR(setOf(Module.CORE, Module.SCAM_SHIELD, Module.GUARDIAN_LINK), showSettings = false),
    EVERYDAY_ADULT(setOf(Module.CORE, Module.SCAM_SHIELD, Module.PRIVACY), showSettings = true),
    POWER_USER(Module.entries.toSet(), showSettings = true),
}
