package be.kunlabora.magnumsal.exception

class IllegalTransitionException(message: String) : Exception(message)

fun transitionRequires(message: String, predicate: () -> Boolean) {
    if (!predicate()) throw IllegalTransitionException("Transition requires $message")
}
