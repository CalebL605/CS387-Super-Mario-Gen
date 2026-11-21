# Presentation Notes

## Caleb: Design Goal and Architecture

Draft for the live demo, with no slides. Aim for about 60 seconds and rehearse
it aloud with the game running before the presentation.

### Talk

We wanted to generate Mario levels with a clear rhythm. The player gets a safe
start, faces harder sections, takes a short break, and reaches a final challenge
before the flag.

Our pacing grammar chooses that structure. Reusable chunks provide gaps, pipes,
stairs, and cannon platforms. Each chunk has a difficulty label and a supported
width.

The generator takes a seed and a width, picks the chunks, and fits them into the
level. It places them from left to right, keeping the ground height consistent
where they join.

All random choices use one seeded generator. That lets us reproduce the same
level or try another layout by changing the seed. We save the result as a text
file for human play or agent evaluation.

Nic will cover the chunks and playability checks next.

### Lecture References

These support the [README write-up](../README.md#design-goal-and-architecture)
and do not need to be read aloud.

- Map qualities and reproducibility: [PCG II slide 58].
- Separating encounter order from layout: [PCG II slides 32–33].
- Building maps from reusable pieces: [PCG I slide 70].
- Planned generate-and-test validation: [PCG II slides 46–47].
