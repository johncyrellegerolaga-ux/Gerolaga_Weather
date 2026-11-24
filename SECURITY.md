# Security Policy

## Supported versions

This is a demo application; only the `main` branch receives fixes.

## Reporting a vulnerability

Please do not open a public issue for security problems. Use GitHub's private
reporting instead:

1. Go to the **Security** tab of this repository.
2. Click **Report a vulnerability**.
3. Describe the issue, the impact, and the steps to reproduce it.

You can expect an initial response within a few days.

## Secret handling

This project needs one credential: an OpenWeatherMap API key.

**How it is supplied**

| Environment | Where the key lives |
|-------------|---------------------|
| Local development | `OPENWEATHER_API_KEY` in `local.properties` (git-ignored) |
| CI / other machines | `OPENWEATHER_API_KEY` environment variable |

`app/build.gradle.kts` reads the key from `local.properties` first, then falls
back to the environment variable, and injects it as `BuildConfig.OPENWEATHER_API_KEY`.

**What must never be committed**

- `local.properties`, `.env*`, `secrets.properties`, `keystore.properties`
- `*.jks` / `*.keystore` signing material
- Any literal API key in source, resources, tests, or documentation

All of the above are listed in `.gitignore`. If you add a new file that holds a
credential, add it there in the same change.

### If a key is exposed

A key pasted into a chat, a log, a screenshot, an issue, or a commit must be
treated as compromised, even if the commit is later deleted — Git history and
forks keep it alive.

1. **Revoke** the key at <https://home.openweathermap.org/api_keys>.
2. **Issue** a new key.
3. **Update** `local.properties` (or the CI secret) with the new value.
4. **Rebuild** and confirm the app still fetches weather.
5. **Rotate again** if the old key ever produced unexpected quota usage.

Deleting the line from the working tree alone is not enough; rotation is the
only step that actually removes access.

### Recommended repository settings

For public repositories, GitHub's native protections cover this project's
needs without extra tooling:

- **Settings → Code security → Secret scanning**: enable (free for public repos).
- **Settings → Code security → Push protection**: enable, so a commit containing
  a recognised key is rejected before it reaches the remote.
- **Settings → Code security → Dependabot alerts**: enable for dependency CVEs.

## Notes for this app

The API key is compiled into `BuildConfig` and therefore extractable from a
distributed APK. That is acceptable for a demo that ships the developer's own
free-tier key, but a production app should proxy requests through a backend so
no key ships with the binary.
