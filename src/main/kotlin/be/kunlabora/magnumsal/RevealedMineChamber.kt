package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.gamepieces.MineChamberTile

data class RevealedMineChamber(val at: PositionInMine, val mineChamberTile: MineChamberTile) {
    init {
        require(at.isInACorridor()) { "A MineChamber can not be present in the mineshaft" }
        require(at.level == mineChamberTile.level) { "A MineChamber's position needs to relate to the level of its MineChamberTile" }
    }

    fun asTile(): MineChamberTile = this.mineChamberTile
}
