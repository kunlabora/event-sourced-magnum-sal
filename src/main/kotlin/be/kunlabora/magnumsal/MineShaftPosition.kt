package be.kunlabora.magnumsal

data class MineShaftPosition(val index: Int) {
    init {
        require(index in 1..6) { "Mine shaft is only 6 deep" }
    }
    fun previous(): MineShaftPosition = MineShaftPosition(index - 1)
    fun next(): MineShaftPosition = MineShaftPosition(index + 1)
    override fun toString(): String = "mine[$index]"
}
