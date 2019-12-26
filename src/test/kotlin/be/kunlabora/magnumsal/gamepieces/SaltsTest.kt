package be.kunlabora.magnumsal.gamepieces

import be.kunlabora.magnumsal.gamepieces.Salt.GREEN
import be.kunlabora.magnumsal.gamepieces.Salt.WHITE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SaltsTest {
    @Test
    fun `canBeMinedFrom | when target Salts contains all salts, returns true`() {
        val saltInMineChamber = Salts(GREEN, GREEN, WHITE)
        val saltsToBeMined = Salts(GREEN, GREEN, WHITE)

        assertThat(saltsToBeMined.canBeMinedFrom(saltInMineChamber)).isTrue()
    }

    @Test
    fun `canBeMinedFrom | when target Salts does not contain one of the salts, returns false`() {
        val saltInMineChamber = Salts(GREEN, WHITE)
        val saltInOtherMineChamber = Salts(GREEN, GREEN)
        val saltsToBeMined = Salts(GREEN, GREEN, WHITE)
        assertThat(saltsToBeMined.canBeMinedFrom(saltInMineChamber)).isFalse()
        assertThat(saltsToBeMined.canBeMinedFrom(saltInOtherMineChamber)).isFalse()
    }
}
