package pl.redny.cqrs

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import pl.redny.cqrs.command.Command
import pl.redny.cqrs.command.CommandHandler
import pl.redny.cqrs.command.CommandProcessor
import pl.redny.cqrs.query.Query
import pl.redny.cqrs.query.QueryHandler
import pl.redny.cqrs.query.QueryProcessor

internal class DefaultDispatcherTest {

    private fun getCommandHandlers(): List<CommandHandler<*>> {
        return listOf(TestCommandHandler(), AnotherTestCommandHandler())
    }

    private fun getQueryHandlers(): List<QueryHandler<*, *>> {
        return listOf(TestQueryHandler(), AnotherTestQueryHandler())
    }

    private fun getProcessors(): List<Processor> {
        return listOf(
            TestProcessor(Processor.ProcessorType.DUPLEX_PROCESSOR),
            TestProcessor(Processor.ProcessorType.PRE_PROCESSOR),
            TestProcessor(Processor.ProcessorType.POST_PROCESSOR)
        )
    }

    private fun getDefaultDispatcher(): DefaultDispatcher {
        return DefaultDispatcher(
            getCommandHandlers() as List<CommandHandler<Command>>,
            getQueryHandlers() as List<QueryHandler<Query, *>>,
            getProcessors()
        )
    }

    @Test
    fun dispatchCommand_command_success() {
        // given
        val commandDispatcher = getDefaultDispatcher()
        val command = TestCommand()
        // when
        val result = commandDispatcher.dispatchCommand(command)
        // then
        assertTrue(result.isSuccess)
    }

    @Test
    fun dispatchCommand_command_failure() {
        // given
        val commandDispatcher = getDefaultDispatcher()
        val command = AnotherTestCommand()
        // when
        val result = commandDispatcher.dispatchCommand(command)
        // then
        assertTrue(result.isFailure)
    }

    @Test
    fun dispatchCommand_command_unhandled() {
        // given
        val commandDispatcher = getDefaultDispatcher()
        val command = UnhandledCommand()
        // when
        val result = commandDispatcher.dispatchCommand(command)
        // then
        assertTrue(result.isFailure)

    }

    @Test
    fun dispatchQuery_command_success() {
        // given
        val queryDispatcher = getDefaultDispatcher()
        val query = TestQuery()
        // when
        val result = queryDispatcher.dispatchQuery(query)
        // then
        assertTrue(result.isSuccess)
    }

    @Test
    fun dispatchQuery_command_failure() {
        // given
        val queryDispatcher = getDefaultDispatcher()
        val query = AnotherTestQuery()
        // when
        val result = queryDispatcher.dispatchQuery(query)
        // then
        assertTrue(result.isFailure)
    }

    @Test
    fun dispatchQuery_command_unhandled() {
        // given
        val queryDispatcher = getDefaultDispatcher()
        val query = UnhandledQuery()
        // when
        val result = queryDispatcher.dispatchQuery(query)
        // then
        assertTrue(result.isFailure)
    }
}

class UnhandledQuery : Query

class TestQuery : Query
class TestQueryHandler : QueryHandler<TestQuery, String> {
    override fun execute(query: TestQuery): Result<String> = Result.success("Test")
    override fun canHandle(query: Query): Boolean = query is TestQuery
}

class AnotherTestQuery : Query
class AnotherTestQueryHandler : QueryHandler<AnotherTestQuery, String> {
    override fun execute(query: AnotherTestQuery): Result<String> = Result.failure(Exception())
    override fun canHandle(query: Query): Boolean = query is AnotherTestQuery
}

class UnhandledCommand : Command

class TestCommand : Command
class TestCommandHandler : CommandHandler<TestCommand> {
    override fun execute(command: TestCommand): Result<Unit> = Result.success(Unit)
    override fun canHandle(command: Command): Boolean = command is TestCommand
}

class AnotherTestCommand : Command
class AnotherTestCommandHandler : CommandHandler<AnotherTestCommand> {
    override fun execute(command: AnotherTestCommand): Result<Unit> = Result.failure(Exception())
    override fun canHandle(command: Command): Boolean = command is AnotherTestCommand
}

class TestProcessor(override val type: Processor.ProcessorType) : QueryProcessor, CommandProcessor {
    override fun <T : CommandHandler<Command>> process(commandHandler: T, command: Any) {}
    override fun process(queryHandler: Any, query: Any) {}
}