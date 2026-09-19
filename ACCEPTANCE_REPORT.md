# Farm Ledger MVP — Acceptance Verification Report

**Date:** 2026-09-19 10:08 WAT (Africa/Lagos)  
**Branch:** `main`  
**Repo:** `/workspace/farm-ledger`  
**Env:** `JAVA_HOME=/home/box/.jdks/jdk-17.0.20.1+1`, `ANDROID_HOME=/home/box/Android/Sdk`

## 1. Unit test results

Command: `./gradlew :app:testDebugUnitTest --rerun-tasks`

| Suite | Tests | Failures | Errors | Skipped | Result |
|-------|------:|---------:|-------:|--------:|--------|
| `DailySettlementLogicTest` | 7 | 0 | 0 | 0 | **PASS** |
| `WeeklyReviewLogicTest` | 2 | 0 | 0 | 0 | **PASS** |
| **Total** | **9** | **0** | **0** | **0** | **GREEN** |

### DailySettlementLogicTest (all passed)

- `settle_awardsFixedOnePoint_oncePerDay` — awards exactly `RewardRules.DAILY_GROWTH_POINTS` (1); same-day re-settle does not re-award
- `editDoesNotReAward_sameDayAlreadySettled` — after settle, edit/re-settle keeps growth points unchanged
- `amountDoesNotAffectRewards` — any amount yields fixed 1 point; amount is not an input to `settle`
- `noActivity_noAward` — no ledger activity → no award
- `clockPaused_pausesRewards_keepsProgress` — clock-back pause blocks awards
- `detectClockRegression_setsPause` — date regression sets `clockPaused`
- `streakMilestones_unlockSeeds` — streak seeds bonus (orthogonal to fixed daily point)

### WeeklyReviewLogicTest (all passed)

- `weeklyReward_capped` — weekly claim capped
- `weeklyReward_notDoubleClaim` — no double claim for same week

## 2. Acceptance rules — how verified

### Rule 1: Daily settlement / no-trade day awards only a fixed 1 growth point

**Status: PASS**

- Source (`DailySettlementLogic.kt`): awards `val points = RewardRules.DAILY_GROWTH_POINTS` only; constant defined as `1` in `RewardRules` (`Models.kt`).
- Activity gate is boolean `hasAnyLedgerActivity` (true for income/expense **or** no-trade-day mark). Amount and entry count are never parameters of the reward amount.
- Tests: `settle_awardsFixedOnePoint_oncePerDay`, `amountDoesNotAffectRewards`, `noActivity_noAward`.

### Rule 2: Editing/backfilling does not re-award; amount and entry count do not enter the reward formula

**Status: PASS**

- Source: if `progress.lastSettleDate == today`, settle returns `awarded = false` with message that editing will not re-grant points.
- `settle(...)` signature takes only `hasAnyLedgerActivity: Boolean` — no amount, no entry count.
- `growthPointsForAmount(amountMinor)` always returns the fixed constant (unused amount; documents independence).
- Tests: `editDoesNotReAward_sameDayAlreadySettled`, `amountDoesNotAffectRewards`, plus same-day double-settle check in `settle_awardsFixedOnePoint_oncePerDay`.
- Related: clock-back (`clockPaused` / `detectClockRegression`) pauses rewards without changing ledger data — covered by tests.

No code changes were required; existing logic and tests already encode both rules.

## 3. Debug APK

| Item | Value |
|------|-------|
| Command | `./gradlew :app:assembleDebug` |
| Build | **SUCCESS** |
| Absolute path | `/workspace/farm-ledger/app/build/outputs/apk/debug/app-debug.apk` |
| Size | 16,941,461 bytes (16.16 MiB) |

Note: `*.apk` is gitignored; the APK is a local build artifact only.

## 4. Remaining limits

- Emulator / device GUI may be unavailable in this environment; verification here is **unit-test + APK build**, not interactive UI smoke on an emulator.
- Instrumented / UI tests were not run.
- Debug APK is signed with the default Android debug keystore (not a release/Play signing key).

## 5. Install steps

### Android Studio

1. Open the project folder `/workspace/farm-ledger` (or clone the repo).
2. Ensure JDK 17 and Android SDK are configured (SDK path matches `local.properties` → `sdk.dir`).
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**, or run `./gradlew :app:assembleDebug`.
4. Use **Run** on a connected device/emulator, **or** drag-install  
   `app/build/outputs/apk/debug/app-debug.apk`.

### adb (device or emulator)

```bash
export JAVA_HOME=/home/box/.jdks/jdk-17.0.20.1+1   # or your JDK 17
export ANDROID_HOME=/home/box/Android/Sdk          # or your SDK
adb devices
adb install -r /workspace/farm-ledger/app/build/outputs/apk/debug/app-debug.apk
```

On another machine after cloning:

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Enable USB debugging (physical device) or start an AVD before `adb install`.

## 6. Summary (Traditional Chinese)

- **規則 1（每日結算／無交易日僅固定 1 成長點）：通過** — `DAILY_GROWTH_POINTS = 1`，單元測試綠燈。
- **規則 2（編輯／補登不重發；金額與筆數不進獎勵公式）：通過** — 同日已結算不重發；`settle` 不接受金額／筆數。
- 單元測試 9/9 通過；debug APK 已產出。
