package nz.coreyh.risktionary.game.domain.model.round.hint

/**
 * Represents a hint for a word to be guessed, consisting of a list of character hints.
 *
 * @property characters the list of character hints that make up the word.
 */
data class WordHint(
    val characters: List<CharacterHint>,
)

fun String.toWordHint(): WordHint =
    WordHint(
        characters =
            map { character ->
                when {
                    character == ' ' -> CharacterHint.Space
                    else -> CharacterHint.Hidden
                }
            },
    )
