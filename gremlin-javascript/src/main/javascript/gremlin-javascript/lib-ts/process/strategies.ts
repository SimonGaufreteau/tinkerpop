/** @abstract */
class TraversalStrategy {
  configuration?: Record<string, any>;
  readonly fqcn: string;
  /**
   * @param {String} fqcn fully qualified class name in Java of the strategy
   * @param {Object} configuration for the strategy
   */
  constructor(fqcn: string, configuration?: Record<string, any>) {
    this.fqcn = fqcn;
    this.configuration = configuration;
  }

  /**
   * @abstract
   * @param {Traversal} traversal
   * @returns {Promise}
   */
  apply(traversal) {}
}

export class ConnectiveStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.ConnectiveStrategy');
  }
}

export class ElementIdStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.ElementIdStrategy');
  }
}

export class HaltedTraverserStrategy extends TraversalStrategy {
  /**
   * @param {String} haltedTraverserFactory full qualified class name in Java of a {@code HaltedTraverserFactory} implementation
   */
  constructor(haltedTraverserFactory: string) {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.HaltedTraverserStrategy');
    if (haltedTraverserFactory !== undefined) {
      this.configuration['haltedTraverserFactory'] = haltedTraverserFactory;
    }
  }
}

export class OptionsStrategy extends TraversalStrategy {
  constructor(options) {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.OptionsStrategy', options);
  }
}

class PartitionStrategy extends TraversalStrategy {
  /**
   * @param {Object} [options]
   * @param {String} [options.partitionKey] name of the property key to partition by
   * @param {String} [options.writePartition] the value of the currently write partition
   * @param {Array<String>} [options.readPartitions] list of strings representing the partitions to include for reads
   * @param {boolean} [options.includeMetaProperties] determines if meta-properties should be included in partitioning defaulting to false
   */
  constructor(options) {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.PartitionStrategy', options);
  }
}

class SubgraphStrategy extends TraversalStrategy {
  /**
   * @param {Object} [options]
   * @param {GraphTraversal} [options.vertices] name of the property key to partition by
   * @param {GraphTraversal} [options.edges] the value of the currently write partition
   * @param {GraphTraversal} [options.vertexProperties] list of strings representing the partitions to include for reads
   * @param {boolean} [options.checkAdjacentVertices] enables the strategy to apply the {@code vertices} filter to the adjacent vertices of an edge.
   */
  constructor(options) {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy', options);
    if (this.configuration.vertices instanceof Traversal) {
      this.configuration.vertices = this.configuration.vertices.bytecode;
    }
    if (this.configuration.edges instanceof Traversal) {
      this.configuration.edges = this.configuration.edges.bytecode;
    }
    if (this.configuration.vertexProperties instanceof Traversal) {
      this.configuration.vertexProperties = this.configuration.vertexProperties.bytecode;
    }
  }
}

class ProductiveByStrategy extends TraversalStrategy {
  /**
   * @param {Object} [options]
   * @param {Array<String>} [options.productiveKeys] set of keys that will always be productive
   */
  constructor(options) {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.ProductiveByStrategy', options);
  }
}

class VertexProgramStrategy extends TraversalStrategy {
  constructor(options) {
    super('org.apache.tinkerpop.gremlin.process.computer.traversal.strategy.decoration.VertexProgramStrategy', options);
  }
}

class MatchAlgorithmStrategy extends TraversalStrategy {
  /**
   * @param matchAlgorithm
   */
  constructor(matchAlgorithm) {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.finalization.MatchAlgorithmStrategy');
    if (matchAlgorithm !== undefined) {
      this.configuration['matchAlgorithm'] = matchAlgorithm;
    }
  }
}

class AdjacentToIncidentStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.AdjacentToIncidentStrategy');
  }
}

class FilterRankingStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.FilterRankingStrategy');
  }
}

class IdentityRemovalStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.IdentityRemovalStrategy');
  }
}

class IncidentToAdjacentStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.IncidentToAdjacentStrategy');
  }
}

class InlineFilterStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.InlineFilterStrategy');
  }
}

class LazyBarrierStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.LazyBarrierStrategy');
  }
}

class MatchPredicateStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.MatchPredicateStrategy');
  }
}

class OrderLimitStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.OrderLimitStrategy');
  }
}

class PathProcessorStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.PathProcessorStrategy');
  }
}

class PathRetractionStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.PathRetractionStrategy');
  }
}

class CountStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.CountStrategy');
  }
}

class RepeatUnrollStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.RepeatUnrollStrategy');
  }
}

class GraphFilterStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.GraphFilterStrategy');
  }
}

class EarlyLimitStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.EarlyLimitStrategy');
  }
}

class LambdaRestrictionStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.LambdaRestrictionStrategy');
  }
}

class ReadOnlyStrategy extends TraversalStrategy {
  constructor() {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.ReadOnlyStrategy');
  }
}

class EdgeLabelVerificationStrategy extends TraversalStrategy {
  /**
   * @param {boolean} logWarnings determines if warnings should be written to the logger when verification fails
   * @param {boolean} throwException determines if exceptions should be thrown when verifications fails
   */
  constructor(logWarnings = false, throwException = false) {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.EdgeLabelVerificationStrategy', {
      logWarnings: logWarnings,
      throwException: throwException,
    });
  }
}

class ReservedKeysVerificationStrategy extends TraversalStrategy {
  /**
   * @param {boolean} logWarnings determines if warnings should be written to the logger when verification fails
   * @param {boolean} throwException determines if exceptions should be thrown when verifications fails
   * @param {Array<String>} keys the list of reserved keys to verify
   */
  constructor(logWarnings = false, throwException = false, keys = ['id', 'label']) {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.ReservedKeysVerificationStrategy', {
      logWarnings: logWarnings,
      throwException: throwException,
      keys: keys,
    });
  }
}

class SeedStrategy extends TraversalStrategy {
  /**
   * @param {Object} [options]
   * @param {number} [options.seed] the seed to provide to the random number generator for the traversal
   */
  constructor(options) {
    super('org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SeedStrategy', {
      seed: options.seed,
    });
  }
}
