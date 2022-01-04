package pl.redny.cqrs.command

import pl.redny.cqrs.Processor

interface CommandProcessor : Processor {
    @Throws(CommandException::class)
    fun <T : CommandHandler<Command>> process(commandHandler: T, command: Any)
}