package pl.redny.cqrs.query

import pl.redny.cqrs.Processor

interface QueryProcessor : Processor {
    @Throws(QueryException::class)
    fun process(queryHandler: Any, query: Any)
}