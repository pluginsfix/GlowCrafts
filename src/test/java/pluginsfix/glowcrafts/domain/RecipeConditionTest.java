package pluginsfix.glowcrafts.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeConditionTest {

    @Test
    void conditionValidation() {
        RecipeCondition condition = new RecipeCondition("glowcrafts.craft.sword", 5, 100.0, 200, 1.5f);

        assertThat(condition.hasPermission()).isTrue();
        assertThat(condition.permission()).isEqualTo("glowcrafts.craft.sword");
        assertThat(condition.hasLevelCost()).isTrue();
        assertThat(condition.levelCost()).isEqualTo(5);
        assertThat(condition.hasMoneyCost()).isTrue();
        assertThat(condition.moneyCost()).isEqualTo(100.0);
        assertThat(condition.furnaceCookingTimeTicks()).isEqualTo(200);
        assertThat(condition.furnaceExperience()).isEqualTo(1.5f);
    }

    @Test
    void emptyCondition() {
        RecipeCondition empty = RecipeCondition.empty();

        assertThat(empty.hasPermission()).isFalse();
        assertThat(empty.hasLevelCost()).isFalse();
        assertThat(empty.hasMoneyCost()).isFalse();
        assertThat(empty.furnaceCookingTimeTicks()).isEqualTo(200);
        assertThat(empty.furnaceExperience()).isEqualTo(0.0f);
    }
}
