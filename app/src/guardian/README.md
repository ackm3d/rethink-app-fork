# Guardian flavour

Guardian is a senior-first safety app built on the RethinkDNS engine in this repo.
It follows the same pattern as the `tv` flavour: everything in `app/src/main` (the VPN
service, Firestack engine, database and rule managers) is reused, and this source set adds
its own launcher, UI and app id.

Rules for keeping upstream merges cheap:

- Our Android code lives under `app/src/guardian/` in the `app.guardian` package.
- Platform-neutral code (personas, list catalogue, later the backend client and pairing)
  lives in the `:shared` Kotlin Multiplatform module, so iOS and desktop apps can reuse it.
- Guardian code talks to the engine only through `app.guardian.engine.GuardianEngine`.
- Don't edit files under `app/src/main` or rename upstream packages. If the engine needs a
  change, keep it small and isolated so it can be offered upstream.
- `app/src/guardian/AndroidManifest.xml` mirrors `app/src/full/AndroidManifest.xml`;
  mirror upstream changes to that file here.

Build a debug APK:

    ./gradlew :app:assembleFdroidGuardianDebug
    ./gradlew :shared:jvmTest
