# Agent & CI/CD Guidelines (Semantic Release)

This document describes the automated Release / Pre-release CI/CD workflow and defines the mandatory commit conventions for developers and AI agents working on the AutoSms repository.

---

## 1. Workflow Overview

This repository uses **GitHub Actions** along with **Semantic Release** (`semantic-release`) to automate 100% of the release lifecycle:

1. **Trigger**:
   - Pushes / merges to `main` or `master`: Generates an official **Release** (e.g., `v1.0.0`, `v1.1.0`).
   - Pushes / merges to `dev` or `beta`: Generates a **Pre-release** (e.g., `v1.1.0-beta.1`).
2. **Semantic Release Execution**:
   - Scans commits since the previous release tag.
   - Parses commit messages following the **Conventional Commits** specification to determine the next SemVer bump.
   - Generates release notes.
   - Calculates the Android `versionCode` dynamically using the formula:
     `MAJOR * 10000 + MINOR * 1000 + PATCH * 100 + CHANNEL_OFFSET`
     where Alpha is `1..19`, Beta is `20..89`, and Official release is `90` (e.g., `1.0.1` -> `10190`, `1.0.1-beta.1` -> `10120`, `1.0.1-alpha.1` -> `10101`).
   - Executes `./gradlew :app:syncUpdateJson -PVERSION_NAME=...` to automatically update `update.json` with the new version code, version name, download URL, and release notes.
   - Builds and signs the release APK via `./gradlew assembleRelease -PVERSION_NAME=...`.
   - Commits the updated `update.json` back to the repository (`[skip ci]`).
   - Pushes a new Git Tag.
   - Publishes a **GitHub Release** (or Pre-release) and attaches the signed release **APK** (`AutoSms-v${version}.apk`).

---

## 2. Commit Conventions (Conventional Commits)

For Semantic Release to calculate the next version correctly, **all commits must strictly adhere to the Conventional Commits specification**:

```text
<type>(<scope>): <short description in imperative mood>

[optional body: detailed explanation of motivation and changes]

[optional footer: breaking changes or issue references]
```

### Supported Types & Version Impacts:

| Type | Meaning | SemVer Impact | Example |
| :--- | :--- | :--- | :--- |
| **`fix`** | Bug fix | **PATCH** (`1.0.0` -> `1.0.1`) | `fix(sms): handle null pointer when parsing otp` |
| **`feat`** | New feature | **MINOR** (`1.0.0` -> `1.1.0`) | `feat(ui): add dark mode toggle` |
| **`BREAKING CHANGE`** (or `!`) | Breaking API/behavior changes | **MAJOR** (`1.0.0` -> `2.0.0`) | `feat(api)!: migrate to new backend schema` |
| `perf` | Performance improvement | PATCH (configurable) | `perf(db): optimize sms query speed` |
| `docs` | Documentation only | No version bump | `docs: update AGENT.md guidelines` |
| `refactor` | Code restructuring without fixing bugs or adding features | No version bump | `refactor: simplify permission handler` |
| `test` | Adding or updating tests | No version bump | `test: add unit test for SmsReceiver` |
| `chore` / `ci` | Build process, CI/CD changes, dependency updates | No version bump | `ci: update semantic-release workflow` |

> ⚠️ **CRITICAL INSTRUCTIONS FOR AI AGENTS:**
> - Never use vague or arbitrary commit messages like `"update code"`, `"fix bug"`, or `"wip"`.
> - If your change introduces a user-facing feature or bug fix that requires a new release, you **must** use `feat:` or `fix:`.
> - If your changes are internal, refactorings, or documentation updates that should not trigger a release, use `chore:`, `refactor:`, or `docs:`.

---

## 3. Required GitHub Secrets

To ensure the build and signing steps succeed in GitHub Actions, configure the following secrets under **Repository Settings > Secrets and variables > Actions**:

- `KEYSTORE_BASE64`: Base64-encoded string of the keystore file (`.jks` or `.keystore`) (e.g., `base64 -w 0 your_keystore.jks`).
- `KEYSTORE_PASSWORD`: Keystore password.
- `KEY_ALIAS`: Signing key alias.
- `KEY_PASSWORD`: Key alias password.
- `GITHUB_TOKEN`: Provided automatically by GitHub Actions runner with `contents: write` permissions.

---

## 4. Configuration Files

- `.releaserc.json`: Configures Semantic Release branches (`main`/`master` for releases, `dev`/`beta` for pre-releases) and assets distribution.
- `.github/workflows/release.yml`: GitHub Actions pipeline that builds, signs the APK, and invokes `semantic-release`.

---

## 5. Scope & Limitations

- **In-Scope:** Building APK, automated signing on CI, semver tagging, changelog generation, and GitHub Release / Pre-release asset publishing.
- **Out-of-Scope:** Direct deployment to Google Play Console or Firebase App Distribution (can be added later if needed).
