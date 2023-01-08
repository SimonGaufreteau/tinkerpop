import { summarize } from '../utils';
import { Element } from './Element';
import { Property, PropertyTypes } from './Property';

// TSTODO : type of value ?
export class VertexProperty extends Element implements Property {
  readonly key: string;
  readonly value: PropertyTypes;

  constructor(id: string, label: string, value: PropertyTypes, properties: Record<any, Property>) {
    super(id, label);
    this.setProperties(properties);
    this.value = value;
  }

  toString(): string {
    return `vp[${this.label}->${summarize(this.value)}]`;
  }
}
