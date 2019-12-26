package be.kunlabora.magnumsal

fun String.toCamelCase(): String {
    val allLower = this.toLowerCase()
    return allLower.replaceFirst(allLower[0], allLower[0].toUpperCase())
}
