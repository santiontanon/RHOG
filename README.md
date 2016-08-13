# RHOG: A Refinement-Operator Library for Directed Labeled Graphs

RHOG is a Java library to operate with directed labeled graphs (DLGs), that includes:
- Import/export to formats such as DOT, GML, GraphML, TGF, and more.
- Operations on DLGs:
  - basic vertex/edge addition/elimination
  - graph type checking (trees, lattices, DAGs, connected graphs, etc.)
  - subsumption checks
  - anti-unification
  - disintegration
- refinement operators: a large set of refinement operators are included in the library, supporting graphs with flat labels, or graphs whose labels are part of an ontology, etc.
- basic machine learning algorithms based on the refinement operators (distance and similarity measures for use in nearest neighbor classifiers)
- graph visualization tools for easy debugging

An article describing all the theory behind the functionality presented in this library can be found [here](http://arxiv.org/abs/1604.06954).

Project supported by NSF grant IIS-1551338.
