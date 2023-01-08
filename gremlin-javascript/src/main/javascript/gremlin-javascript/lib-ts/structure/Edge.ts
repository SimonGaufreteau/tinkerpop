import { Element } from './Element';
import { Property } from './Property';
import { Vertex } from './Vertex';

export class Edge extends Element {
  readonly outV: Vertex;
  readonly inV: Vertex;
  constructor(id: any, outV: Vertex, label: string, inV: Vertex, properties: Record<any, Property>) {
    super(id, label);
    this.outV = outV;
    this.inV = inV;
    this.setProperties(properties);
  }

  toString() {
    const outVId = this.outV ? this.outV.id : '?';
    const inVId = this.inV ? this.inV.id : '?';

    return `e[${this.id}][${outVId}-${this.label}->${inVId}]`;
  }
}
