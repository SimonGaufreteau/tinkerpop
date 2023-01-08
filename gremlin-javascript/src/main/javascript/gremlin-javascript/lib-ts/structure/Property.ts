export class Property {
  key: string;
  value: PropertyTypes;
  equals(other: any): boolean {
    return other instanceof Property && this.key === other.key && this.value === other.value;
  }
}

// TSTODO : types of properties ?
export type PropertyTypes = any;
