export class RoundNotActiveError extends Error {
  constructor() {
    super("Round not active")
    this.name = "RoundNotActiveError"
  }
}

export class RoundNotInProgress extends Error {
  constructor() {
    super("Round not in progress")
    this.name = "RoundNotInProgress"
  }
}
