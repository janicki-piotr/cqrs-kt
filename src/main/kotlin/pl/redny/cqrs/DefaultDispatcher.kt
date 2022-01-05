package pl.redny.cqrs

import pl.redny.cqrs.command.Command
import pl.redny.cqrs.command.CommandDispatcher
import pl.redny.cqrs.command.CommandHandler
import pl.redny.cqrs.command.CommandProcessor
import pl.redny.cqrs.command.CommandException
import pl.redny.cqrs.query.QueryException
import pl.redny.cqrs.query.*

class DefaultDispatcher(
    commandHandlers: List<CommandHandler<Command>>, queryHandlers: List<QueryHandler<Query, *>>
) : CommandDispatcher, QueryDispatcher {
    private val commandHandlers: MutableList<CommandHandler<Command>> = mutableListOf()
    private val queryHandlers: MutableList<QueryHandler<Query, *>> = mutableListOf()
    private val processors: MutableList<Processor> = mutableListOf()

    init {
        this.commandHandlers.addAll(commandHandlers)
        this.queryHandlers.addAll(queryHandlers)
    }

    constructor(
        commandHandlers: List<CommandHandler<Command>>,
        queryHandlers: List<QueryHandler<Query, *>>,
        processors: List<Processor>
    ) : this(commandHandlers, queryHandlers) {
        this.processors.addAll(processors)
    }

    override fun dispatchCommand(command: Command): Result<Unit> {
        return try {
            val commandHandler = findCommandHandler(command, commandHandlers)
            processCommandProcessors(command, commandHandler, findPreProcessors(processors))
            val result: Result<Unit> = commandHandler.execute(command)
            processCommandProcessors(command, commandHandler, findPostProcessors(processors))

            result
        } catch (e: CommandException) {
            Result.failure(e)
        }
    }

    @Throws(CommandException::class)
    private fun processCommandProcessors(
        command: Command, commandHandler: CommandHandler<Command>, postProcessors: List<Processor>
    ) {
        return postProcessors.filterIsInstance<CommandProcessor>().forEach { it.process(commandHandler, command) }
    }

    private fun findCommandHandler(
        command: Command, commandHandlers: List<CommandHandler<Command>>
    ): CommandHandler<Command> {
        return commandHandlers.find { it.canHandle(command) }
            ?: throw CommandException("No command handler implementations for $command")
    }

    override fun dispatchQuery(query: Query): Result<*> {
        return try {
            val queryHandler: QueryHandler<Query, *> = findQueryHandler(query, queryHandlers)
            processQueryProcessors(query, queryHandler, findPreProcessors(processors))
            val result: Result<*> = queryHandler.execute(query)
            processQueryProcessors(query, queryHandler, findPostProcessors(processors))

            result
        } catch (e: QueryException) {
            Result.failure<Any>(e)
        }
    }

    @Throws(QueryException::class)
    private fun processQueryProcessors(
        query: Query, queryHandler: QueryHandler<Query, *>, postProcessors: List<Processor>
    ) {
        return postProcessors.filterIsInstance<QueryProcessor>().forEach { it.process(queryHandler, query) }
    }

    private fun findQueryHandler(
        query: Query, queryHandler: List<QueryHandler<Query, *>>
    ): QueryHandler<Query, *> {
        return queryHandler.find { it.canHandle(query) }
            ?: throw QueryException("No query handler implementations for $query")
    }

    private fun findPreProcessors(processors: List<Processor>): List<Processor> {
        return processors.filter {
            it.type == Processor.ProcessorType.PRE_PROCESSOR || it.type == Processor.ProcessorType.DUPLEX_PROCESSOR
        }
    }

    private fun findPostProcessors(processors: List<Processor>): List<Processor> {
        return processors.filter {
            it.type == Processor.ProcessorType.POST_PROCESSOR || it.type == Processor.ProcessorType.DUPLEX_PROCESSOR
        }
    }
}