import { Element } from './Element';
import { VertexProperty } from './VertexProperty';

export class Vertex extends Element {
  readonly properties: Record<any, VertexProperty>;

  constructor(id: string, label: string, properties) {
    super(id, label);
    this.properties = properties;
  }

  toString(): string {
    return `v[${this.id}]`;
  }
}
