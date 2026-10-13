# Oculomotor Tracking Assessment

A desktop application that measures how well you follow a moving on-screen target
with your cursor. It is a simplified, educational tool **inspired by** clinical
smooth-pursuit and saccadic eye-movement screening (in the spirit of tests such as
King-Devick), using the **mouse as a proxy for gaze** — no eye-tracking hardware
required.

> **Disclaimer:** This is a course project for educational and demonstration
> purposes only. It is **not** a clinical, medical, or diagnostic tool and makes
> no claim of clinical accuracy.

## Features

- **Two test modes**
  - **Smooth Pursuit** — the target follows a continuous figure-eight (Lissajous)
    path to probe steady tracking.
  - **Saccadic** — the target holds still, then jumps to random locations at
    randomized intervals to probe reaction time to sudden shifts.
- **3-2-1 countdown** before each run, with a fixed 20-second duration.
- **Live visuals** — the moving target, your cursor as a crosshair, and a short
  fading trail of recent target positions.
- **Results screen** with a headline metric and an expandable jump-by-jump table:
  - *Average deviation* (px) — overall tracking accuracy (mean cursor-to-target
    distance).
  - *Reaction latency* (ms) — for saccadic runs, how quickly you reacted to each
    jump (average, fastest, and slowest).

## Requirements

- **Java JDK 17 or newer** (developed and tested on a recent JDK). No external
  libraries — standard JDK only (`javax.swing`, `java.awt`, `java.util`).

## Build & Run

From inside the `Particle Simulator` directory:

```bash
javac -d out particlesim/Main.java particlesim/model/*.java particlesim/movement/*.java particlesim/analysis/*.java particlesim/ui/*.java
```

```bash
java -cp out particlesim.Main
```

Then pick a **Test type**, press **Start Test**, follow the blue target with your
mouse, and read your results. **Reset** clears the current run.

## Architecture

The code is organized by responsibility, so movement and analysis logic never live
inside the UI classes:

| Package | Responsibility |
|---|---|
| `particlesim` | `Main` — entry point / composition root; wires everything together with a `CardLayout` (test view ↔ results view). |
| `particlesim.model` | `Target` (the moving dot: position, radius, current movement strategy) and `World` (arena bounds + the target). |
| `particlesim.movement` | `MovementStrategy` interface with `SmoothPursuitPath` and `SaccadicPath` implementations; `TestType` maps a chosen test to its strategy. |
| `particlesim.analysis` | `TrackPoint` (one recorded sample), `Tracker` (records samples each frame), `MetricsEngine` (computes deviation and reaction-latency metrics after a run). |
| `particlesim.ui` | `TestPanel` (~60 fps animation + rendering), `ControlPanel` (test selection, start/reset), `ResultsPanel` (metrics + per-jump table). |

**Design pattern:** the Strategy pattern (`MovementStrategy`) makes the target's
motion interchangeable, which is what allows adding new test types without touching
the rest of the app.

## License

Released for educational use. See `LICENSE` if present, or treat as all-rights-reserved
coursework otherwise.
