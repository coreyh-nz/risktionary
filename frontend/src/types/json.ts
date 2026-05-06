export type JsonSerializable =
  | string
  | number
  | boolean
  | null
  | undefined
  | JsonSerializable[]
  | { [key: string]: JsonSerializable }
