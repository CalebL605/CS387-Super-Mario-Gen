# Project 4 Plan: Team NPCLRF

> **Status: PROPOSED.** Comment on the PR that adds this file. Merging that PR means the team agrees.
> **To raise a concern** later, comment on the relevant GitHub issue, or message the group chat the same day you notice it.

Team **NPCLRF**: Caleb (`CalebL605`), Nic (`nicolasplaia`), Ryan (`rfitzpatrick517`).
**Internal deadline: Sun Nov 29, 11:59 PM.** The presentation may be Mon Nov 30; the official due date is Tue Dec 1.

Slide citations like `[PCG II slide 47]` mean slide 47 of the "PCG II" lecture PDF on Canvas (slide 1 is the title slide).

---

## 1. Facts that shape the plan (checked Nov 18)

| Fact | Why it matters |
|---|---|
| The framework already defines `MarioLevelGenerator` (`getGeneratedLevel(MarioLevelModel, MarioTimer)` returning a level `String`). `MarioLevelModel` has char constants for every tile plus `setBlock`, `setRectangle`, `copyFromString` and `getMap`. | Our generator should implement this interface. That covers "can be loaded with the engine" for free. |
| The original levels are **16 rows tall and 149–373 columns wide** (median ~190). `GenerateLevel.java` uses 150×16. | Match 16 rows. A width around 150–200 emulates the originals. |
| The built-in generators call `new Random()` **with no seed**. | They aren't reproducible. Ours must take an explicit seed. |
| The robinBaumgarten A* agent plays a level headless in **~0.4–6 s**. It wins **12 of 15** original levels (it loses 3, 6 and 10 at 42–53%), 10/10 `notch` levels and 6/10 `ore` levels (60 s timer, small Mario). | Running the agent over hundreds of levels is cheap. But "A* loses" doesn't mean "unplayable", because it even fails some real levels. |
| **The A* agent is not fully deterministic.** Rerunning lvl-2 gave a different finish time, because its search is limited by wall-clock time. | If an agent playthrough decides which level gets saved, **the same seed could produce different levels on different machines**, which breaks "reproducible". See D3. |
| `javac -d out $(find src -name '*.java')` builds the whole framework from the repo root, without IntelliJ. The engine loads `img/` and `levels/` by relative path. | The README can give a no-IntelliJ setup: build, then run from the repo root. |
| The assignment's quality words come from the lecture's Empire Earth slide `[PCG II slide 58]`: **fair**, **predictable** ("similar by type, allow for different outcomes"), **reproducible** ("seed & type & size"), **varied**, **relatively quick**. | These definitions can be quoted in the write-up and turned into checks. |

---

## 2. Decisions

Each decision has a **recommended default**. Reply "approve" or object in the PR or the group chat.

### Blocking: needed before Caleb starts (target: agreed by Fri Nov 20)

**D1. Technique.**
- **Recommended: hybrid "constructive + generate & test".**
  1. A **pacing grammar** lays out the level as a sequence of segments with a difficulty curve: a safe start, introduce a hazard, repeat it, combine hazards, a harder final stretch, then the goal. This is the kihon → kata → kumite structure `[PCG II slides 32–35]`.
  2. Each segment is filled from a **library of parameterized chunks** modeled on the original levels: flat runs, gaps, pipes, staircases, brick and `?` rows, floating platforms, enemy groups. This is 1D Spelunky-style tiling, with edge constraints on ground height at the boundaries `[PCG I slides 70–84]`.
  3. A **deterministic static playability checker** verifies the result. A failing segment is regenerated with the next random draw, within a bounded number of retries. This is the lecture's generate & test with "verify that a path exists from start to goal" `[PCG II slides 46–47]`.
- **Why:**
  - It's the best-covered option in lecture.
  - Playability comes mostly by construction, with a checker as a safety net.
  - It's controllable: difficulty and pacing make a clear design goal for the presentation.
  - It splits cleanly into three parts.
- **Alternative: pattern-based Markov chain** over level columns, trained on `levels/original/` `[PCG I slide 38]`, plus the same checker and repair.
  - Pros: it emulates the originals' style automatically.
  - Cons: with only 15 training levels it tends to copy long stretches (weak variety) or produce broken structures (pipes cut in half, impossible gaps) that need heavy repair. Its core is also small, which makes an even three-way split harder.

**D2. Level size.** Recommended: **16 rows**, default **width 200**, configurable with `--width`.

**D3. What "playable" means, and where the agent is used.**
- **Recommended:**
  - **Inside the generator:** only the *deterministic* static checker, so the same seed always gives byte-identical output.
  - **Outside the generator:** the robinBaumgarten A* agent is used only in the **evaluation** (reported numbers), never to decide what gets saved.
- **Target:** 100% of saved levels pass the static checker, and **≥ 90% are A\* wins** over 100 seeds. For comparison, A* wins 80% of the original levels.

**D4. Reproducibility contract.** Recommended:
- One `java.util.Random(seed)`, passed down everywhere. No `Math.random()`, no `new Random()`, no wall-clock time.
- The same `(seed, width, params)` always gives the identical file, on any OS (see section 6).
- The submitted levels are `levels/npclrf/lvl-1.txt` … `lvl-N.txt`, generated from seeds 1…N by one documented command.

**D5. Where things live.** Recommended:
- Generator code: `src/levelGenerators/npclrf/`.
- Evaluation code: `src/levelGenerators/npclrf/eval/`.
- Output: `levels/npclrf/`.
- Write-up: `docs/writeup.md`.
- Scripts: `scripts/`.

**D6. The work split and checkpoints in sections 3 and 5.**

### Non-blocking: can be settled during the week

- **N1. Evaluation metrics.** Recommended:
  - A* win rate and completion %.
  - Static-check pass rate and retry count.
  - Classic level metrics from the framework's PCG-comparison paper (Horn et al. 2014, linked in `README.md`): **leniency** (enemies and gaps), **linearity** (height variance), **density**, and **variety** (pairwise tile distance between levels).
  - The same metrics on `levels/original` as a baseline.
- **N2. Git workflow.** Recommended:
  - One branch per task.
  - A PR into `main` with one teammate's approval.
  - Everyone commits under their own account (the professor tracks contributions).
  - No force-pushes to `main`.
- **N3. The write-up outline and who writes which section** (section 3 suggests owners).
- **N4. Presentation format** (Google Slides?) and **the rehearsal time** (Sun Nov 29).
- **N5. Communication.** Recommended: a group-chat post at each checkpoint, and blockers raised the same day.
- **N6. Java language level.** Recommended: plain Java 11-era features, so everyone's JDK compiles it. **Each person: post your `javac -version` in the group chat.**
- **N7. Evaluation settings.** Recommended: small Mario (`marioState 0`), 60 s timer, no visuals.

---

## 3. Work breakdown

This assumes D1 = the recommended hybrid. It's ordered by each person's schedule, so the earliest work unblocks the rest.

### Caleb: "Skeleton & structure" (Wed Nov 18 – Sun Nov 22)
1. **Line-ending setup and scripts:**
   - Add `.gitattributes` (section 6).
   - Add build and run scripts (`scripts/build.sh`, `scripts/generate.sh`) that work in Git Bash on Windows and in macOS/Linux shells.
   - Confirm a clean clone builds without IntelliJ.
2. **Generator skeleton:**
   - `levelGenerators.npclrf.LevelGenerator implements MarioLevelGenerator`, with a `(long seed)` constructor.
   - A CLI `Generate` (`--seed`, `--count`, `--width`, `--out`) that writes `lvl-N.txt`.
   - A `PlayGenerated` class to watch a level, for demos.
3. **The chunk interface** (contract in section 4), plus **3–4 starter chunks** (flat, simple gap, pipe, single enemy), so the pipeline runs end to end.
4. **The pacing grammar / assembler:**
   - Segment sequence and difficulty curve.
   - A safe start zone with `M`, an end zone with `F`.
   - Matching ground height at chunk boundaries.
5. **README:**
   - Setup and build without IntelliJ (including Git Bash on Windows).
   - How to run the generator and watch a level.
   - The third-party code list (the Mario AI Framework, its robinBaumgarten A* agent, anything else reused).
   - A placeholder heading, "Reproducing the experiments", for Ryan to fill in.

   Written this weekend, while the build steps are fresh.
6. **The write-up file:** create `docs/writeup.md` with every section heading and its owner marked (see "Shared" below). There's no separate assembly step later; each person fills in their own section.
7. **Write-up and slides:** the "Design goal & architecture" section.

**Done when:**
- `scripts/generate.sh --seed 1` writes a level that loads and plays in the engine, and the same seed twice gives identical files.
- A teammate can follow the README from a clean clone to a playable level.
- `docs/writeup.md` has its section headings in place.

### Nic: "Content & correctness" (Mon Nov 23 – Wed Nov 25)
1. **The full chunk library:**
   - About 12–15 chunk types modeled on `levels/original/`: gaps of varying width, pipe runs (with and without piranhas), staircases, brick/`?`/coin rows, floating platforms, cannon/bullet bills, enemy groups (goombas, koopas, winged).
   - Each is parameterized and tagged with a difficulty.
2. **Mario's physical limits, measured** (max jump height, max gap width from running and standing). Use the engine itself; this sets the constraints chunks and the checker must respect.
3. **The static playability checker:**
   - Deterministic reachability from `M` to `F` over the tile grid using those jump limits. This is a platform graph plus BFS/A* `[Movement I slide 19]`.
   - Returns pass/fail plus the first failing column.
4. **The generate & test loop:** wire the checker into the assembler, regenerating a failing segment with bounded retries.
5. **The final level set,** at the freeze:
   - Run the documented command to generate `levels/npclrf/lvl-1..lvl-20` (or more) from seeds 1…N, and commit them.
   - Take 3–4 screenshots of example levels for the slides and write-up.
6. **Write-up and slides:** the "Technique" section (chunks, grammar, generate & test), with lecture citations.

**Done when:** 100 seeds all pass the checker with no infinite retry loops, and the 20+ levels and screenshots are committed.

**The generator is frozen Wed Nov 25; after that, only bug fixes.** This is the most important date in the plan, because Ryan's final experiment runs depend on it.

### Ryan: "Evaluation" (harness code Mon Nov 23 – Wed Nov 25; final runs and writing Thu Nov 26 – Sat Nov 28)
1. **The evaluation harness.** This doesn't depend on our generator, so it **can be built and tested on `levels/original` and `levels/notch` before Thanksgiving.**
   - It runs the A* agent headless over a folder of levels.
   - It computes the N1 metrics.
   - It writes a CSV and a summary table.
2. **Experiments,** run after Nic's Wed Nov 25 freeze:
   - **Required minimum:** one table comparing our generator over 100 seeds with `levels/original` (and `levels/notch`): A* win rate, completion %, and three metrics (e.g. leniency, linearity, density).
   - **Stretch:** a difficulty-curve check (e.g. A* hurts or deaths by segment) and one or two plots.
3. **README:** fill in the "Reproducing the experiments" section that Caleb stubs out, with the exact commands that regenerate the CSV and table.
4. **Write-up and slides:** the "Results" section and the results and example-levels slides, using Nic's screenshots.

**Done when:**
- The harness runs on `levels/original` (target: by Wed Nov 25).
- The README's experiment commands reproduce the results table from a clean clone.

**Nothing required for submission is waiting on Ryan's final weekend.** Only the results numbers and their write-up land then.

### Shared
- **The write-up (`docs/writeup.md`):**
  - Each person writes their own section directly in the file Caleb creates. There's no separate assembly step.
  - All three proofread it together at the Sunday review, and trim it to 1–2 pages.
- **"What we learned":** each person writes 2–3 bullets. These close the write-up and the talk.
- **Presentation (4–5 min, about 1.5 min each):**
  - Caleb: the design goal and architecture.
  - Nic: the technique and how playability is guaranteed.
  - Ryan: example levels and results.
  - Everyone: one learning each.
- **Rehearse Sun Nov 29 with a timer.**

---

## 4. Interfaces (contracts between people)

Caleb finalizes these by **Sat Nov 21**, so Nic and Ryan can code against them.

```java
// Caleb: generator entry point (framework interface)
public class LevelGenerator implements MarioLevelGenerator {
    public LevelGenerator(long seed);            // all randomness flows from here
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer);
}

// Caleb defines, Nic fills in the library
public interface Chunk {
    String name();
    int difficulty();                            // e.g. 0 = rest, 1..3 = increasing
    int minWidth(); int maxWidth();
    // Writes into model columns [x, x+width). Must enter and exit at groundHeight
    // (or report its exit height). Uses only rnd for randomness.
    int place(MarioLevelModel model, int x, int width, int groundHeight, java.util.Random rnd);
}

// Nic
public final class PlayabilityChecker {
    public static Result check(String level);    // deterministic
    // Result: playable (boolean), failColumn (int), reason (String).
    // Use a small final class rather than a record unless everyone's JDK is 16+ (N6).
}

// Ryan
//   java -cp out levelGenerators.npclrf.eval.Evaluate --levels levels/npclrf --out results/npclrf.csv
//   CSV columns: level, status, completion, ..., leniency, linearity, density
```

---

## 5. Timeline and checkpoints

| Date | Checkpoint | Owner |
|---|---|---|
| Fri Nov 20 | D1–D6 agreed (this PR merged); GitHub issues created | All |
| Sat Nov 21 | Interfaces in section 4 merged to `main` | Caleb |
| **Sun Nov 22** | **End-to-end skeleton:** seed → loadable level, with starter chunks. README setup and run sections, and the `docs/writeup.md` headings | Caleb |
| **Wed Nov 25** | **Generator frozen:** full chunk library, checker, generate & test; 100 seeds pass. 20+ levels and screenshots committed | Nic |
| Wed Nov 25 | The eval harness runs on `levels/original` | Ryan |
| Thu Nov 26 | Thanksgiving: buffer, no checkpoint | — |
| Sat Nov 28 | Results table, README experiments section, every write-up section filled in, slides drafted | Ryan (results) + all (own sections) |
| **Sun Nov 29** | **Final review, timed rehearsal, submit the link on Canvas** | All |

**Risks:**
- **The checker disagrees with the game.** Mitigation: Nic calibrates the jump limits against the engine. Ryan's A* results act as a second opinion.
- **Ryan's window includes Thanksgiving.** Mitigations:
  - The harness code is due before Thanksgiving.
  - The README, the final levels and the write-up structure belong to Caleb and Nic.
  - The experiments have a fixed minimum, and the final run takes minutes.
- **The Wed Nov 25 freeze slips.** Every day it slips comes out of Ryan's window. Mitigation: Nic raises it by Tue Nov 24 if it's at risk, and the team cuts chunk types before moving the date.
- **Windows vs. macOS differences** (Caleb and Ryan are on Windows with Git Bash; Nic is on macOS). Mitigation: section 6, plus each checkpoint is checked on at least one Windows machine.

---

## 6. Cross-platform conventions (Windows + Git Bash, and macOS)

1. **Line endings are LF everywhere.** Add a `.gitattributes` at the repo root:
   ```
   * text=auto
   *.txt text eol=lf
   *.sh  text eol=lf
   *.java text eol=lf
   *.md  text eol=lf
   ```
   **Why:**
   - Git on Windows usually converts files to CRLF on checkout. A `.sh` script with CRLF fails in Git Bash.
   - `MarioLevelModel.copyFromString` splits on `"\n"` only, so a CRLF level read from disk leaves stray `\r` characters in a row.
   - The engine's own loader (`MarioLevel`) does handle CRLF.
2. **Write level files with `"\n"` explicitly.** Never use `println`, `System.lineSeparator()` or `BufferedWriter.newLine()` for level output. On Windows those produce `\r\n`, so the same seed would give different bytes on different OSes, which breaks D4.
3. **Classpath:** keep it to the single entry `-cp out`. The separator for multiple entries is `:` on macOS but `;` on Windows.
4. **Paths in Java:** use forward slashes (`levels/npclrf/lvl-1.txt`); they work on both. Run everything from the repo root.
5. **Scripts** are Bash, run from Git Bash on Windows. Git Bash includes `find`, so the build line works as is. Don't depend on tools Git Bash lacks (e.g. GNU `timeout` behavior, `python`).
6. **Checking reproducibility:** compare output with `cmp` or `git diff --exit-code` after regenerating. Both are available in Git Bash.
