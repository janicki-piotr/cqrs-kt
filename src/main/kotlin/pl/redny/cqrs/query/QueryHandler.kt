package pl.redny.cqrs.query

interface QueryHandler<T : Query, U> {
    fun execute(query: T): Result<U>
    fun canHandle(query: Query): Boolean
}