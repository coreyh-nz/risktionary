export interface Word {
  id: string
  value: string
  synonyms: string[]
  descriptionText: string
  descriptionContent: string
}

export interface WordSummary {
  id: string
  value: string
  synonyms: string[]
  description: string
}
