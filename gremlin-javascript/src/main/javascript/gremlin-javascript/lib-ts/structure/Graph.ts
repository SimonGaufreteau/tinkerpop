/**
 * An "empty" graph object to server only as a reference.
 */

export class Graph {
  /**
   * Returns the graph traversal source.
   * @param {Function} [traversalSourceClass] The constructor to use for the {@code GraphTraversalSource} instance.
   * @returns {GraphTraversalSource}
   * @deprecated As of release 3.3.5, replaced by the traversal() anonymous function.
   */
  // TSTODO : change this
  traversal(traversalSourceClass: any): gt.GraphTraversalSource {
    return new gt.GraphTraversalSource(this, new TraversalStrategies());
  }

  toString() {
    return 'graph[]';
  }
}
