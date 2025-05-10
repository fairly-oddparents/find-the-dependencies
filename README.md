# Find the Dependencies! 🔎
The repository contains the second assignment of the [Concurrent and Distributed Programming course](https://www.unibo.it/en/study/course-units-transferable-skills-moocs/course-unit-catalogue/course-unit/2024/412598) (Master's Degree in CSE @ Unibo).

The project involves designing and developing a library for analyzing class/interface dependencies in a Java codebase, in two different version: based on asynchronous programming (event-loop based) and on reactive programming (GUI-based for visual exploration).

This project consists of two independent parts: an asynchronous library for dependency analysis and a reactive GUI for visualizing dependencies.

The **asynchronous library** (DependencyAnalyserLib) provides non-blocking methods using [Vert.x](https://vertx.io/) to analyze Java source files and extract dependencies between classes, interfaces, and packages. It supports:
* getClassDependencies(file): ClassDepsReport
* getPackageDependencies(folder): PackageDepsReport
* getProjectDependencies(projectFolder): ProjectDepsReport

The **reactive GUI** (based on DependencyAnalyser) uses [RxJava](https://github.com/ReactiveX/RxJava) to incrementally display dependencies as a dynamic graph. Features include:
* Folder selection and analysis start button
* Real-time visualization with zoom/pan
* Nodes grouped by package, laid out without overlapping
* Arrows for dependencies
* Statistics on elements scanned

Parsing is based on [JavaParser](https://javaparser.org/). A test program is included to demonstrate usage.

### Documentation
Within the documentation directory, you will find a concise [report](docs/report.md) (in Italian) detailing all the design and implementation decisions made during the development process. In particular:
- A brief analysis of the problem.
- A description of the adopted design, strategy and architecture.
- An explanation of the system's behaviour, using Petri Nets.
