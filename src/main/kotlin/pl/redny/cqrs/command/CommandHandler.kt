package pl.redny.cqrs.command

interface CommandHandler<T : Command> {
    @Throws(CommandException::class)
    fun execute(command: T): Result<Void>
    fun canHandle(command: Command): Boolean
}