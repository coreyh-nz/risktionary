package nz.coreyh.risktionary.unit.game.domain.model.round.hint

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import nz.coreyh.risktionary.game.domain.model.round.hint.CharacterHint
import nz.coreyh.risktionary.game.domain.model.round.hint.toWordHint
import org.junit.jupiter.api.Test

class WordHintTests {
    @Test
    fun `to word hint returns all hidden when string contains no spaces`() {
        val result = "abc".toWordHint()

        result.characters shouldContainExactly
            listOf(
                CharacterHint.Hidden,
                CharacterHint.Hidden,
                CharacterHint.Hidden,
            )
    }

    @Test
    fun `to word hint returns space when character is space`() {
        val result = "a b".toWordHint()

        result.characters shouldContainExactly
            listOf(
                CharacterHint.Hidden,
                CharacterHint.Space,
                CharacterHint.Hidden,
            )
    }

    @Test
    fun `to word hint returns empty list when string is empty`() {
        val result = "".toWordHint()

        result.characters.shouldBeEmpty()
    }
}
