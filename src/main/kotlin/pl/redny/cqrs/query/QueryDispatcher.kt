package pl.redny.cqrs.query

interface QueryDispatcher {
    fun dispatchQuery(query: Query): Result<*>
}