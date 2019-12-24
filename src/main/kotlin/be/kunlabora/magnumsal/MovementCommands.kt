package be.kunlabora.magnumsal

sealed class MinerMovement(open val player: PlayerColor, open val at: PositionInMine) {
    data class RemoveMiner(override val player: PlayerColor, override val at: PositionInMine) : MinerMovement(player, at)
    data class PlaceMiner(override val player: PlayerColor, override val at: PositionInMine): MinerMovement(player, at)
}

