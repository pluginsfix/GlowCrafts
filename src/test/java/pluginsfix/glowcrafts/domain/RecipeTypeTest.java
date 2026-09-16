package pluginsfix.glowcrafts.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeTypeTest {

    @Test
    void parseFromString() {
        assertThat(RecipeType.fromString("SHAPED")).isEqualTo(RecipeType.SHAPED);
        assertThat(RecipeType.fromString("shapeless")).isEqualTo(RecipeType.SHAPELESS);
        assertThat(RecipeType.fromString("ANVIL")).isEqualTo(RecipeType.ANVIL);
        assertThat(RecipeType.fromString("furnace")).isEqualTo(RecipeType.FURNACE);
        assertThat(RecipeType.fromString("unknown")).isEqualTo(RecipeType.SHAPED);
        assertThat(RecipeType.fromString(null)).isEqualTo(RecipeType.SHAPED);
    }
}
