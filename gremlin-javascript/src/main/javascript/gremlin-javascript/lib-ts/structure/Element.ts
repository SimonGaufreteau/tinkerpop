import { Property } from './Property';

// TSTODO : type of id ?
// type of record key ?
export class Element {
  readonly id: any;
  readonly label: string;
  properties: Record<any, Property>;

  constructor(id: any, label: string) {
    this.id = id;
    this.label = label;
    this.properties = {};
  }

  setProperties(newProperties: Record<any, Property>) {
    this.properties = newProperties;
  }

  /**
   * Compares this instance to another and determines if they can be considered as equal.
   * @param {Element} other
   * @returns {boolean}
   */
  equals(other: Element): boolean {
    return other instanceof Element && this.id === other.id;
  }
}
