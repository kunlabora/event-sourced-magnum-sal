package be.kunlabora.magnumsal

data class MineShaftPosition(private val _at: Int) {
    init {
        if (_at !in 1..6) throw IllegalArgumentException("MineShaftPosition $_at does not exist.")
    }

    fun previous(): MineShaftPosition = if (_at == 1) this else this.copy(_at = _at - 1)
    fun next(): MineShaftPosition = if (_at == 6) this else this.copy(_at = _at + 1)
    operator fun rangeTo(other: MineShaftPosition): List<MineShaftPosition> = (this._at..other._at).map { MineShaftPosition(it) }
    override fun toString(): String = "mineshaft[$_at]"
}
