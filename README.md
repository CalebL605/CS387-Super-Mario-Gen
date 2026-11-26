# CS387 Super Mario Level Generation

Our CS387 project at Drexel generates Super Mario levels using a pacing grammar
and reusable chunks (tiles). Each level starts with a safe area, builds up to
harder encounters, gives the player a break, and finishes with a final challenge.
The generator saves levels as text files that the Mario AI Framework can load.
You can play them yourself or watch the Robin Baumgarten A* agent play.

See the [original framework documentation](FRAMEWORK_README.md).

## Table of Contents

- [Setup](#setup)
- [Generating Levels](#generating-levels)
- [Playing and Watching Levels](#playing-and-watching-levels)
- [Generated Level Collection](#generated-level-collection)
- [Reproducing the Experiments](#reproducing-the-experiments)
- [Project Write-up](#project-write-up)
- [Team Contributions](#team-contributions)
- [Third-party Code and References](#third-party-code-and-references)

## Setup

### Requirements

- **JDK 25.** Install the full JDK so both `java` and `javac` are available.
  A newer JDK also works if it supports compiling with `--release 25`.
- **Git.** On Windows, use the Git Bash terminal included with Git for Windows
  for the commands below. On macOS or Linux, use your usual terminal with Bash
  installed.

### Build

```bash
git clone https://github.com/CalebL605/CS387-Super-Mario-Gen.git
cd CS387-Super-Mario-Gen
bash scripts/build.sh
```

The build script compiles everything in `src/` for Java 25 and writes the classes
to `out/`. It prints the number of compiled source files when it finishes.

## Generating Levels

Generate one level with the default settings:

```bash
bash scripts/generate.sh
```

This saves a 200-by-16-tile level to `levels/npclrf/lvl-1.txt` using seed 1.

To generate a batch:

```bash
bash scripts/generate.sh --seed 1 --count 5 --width 200
```

This writes `lvl-1.txt` through `lvl-5.txt`, using seeds 1 through 5.
The filenames always start at `lvl-1.txt`. For example, `--seed 100 --count 5`
uses seeds 100 through 104 but still names the files `lvl-1.txt` through
`lvl-5.txt`.

| Option | Default | What it controls |
| --- | --- | --- |
| `--seed <long>` | `1` | Seed for the first level. Accepts a signed 64-bit integer. |
| `--count <int>` | `1` | Number of levels to generate. Must be at least 1. |
| `--width <int>` | `200` | Width in tiles. Must be at least 68 to fit the pacing grammar. |
| `--out <path>` | `levels/npclrf` | Folder for the generated level files. |
| `--help`, `-h` | | Prints the available options. |

The height is fixed at 16 tiles. Each level in a batch uses the next consecutive
seed, and the whole seed range must fit in a Java `long`.

**Existing files with matching names are overwritten.** The generator creates
the output folder if needed and leaves other files in it alone. Use a different
folder to keep another batch.

With the same generator version, seed, and width, you get the same level file.
Changing the seed gives you another layout. The agent's runs can still differ
because its search uses a time limit.

### Checking Playability

The generator only saves levels that pass its static playability checker (see
the [write-up](docs/writeup.md#generation-technique)). Run the checker yourself
from the repository root:

```bash
bash scripts/check.sh levels/npclrf/*.txt
bash scripts/check.sh --seeds 1 100
```

The first command checks saved level files. The second generates seeds 1–100
twice each. It prints `seed,playable,reproducible,retries,fallbacks` as CSV,
then a summary line. Other options:

| Option | What it does |
| --- | --- |
| `--measured` | Checks files against the raw engine measurements instead of the design limits. |
| `--width <int>` | Level width for `--seeds` (default 200). |
| `--spot-check` | Runs hand-built levels with known answers. |
| `--chunks` | Places every chunk alone at every width it allows and checks each placement. |

The checker exits with status 3 if any level fails. Use
`java -cp out levelGenerators.npclrf.JumpProbe` to re-measure Mario's jump
limits in the engine. It exits with status 1 if they no longer match
`JumpLimits`.

## Playing and Watching Levels

Play a saved level yourself:

```bash
java -cp out levelGenerators.npclrf.Play --human --level levels/npclrf/lvl-1.txt
```

Watch the Robin Baumgarten A* agent play it:

```bash
java -cp out levelGenerators.npclrf.Play --agent --level levels/npclrf/lvl-1.txt
```

Change the path after `--level` to choose another file. If you leave out the
options, `Play` watches the agent on `levels/npclrf/lvl-1.txt`. Both modes start
as small Mario with 20 seconds on the game clock and open a desktop window.
The terminal prints the result when the run ends. Close the window to exit.

| Key | Action |
| --- | --- |
| Left / Right arrow | Move |
| Down arrow | Duck |
| S | Jump |
| A | Run, or shoot if Mario has firepower |

You can also use the starter [`src/GenerateLevel.java`](src/GenerateLevel.java)
to generate and play a level. This file can be modified to change the seed, width,
or other parameters as needed.

## Generated Level Collection

Our final collection is the 20 levels in [`levels/npclrf/`](levels/npclrf/),
generated from seeds 1 through 20 with a width of 200 and a height of 16. To
regenerate them byte for byte:

```bash
bash scripts/generate.sh --seed 1 --count 20 --width 200
```

Every level passes the playability checker:

```bash
bash scripts/check.sh levels/npclrf/*.txt
```

Screenshots of four examples are in [`docs/img/`](docs/img/):
[seed 1](docs/img/lvl-1.png), [seed 4](docs/img/lvl-4.png),
[seed 7](docs/img/lvl-7.png) and [seed 16](docs/img/lvl-16.png). Re-render one
with:

```bash
java -cp out levelGenerators.npclrf.Screenshot --level levels/npclrf/lvl-4.txt --out docs/img/lvl-4.png
```

## Reproducing the Experiments

<!-- Ryan owns the experiment instructions and results. -->

- TODO provide exact commands to regenerate the evaluation levels and run the evaluation harness.
- TODO identify the generated-level sample and original/framework levels used as baselines.
- TODO document agent version, starting state, time limits, repeat counts, and relevant machine/JDK details.
- TODO define each selected metric and its formula, including normalization for different level sizes.
- TODO explain where CSV files, summary tables, and plots are saved and how to regenerate them.
- TODO distinguish deterministic map output from variation in the agent's timed evaluation.

## Project Write-up

The 1–2 page write-up is in [`docs/writeup.md`](docs/writeup.md). It covers the
design goal and architecture, the generation technique, evaluation and results,
and limitations and lessons learned.

## Team Contributions

| Team member | Planned responsibilities |
| --- | --- |
| Caleb Liang (`CalebL605`) | Generator foundation, pacing grammar, starter chunks, build/run setup, and project documentation. |
| Nicolas Plaia (`nicolasplaia`) | Chunk library, movement calibration, playability checking, regeneration, and final level collection. |
| Ryan (`rfitzpatrick517`) | Evaluation harness, experiments, metrics, and results. |

<!-- Update this table to reflect actual contributions before submission. -->

## Third-party Code and References

This project builds on the Mario AI Framework provided with the assignment,
including its engine, assets, example levels, and Robin Baumgarten A* agent. See
[the original framework documentation](FRAMEWORK_README.md) for credits and usage
notices. We added no external Java libraries.

<!-- Edit this section if third-party code or references are added. -->
