import { Traversal } from './traversal/Traversal';

export class TraversalStrategies {
  strategies: TraversalStrategy[];
  /**
   * Creates a new instance of TraversalStrategies.
   * @param {TraversalStrategies} [parent] The parent strategies from where to clone the values from.
   */
  constructor(parent?: TraversalStrategies) {
    if (parent) {
      // Clone the strategies
      this.strategies = [...parent.strategies];
    } else {
      this.strategies = [];
    }
  }

  addStrategy(strategy: TraversalStrategy) {
    this.strategies.push(strategy);
  }

  removeStrategy(strategy: TraversalStrategy) {
    const idx = this.strategies.findIndex((s) => s.fqcn === strategy.fqcn);
    if (idx !== -1) {
      return this.strategies.splice(idx, 1)[0];
    }

    return undefined;
  }

  applyStrategies(traversal: Traversal) {
    // Apply all strategies serially
    return this.strategies.reduce(
      (promise, strategy) => promise.then(() => strategy.apply(traversal)),
      Promise.resolve(),
    );
  }
}
