import { areEqual } from '../utils';

export class Path {
  /**
   * Represents a walk through a graph as defined by a traversal.
   * @param {Array} labels
   * @param {Array} objects
   * @constructor
   */
  readonly labels: string[];
  // TSTODO : object types ?
  readonly objects: any[];
  constructor(labels: string[], objects: any[]) {
    this.labels = labels;
    this.objects = objects;
  }

  toString() {
    return `path[${(this.objects || []).join(', ')}]`;
  }

  equals(other: any) {
    if (!(other instanceof Path)) {
      return false;
    }
    if (other === this) {
      return true;
    }
    return areEqual(this.objects, other.objects) && areEqual(this.labels, other.labels);
  }
}
