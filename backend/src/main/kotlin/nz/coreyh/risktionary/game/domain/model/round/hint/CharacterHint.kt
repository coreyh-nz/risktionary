package nz.coreyh.risktionary.game.domain.model.round.hint

/**
 * Represents a hint for a single character in a word.
 *
 * Each character in the word is represented as one of:
 * - [Hidden] — the character exists but has not been revealed.
 * - [Space] — the character is a space between words.
 * - [Revealed] — the character has been revealed to the guesser.
 */
sealed interface CharacterHint {
    val type: CharacterHintType

    /**
     * A character that exists in the word but has not yet been revealed.
     */
    data object Hidden : CharacterHint {
        override val type = CharacterHintType.HIDDEN
    }

    /**
     * A space between words.
     */
    data object Space : CharacterHint {
        override val type = CharacterHintType.SPACE
    }

    /**
     * A character that has been revealed to the guesser.
     *
     * @property character the revealed character.
     */
    data class Revealed(
        val character: Char,
    ) : CharacterHint {
        override val type = CharacterHintType.REVEALED
    }
}
