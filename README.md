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
| `--width <int>` | `200` | Width in tiles. Must be at least 84 to fit the pacing grammar. |
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

Our levels are in [`levels/npclrf/`](levels/npclrf/). The five current examples
use seeds 1 through 5, with a width of 200 and a height of 16. Regenerate them
with:

```bash
bash scripts/generate.sh --seed 1 --count 5 --width 200
```

<!-- Nic will add the final collection and its generation command. -->

## Reproducing the Experiments

<!-- Ryan owns the experiment instructions and results. -->

- TODO provide exact commands to regenerate the evaluation levels and run the evaluation harness.
- TODO identify the generated-level sample and original/framework levels used as baselines.
- TODO document agent version, starting state, time limits, repeat counts, and relevant machine/JDK details.
- TODO define each selected metric and its formula, including normalization for different level sizes.
- TODO explain where CSV files, summary tables, and plots are saved and how to regenerate them.
- TODO distinguish deterministic map output from variation in the agent's timed evaluation.

## Project Write-up

<!-- Keep the completed narrative in this section to approximately 1–2 pages. -->

### Design Goal and Architecture

<!-- Caleb owns this subsection. -->

- TODO explain the intended player experience and the goals for fairness, pacing, reproducibility, and variety.
- TODO describe the pipeline from seed and parameters through grammar, chunks, validation, and level output.
- TODO explain how the generator integrates with the Mario AI Framework.

### Generation Technique

<!-- Nic owns this subsection, with Caleb contributing the grammar and assembler explanation. -->

- TODO describe the pacing grammar and provide a small example of its production rules.
- TODO explain chunk types, parameters, selection, and boundary constraints.
- TODO describe the reachability model, movement calibration, regeneration, and retry limit behavior.
- TODO distinguish the checker's assumptions from evidence obtained through engine playthroughs.
- TODO cite the relevant course lectures and any external techniques actually used.

### Evaluation and Results

<!-- Ryan owns this subsection. -->

- TODO summarize the experiment design and compare generated levels with the baselines.
- TODO include the measured results table and any plots that help explain it.
- TODO interpret playability, variety, retry counts, and other selected measurements against the design goals.

### Limitations and Lessons Learned

<!-- Each teammate contributes findings from their work. -->

- TODO discuss observed failure cases, repetition, checker limitations, and agent evaluation variability.
- TODO explain what worked, what changed during development, and what the team learned.

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
