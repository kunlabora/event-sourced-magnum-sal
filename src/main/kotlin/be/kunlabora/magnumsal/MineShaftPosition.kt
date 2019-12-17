package be.kunlabora.magnumsal

data class MineShaftPosition constructor(val index: Int) {
    init {
        require(index in 1..6) { "Mine shaft is only 6 deep" }
    }
    fun previous() = MineShaftPosition(index - 1)
    fun next() = MineShaftPosition(index + 1)
    fun isTheTop() = this.index == 1
    override fun toString() = "mine[$index]"

    companion object {
        fun at(index: Int) : MineShaftPosition = MineShaftPosition(index)
    }
}
