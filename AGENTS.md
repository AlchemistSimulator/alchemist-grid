# AGENTS.md

Guidance for AI coding agents working on this repository.

## What this is

`alchemist-grid` is a library that runs batches of [Alchemist](https://github.com/AlchemistSimulator/Alchemist)
simulations on an [Apache Ignite](https://ignite.apache.org/) cluster.
It is a single Gradle module that mixes Java (most of the code) and Kotlin (the launchers).

## Commands

| Goal | Command |
|---|---|
| Full build: compile, tests, static analysis | `./gradlew build` |
| Tests only | `./gradlew test` |
| Static analysis only | `./gradlew check -x test` |
| Fix Kotlin formatting | `./gradlew ktlintFormat` |
| Dry-run the CI publication | `./gradlew publishAllPublicationsToProjectLocalRepository` |

Before you finish a change, run `./gradlew build` and make sure it passes.
CI runs it on Linux, macOS, and Windows.

## Layout

```
src/main/java/it/unibo/alchemist/boundary/grid/
  cluster/      Cluster, WorkerSet: connect to Ignite and pick the nodes that run the jobs
  config/       GeneralSimulationConfig (shared by the whole batch: loader, end time/step,
                remote dependency files) and SimulationConfig (one variable assignment)
  simulation/   SimulationSet, RemoteSimulation (the Ignite job), RemoteResult, Complexity
  util/         WorkingDirectory: temporary directory that holds the dependency files on workers
  exceptions/
src/main/kotlin/it/unibo/alchemist/boundary/launchers/
  DistributedExecution.kt   launcher that submits a batch (Ignite client)
  IgniteServerLauncher.kt   launcher that starts a worker (Ignite server)
src/test/      JUnit 5 (Java) and Kotest (Kotlin) tests; YAML fixtures in src/test/resources/config
```

Alchemist finds the launchers by their simple class name in the simulation YAML
(`launcher: type: DistributedExecution`), so keep them in the
`it.unibo.alchemist.boundary.launchers` package and don't rename them.

## Toolchain and dependencies

- The JVM target is set by `multiJvm { jvmVersionForCompilation = 21 }` in `build.gradle.kts`.
  It must stay at or above what the Alchemist dependency requires, or dependency resolution fails.
- All versions live in `gradle/libs.versions.toml`. Renovate updates them automatically;
  don't bump versions unless that is the task.
- Alchemist often breaks its API across major versions (examples so far: `cartesianProductOf`
  return types, `Simulation.getError()` changing from `java.util.Optional` to Arrow `Option`).
  After an Alchemist upgrade, read the compiler errors and adapt at the call sites.
  To check the new API, look at the `-sources.jar` files under `~/.gradle/caches/modules-2/files-2.1/it.unibo.alchemist/`.

## Conventions

- Kotlin compiles with `allWarningsAsErrors = true`: fix warnings, don't suppress them broadly.
- Code must pass ktlint and detekt (Kotlin) and the Java checks from the Kotlin QA plugin.
  SpotBugs findings that are intentional are suppressed with `@SuppressFBWarnings`
  plus a `justification`.
- Java: `final` parameters and locals, Javadoc on public members, `Objects.requireNonNull` on constructor inputs.
- Everything sent to Ignite workers (configs, jobs, results, variable values) must be `Serializable`.
- Keep the existing license header at the top of new source files.
- Commits must follow Conventional Commits (`feat:`, `fix:`, `chore:`, `docs:`, ...).
  A Git hook checks this and runs `ktlintCheck` before each commit.
  Versioning and releases come from these messages via semantic-release.
- Never edit `CHANGELOG.md` by hand: semantic-release generates it.

## Things to watch out for

- Ignite tests need real network discovery. Don't add tests that start an Ignite cluster
  unless they can run reliably on all three CI operating systems.
- `.junie/` holds another agent's notes; ignore it.
