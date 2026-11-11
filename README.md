# Alchemist Grid

Distributed execution of [Alchemist](https://alchemistsimulator.github.io/) simulation batches
on a cluster of machines, based on [Apache Ignite](https://ignite.apache.org/).

Alchemist Grid provides two Alchemist launchers:

- **`IgniteServerLauncher`** turns a machine into a worker node of an Ignite cluster;
- **`DistributedExecution`** computes the Cartesian product of the chosen simulation variables
  and sends one simulation per combination to the worker nodes,
  together with any files the simulations need.

## Requirements

- Java 21 or newer (required by Alchemist 49+)
- An [Apache Ignite](https://ignite.apache.org/docs/latest/) XML configuration file
  that lets the nodes discover each other.
  On recent JVMs, Ignite may need extra `--add-opens` JVM flags;
  see the [Ignite documentation](https://ignite.apache.org/docs/latest/).

## Installation

The module is published on Maven Central as `it.unibo.alchemist:alchemist-grid`.
Add it next to Alchemist:

```kotlin
dependencies {
    implementation("it.unibo.alchemist:alchemist:<alchemist-version>")
    implementation("it.unibo.alchemist:alchemist-grid:<alchemist-grid-version>")
}
```

## Usage

### 1. Start the worker nodes

On each machine that will run simulations, start Alchemist with a simulation file
that selects the server launcher:

```yaml
incarnation: protelis
launcher:
  type: IgniteServerLauncher
  parameters: [path/to/ignite-config.xml]
```

If no configuration path is given, Ignite uses its default configuration.

### 2. Submit a batch

On the submitting machine, write the simulation as usual and select `DistributedExecution`:

```yaml
incarnation: sapere

variables:
  seed: &seed
    min: 0
    max: 9
    step: 1
    default: 0

launcher:
  type: DistributedExecution
  parameters:
    variables: [seed]                                  # variables to run a batch over
    distributedConfigPath: path/to/ignite-config.xml   # Ignite client configuration

# Files the simulations need at runtime. They are shipped to every worker
# and put in a temporary working directory on the classpath.
remote-dependencies:
  - path/to/some/resource.txt
```

The submitter joins the cluster as an Ignite *client*. It picks the server nodes that have
enough heap for the batch's estimated memory needs and sends them one job per
combination of the selected variables.
Variable values must be `java.io.Serializable`, since they are sent over the network.
Each simulation runs until its environment terminates (or forever, if it never does),
so make sure your simulations define appropriate terminators.

## Building from source

```bash
./gradlew build
```

This compiles the project and runs the tests and the static analysis checks
(ktlint, detekt, and the other checks from the
[Kotlin QA plugin](https://github.com/DanySK/gradle-kotlin-qa)).

## Contributing

Commit messages must follow [Conventional Commits](https://www.conventionalcommits.org/):
releases and the [changelog](CHANGELOG.md) are generated automatically by
[semantic-release](https://semantic-release.gitbook.io/).
A Git hook installed by the build checks commit messages and runs `ktlintCheck` before each commit.

## License

Alchemist Grid is part of the Alchemist project and is distributed under the terms of the
GNU General Public License, with a linking exception.
