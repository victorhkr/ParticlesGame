# ParticlesGame

Many particles with mass interact in a system with gravity.

## Overview

**ParticlesGame** is a JavaFX application that simulates the gravitational interaction between multiple particles. Each particle has mass and is affected by the forces from every other particle in the system, providing a visual and interactive demonstration of n-body gravitational physics.

## Features

- Real-time simulation of many particles with gravitational forces.
- Visualization using JavaFX.
- Adjustable parameters for experimentation (e.g., number of particles, initial conditions).
- Modular code for easy extension and experimentation.

## Getting Started

### Prerequisites

- Java 11 or higher (JavaFX compatible)
- JavaFX SDK (if using JDK versions that do not bundle JavaFX)
- (Optional) An IDE such as IntelliJ IDEA or Eclipse for easier project management.

### Project Structure

```
ParticlesGame/
├── src/
│   └── application/
│       ├── ParticlesGame.java      # Main JavaFX application
│       ├── Particle.java           # Particle data and physics
│       └── application.css         # Styles for the JavaFX UI
│   └── module-info.java            # Java module declaration
├── build.fxbuild                   # JavaFX build configuration (optional)
├── bin/                            # (May contain compiled classes)
├── .classpath, .project, .settings # Eclipse IDE config files
```

### Running the Project

#### Option 1: From the Command Line

1. **Compile:**
    ```sh
    javac --module-path /path/to/javafx/lib --add-modules javafx.controls -d bin src/module-info.java src/application/*.java
    ```
    Replace `/path/to/javafx/lib` with the path to your JavaFX SDK.

2. **Run:**
    ```sh
    java --module-path /path/to/javafx/lib --add-modules javafx.controls -cp bin application.ParticlesGame
    ```

#### Option 2: Using an IDE

- Import the project as a Java project.
- Configure the JavaFX library in your project settings.
- Run `ParticlesGame.java` as a Java application.

### Controls & Usage

- On launch, particles are displayed and start interacting via gravity.
- You may be able to adjust simulation parameters in the UI or in code (see comments in `ParticlesGame.java`).
- The look and feel can be changed via `application.css`.

## Implementation Notes

- **Gravitational Calculation:** Physics is handled in `Particle.java` and `ParticlesGame.java`. Each frame, the force on each particle is computed using Newton's law of universal gravitation.
- **Rendering & Animation:** The JavaFX framework is used for rendering the simulation and handling user interaction.
- **Styling:** All UI styling is in `application.css`.

## Customization

- To change the number or properties of particles, modify the relevant constants or initialization code in `ParticlesGame.java`.
- To adjust the appearance, edit `application.css`.

## License

This project is provided as-is for educational and demonstration purposes.

---

**Author:** [victorhkr](https://github.com/victorhkr)
