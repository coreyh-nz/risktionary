package nz.coreyh.risktionary.unit.game.web.dto.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import nz.coreyh.risktionary.game.web.dto.GameConfigurationDto
import nz.coreyh.risktionary.game.web.dto.request.CreateGameRequest
import nz.coreyh.risktionary.game.web.dto.request.toCommand
import nz.coreyh.risktionary.shared.exception.ValidationException
import nz.coreyh.risktionary.support.annotation.MockKTest
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import nz.coreyh.risktionary.support.factory.word.createTestWord
import nz.coreyh.risktionary.words.application.service.WordService
import nz.coreyh.risktionary.words.domain.model.createWordId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

@MockKTest
class CreateGameRequestTests {
    private lateinit var wordService: WordService

    private val hostId = createTestUserId()

    @BeforeEach
    fun setup() {
        wordService = mockk()
    }

    private fun requestWith(
        wordIds: List<String> = listOf(),
        lobbyCountdownMs: Long = 5_000,
        phaseDurationsMs: Map<String, Long> = mapOf(),
        skippingCountdownsEnabled: Boolean = false,
    ) = CreateGameRequest(
        configuration =
            GameConfigurationDto(
                wordIds = wordIds,
                lobbyCountdownMs = lobbyCountdownMs,
                phaseDurationsMs = phaseDurationsMs,
                skippingCountdownsEnabled = skippingCountdownsEnabled,
            ),
    )

    @Test
    fun `to command builds command from a valid request`() {
        val word = createTestWord()
        every { wordService.findWordsByIds(listOf(word.id)) } returns listOf(word)
        val request =
            requestWith(
                wordIds = listOf(word.id.toString()),
                lobbyCountdownMs = 10_000,
                phaseDurationsMs = mapOf(RoundPhaseType.DRAWING.name to 30_000),
                skippingCountdownsEnabled = true,
            )

        val command = request.toCommand(hostId, wordService)

        command.hostId shouldBe hostId
        command.words shouldBe listOf(word)
        command.lobbyCountdown shouldBe 10_000.milliseconds
        command.phaseDurations shouldBe mapOf(RoundPhaseType.DRAWING to 30_000.milliseconds)
        command.skippingCountdownsEnabled shouldBe true
    }

    @Test
    fun `to command resolves words in the order the ids were given`() {
        val first = createTestWord()
        val second = createTestWord()
        every { wordService.findWordsByIds(listOf(first.id, second.id)) } returns listOf(second, first)
        val request = requestWith(wordIds = listOf(first.id.toString(), second.id.toString()))

        val command = request.toCommand(hostId, wordService)

        command.words shouldBe listOf(first, second)
    }

    @Test
    fun `to command throws when no words are given`() {
        val request = requestWith(wordIds = listOf())

        val exception =
            shouldThrow<ValidationException> {
                request.toCommand(hostId, wordService)
            }

        exception.fieldErrors shouldContainKey "configuration.wordIds"
    }

    @Test
    fun `to command does not look up words when no word ids are given`() {
        val request = requestWith(wordIds = listOf())

        shouldThrow<ValidationException> {
            request.toCommand(hostId, wordService)
        }

        verify(exactly = 0) { wordService.findWordsByIds(any()) }
    }

    @Test
    fun `to command throws with an indexed error for a malformed word id`() {
        val word = createTestWord()
        every { wordService.findWordsByIds(listOf(word.id)) } returns listOf(word)
        val request = requestWith(wordIds = listOf(word.id.toString(), "not-a-uuid"))

        val exception =
            shouldThrow<ValidationException> {
                request.toCommand(hostId, wordService)
            }

        exception.fieldErrors shouldContainKey "configuration.wordIds[1]"
    }

    @Test
    fun `to command throws with an indexed error for each unknown word id`() {
        val unknownId = createWordId()
        every { wordService.findWordsByIds(listOf(unknownId)) } returns listOf()
        val request = requestWith(wordIds = listOf(unknownId.toString()))

        val exception =
            shouldThrow<ValidationException> {
                request.toCommand(hostId, wordService)
            }

        exception.fieldErrors shouldContainKey "configuration.wordIds[0]"
    }

    @Test
    fun `to command reports errors for both a malformed and an unknown word id`() {
        val unknownId = createWordId()
        every { wordService.findWordsByIds(listOf(unknownId)) } returns listOf()
        val request = requestWith(wordIds = listOf("not-a-uuid", unknownId.toString()))

        val exception =
            shouldThrow<ValidationException> {
                request.toCommand(hostId, wordService)
            }

        exception.fieldErrors shouldContainKey "configuration.wordIds[0]"
        exception.fieldErrors shouldContainKey "configuration.wordIds[1]"
    }

    @Test
    fun `to command throws with an error for an unknown round phase`() {
        val word = createTestWord()
        every { wordService.findWordsByIds(listOf(word.id)) } returns listOf(word)
        val request =
            requestWith(
                wordIds = listOf(word.id.toString()),
                phaseDurationsMs = mapOf("NOT_A_PHASE" to 1_000),
            )

        val exception =
            shouldThrow<ValidationException> {
                request.toCommand(hostId, wordService)
            }

        exception.fieldErrors shouldContainKey "configuration.phaseDurationsMs[NOT_A_PHASE]"
    }

    @Test
    fun `to command reports all field errors from a single call together`() {
        val request =
            requestWith(
                wordIds = listOf("not-a-uuid"),
                phaseDurationsMs = mapOf("NOT_A_PHASE" to 1_000),
            )

        val exception =
            shouldThrow<ValidationException> {
                request.toCommand(hostId, wordService)
            }

        exception.fieldErrors shouldContainKey "configuration.wordIds[0]"
        exception.fieldErrors shouldContainKey "configuration.phaseDurationsMs[NOT_A_PHASE]"
    }
}
