package pl.redny.cqrs.command

interface CommandDispatcher {
    @Throws(CommandException::class)
    fun dispatchCommand(command: Command): Result<Unit>
}