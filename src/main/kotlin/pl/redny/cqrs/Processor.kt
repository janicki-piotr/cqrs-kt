package pl.redny.cqrs

interface Processor {
    val type: ProcessorType

    enum class ProcessorType {
        PRE_PROCESSOR, POST_PROCESSOR, DUPLEX_PROCESSOR
    }
}