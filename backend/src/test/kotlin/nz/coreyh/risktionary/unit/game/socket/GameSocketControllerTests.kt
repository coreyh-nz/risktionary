package nz.coreyh.risktionary.unit.game.socket

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.domain.model.drawing.DrawingPoint
import nz.coreyh.risktionary.game.domain.model.drawing.DrawingTool
import nz.coreyh.risktionary.game.socket.GameSocketController
import nz.coreyh.risktionary.game.socket.messages.inbound.drawing.DrawingCanvasClearCommand
import nz.coreyh.risktionary.game.socket.messages.inbound.drawing.DrawingStrokeEndCommand
import nz.coreyh.risktionary.game.socket.messages.inbound.drawing.DrawingStrokePointsCommand
import nz.coreyh.risktionary.game.socket.messages.inbound.drawing.DrawingStrokeStartCommand
import nz.coreyh.risktionary.game.socket.messages.outbound.drawing.DrawingCanvasClearEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.drawing.DrawingStrokeEndEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.drawing.DrawingStrokePointsEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.drawing.DrawingStrokeStartEvent
import nz.coreyh.risktionary.game.socket.support.WebSocketDestinations
import nz.coreyh.risktionary.support.factory.game.socket.createTestGameSocketHostPrincipal
import nz.coreyh.risktionary.support.factory.game.socket.createTestGameSocketPlayerPrincipal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.messaging.simp.SimpMessagingTemplate

class GameSocketControllerTests {
    private lateinit var gameSessionService: GameSessionService
    private lateinit var simpMessagingTemplate: SimpMessagingTemplate
    private lateinit var gameSocketController: GameSocketController

    @BeforeEach
    fun setup() {
        gameSessionService = mockk()
        simpMessagingTemplate = mockk()
        gameSocketController = GameSocketController(gameSessionService, simpMessagingTemplate)
    }

    @Nested
    inner class Draw {
        private val capturedDestination = slot<String>()
        private val capturedEvent = slot<Any>()

        @BeforeEach
        fun setup() {
            every {
                simpMessagingTemplate.convertAndSend(
                    capture(capturedDestination),
                    capture(capturedEvent),
                )
            } returns Unit
        }

        @Nested
        inner class StrokeStart {
            private val command =
                DrawingStrokeStartCommand(
                    tool = DrawingTool.PEN,
                    point = DrawingPoint(x = 10.0, y = 20.0),
                    colour = "#FF0000",
                )

            @Test
            fun `broadcasts STROKE_START event with host id when principal is host`() {
                val principal = createTestGameSocketHostPrincipal()

                gameSocketController.onDraw(principal, command)

                assertBroadcast(WebSocketDestinations.Topic.draw(principal.gameId)) { event ->
                    event.shouldBeInstanceOf<DrawingStrokeStartEvent>()
                    event.drawerId shouldBe principal.id
                    event.tool shouldBe command.tool
                    event.point shouldBe command.point
                    event.colour shouldBe command.colour
                }
            }

            @Test
            fun `broadcasts STROKE_START event with player id when principal is player`() {
                val principal = createTestGameSocketPlayerPrincipal()

                gameSocketController.onDraw(principal, command)

                assertBroadcast(WebSocketDestinations.Topic.draw(principal.gameId)) { event ->
                    event.shouldBeInstanceOf<DrawingStrokeStartEvent>()
                    event.drawerId shouldBe principal.id
                    event.tool shouldBe command.tool
                    event.point shouldBe command.point
                    event.colour shouldBe command.colour
                }
            }
        }

        @Nested
        inner class StrokePoints {
            private val command =
                DrawingStrokePointsCommand(
                    points = listOf(DrawingPoint(1.0, 2.0), DrawingPoint(3.0, 4.0)),
                )

            @Test
            fun `broadcasts STROKE_POINTS event with host id`() {
                val principal = createTestGameSocketHostPrincipal()

                gameSocketController.onDraw(principal, command)

                assertBroadcast(WebSocketDestinations.Topic.draw(principal.gameId)) { event ->
                    event.shouldBeInstanceOf<DrawingStrokePointsEvent>()
                    event.drawerId shouldBe principal.id
                    event.points shouldBe command.points
                }
            }

            @Test
            fun `broadcasts STROKE_POINTS event with player id`() {
                val principal = createTestGameSocketPlayerPrincipal()

                gameSocketController.onDraw(principal, command)

                assertBroadcast(WebSocketDestinations.Topic.draw(principal.gameId)) { event ->
                    event.shouldBeInstanceOf<DrawingStrokePointsEvent>()
                    event.drawerId shouldBe principal.id
                    event.points shouldBe command.points
                }
            }

            @Test
            fun `broadcasts empty STROKE_POINTS event with empty points list`() {
                val principal = createTestGameSocketHostPrincipal()

                gameSocketController.onDraw(principal, DrawingStrokePointsCommand(points = emptyList()))

                assertBroadcast(WebSocketDestinations.Topic.draw(principal.gameId)) { event ->
                    event.shouldBeInstanceOf<DrawingStrokePointsEvent>()
                    event.points shouldBe emptyList()
                }
            }
        }

        @Nested
        inner class StrokeEnd {
            private val command =
                DrawingStrokeEndCommand()

            @Test
            fun `broadcasts STROKE_END event with host id`() {
                val principal = createTestGameSocketHostPrincipal()

                gameSocketController.onDraw(principal, command)

                assertBroadcast(WebSocketDestinations.Topic.draw(principal.gameId)) { event ->
                    event.shouldBeInstanceOf<DrawingStrokeEndEvent>()
                    event.drawerId shouldBe principal.id
                }
            }

            @Test
            fun `broadcasts STROKE_END event with player id`() {
                val principal = createTestGameSocketPlayerPrincipal()

                gameSocketController.onDraw(principal, command)

                assertBroadcast(WebSocketDestinations.Topic.draw(principal.gameId)) { event ->
                    event.shouldBeInstanceOf<DrawingStrokeEndEvent>()
                    event.drawerId shouldBe principal.id
                }
            }
        }

        @Nested
        inner class CanvasClear {
            @Test
            fun `broadcasts CANVAS_CLEAR event as host`() {
                val principal = createTestGameSocketHostPrincipal()

                gameSocketController.onDraw(principal, DrawingCanvasClearCommand())

                assertBroadcast(WebSocketDestinations.Topic.draw(principal.gameId)) { event ->
                    event.shouldBeInstanceOf<DrawingCanvasClearEvent>()
                }
            }

            @Test
            fun `broadcasts CANVAS_CLEAR event as player`() {
                val principal = createTestGameSocketPlayerPrincipal()

                gameSocketController.onDraw(principal, DrawingCanvasClearCommand())

                assertBroadcast(WebSocketDestinations.Topic.draw(principal.gameId)) { event ->
                    event.shouldBeInstanceOf<DrawingCanvasClearEvent>()
                }
            }
        }

        private fun assertBroadcast(
            expectedDestination: String,
            block: (Any) -> Unit,
        ) {
            verify(exactly = 1) { simpMessagingTemplate.convertAndSend(any<String>(), any<Any>()) }
            capturedDestination.captured shouldBe expectedDestination
            block(capturedEvent.captured)
        }
    }
}
