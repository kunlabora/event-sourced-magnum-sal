package be.kunlabora.magnumsal.gamepieces

import be.kunlabora.magnumsal.gamepieces.Salt.*
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

    @Test
    fun `minus | delegates to Iterable's minus`() {
        assertThat(Salts(GREEN, WHITE) - Salts(emptyList())).isEqualTo(Salts(GREEN, WHITE))
        assertThat(Salts(emptyList()) - Salts(GREEN, WHITE)).isEqualTo(Salts(emptyList()))
        assertThat(Salts(GREEN, GREEN, WHITE) - Salts(BROWN)).isEqualTo(Salts(GREEN, GREEN, WHITE))
        assertThat(Salts(GREEN, GREEN, WHITE) - Salts(GREEN, GREEN, WHITE)).isEqualTo(Salts(emptyList()))
        assertThat(Salts(GREEN, GREEN, WHITE) - Salts(GREEN, WHITE)).isEqualTo(Salts(GREEN))
    }
}
