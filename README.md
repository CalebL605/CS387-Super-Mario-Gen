# CS387 Super Mario Level Generation

This is a project for the CS387 course at Drexel University. The goal of this project is to generate levels for a Super Mario Framework using procedural content generation techniques.

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

- TODO document the agreed Java target and supported JDK versions.
- TODO explain Git Bash setup on Windows and shell requirements on macOS/Linux.

### Build

- TODO provide clone and build commands, run from the repository root.
- TODO explain where compiled classes are written and how to verify the build.

## Generating Levels

- TODO provide commands for generating one level and a batch of levels.
- TODO document the seed, count, width, and output options, including defaults and valid ranges.
- TODO explain output filenames and how batch seeds map to individual files.
- TODO describe how to reproduce identical output with the same seed and parameters.

## Playing and Watching Levels

- TODO provide commands for playing a saved level manually and watching the agent play it.
- TODO document controls, level selection, Mario's starting state, and the game time limit.

## Generated Level Collection

- TODO link to the team's output folder once the level files are available.
- TODO document the command, seeds, and parameters used to generate the submitted collection of at least 20 levels.
- TODO show a few example levels with screenshots, seeds, and short descriptions of their differences.

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

- TODO credit the Mario AI Framework and its authors, linking to the original documentation above.
- TODO identify the reused Robin Baumgarten agent and any other reused code, libraries, or assets.
- TODO list the course slide citations and external references used in the write-up.
- TODO retain applicable attribution and usage notices from the framework.
