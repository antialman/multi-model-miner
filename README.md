# Multi-Model-Miner
Prototype application for multi-model process discovery.

Requires Java 11 JDK. Dependencies managed by Ivy.

Uses the implementation of Declare Miner from: "Maggi, F.M., Ciccio, C.D., Francescomarino, C.D., Kala, T.: Parallel algorithms for the automated discovery of declarative process models. Inf. Syst. 74(Part), 136–152 (2018)"

Additional required libraries (available in 'lib' folder):
* OpenXES-20181205.jar
* resources-1.0.jar
* Spex-1.1.jar
* TheMiner-1.1.jar

Application entry point: MainLauncherV2

Results are displayed in three tabs:
* Pruned Declare Model - Displays the discovered Succession, Precedence, Response, and Not Co-Existence constraints in a single Declare model. All relations (except Not_CoExistence are pruned). Other relations are ommitted from the visualization, but a full list of relations is provided below the model;
* Constraint Subsets - Lists all activities of the event log on three groups: mandatory, never repeated, and unconstrained cardinality. Shows Declare models of Succession, Precedence, Response, and Not Co-Existence relations separately;
* Initial Petri net - Displays the Petri net representation of the concurrently executed processes in the given event log (constructed using only the information in the pruned Declare model).

Examples of all three views are provided in the 'img' folder.
