package be.kunlabora.magnumsal

data class PositionInMine(val depth: Int, val width: Int) {
    init {
        require(depth in 1..6) { "The Mine is only 6 deep" }
    }

    fun higher() = PositionInMine(depth - 1, 0)
    fun deeper() = PositionInMine(depth + 1, 0)
    fun isTheTop() = this.depth == 1
    override fun toString(): String {
        return if (width == 0) "mineshaft[$depth]" else if (width < 0) "leftcorridor[$depth]" else "rightcorridor[$depth]"
    }

    companion object {
        fun at(depth: Int, width: Int): PositionInMine = PositionInMine(depth, width)
    }
}
