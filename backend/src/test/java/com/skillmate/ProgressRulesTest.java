package com.skillmate;

import com.skillmate.dto.DashboardDtos.ReadinessDto;
import com.skillmate.entity.ProgressStatus;
import com.skillmate.service.ProgressService;
import com.skillmate.service.ReadinessCalculator;
import com.skillmate.service.ReadinessCalculator.SkillSnapshot;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/** Pure unit tests for the progress and readiness formulas (no Spring context needed). */
class ProgressRulesTest {
    @Test
    void percentageFollowsStatusRules() {
        assertThat(ProgressService.computePercentage(ProgressStatus.NOT_STARTED, 4, 4)).isEqualByComparingTo("0");
        assertThat(ProgressService.computePercentage(ProgressStatus.COMPLETED, 4, 0)).isEqualByComparingTo("100");
        assertThat(ProgressService.computePercentage(ProgressStatus.IN_PROGRESS, 4, 1)).isEqualByComparingTo("25");
        assertThat(ProgressService.computePercentage(ProgressStatus.IN_PROGRESS, 4, 4)).isEqualByComparingTo("99");
        assertThat(ProgressService.computePercentage(ProgressStatus.IN_PROGRESS, 100, 0)).isEqualByComparingTo("1");
        assertThat(ProgressService.computePercentage(ProgressStatus.IN_PROGRESS, 0, 0)).isEqualByComparingTo("50");
        for (ProgressStatus s : ProgressStatus.values()) {
            BigDecimal p = ProgressService.computePercentage(s, 3, 7);
            assertThat(p).isBetween(BigDecimal.ZERO, BigDecimal.valueOf(100));
        }
    }

    private final ReadinessCalculator calc = new ReadinessCalculator();

    @Test
    void noProgressMeansNotAssessedAndNothingIsWeak() {
        ReadinessDto r = calc.calculate(List.of(new SkillSnapshot(1L, "A", 1L, "S1", false, 0), new SkillSnapshot(2L, "B", 2L, "S2", false, 0)));
        assertThat(r.assessed()).isFalse();
        assertThat(r.level()).isEqualTo("NOT_ASSESSED");
        assertThat(r.overallPercentage()).isZero();
        assertThat(r.weakSkills()).isEmpty();
        assertThat(r.subjects()).allMatch(s -> s.classification().equals("NOT_ASSESSED"));
    }

    @Test
    void missingDataCountsAsZeroNotAsComplete() {
        ReadinessDto r = calc.calculate(List.of(
                new SkillSnapshot(1L, "A", 1L, "S1", true, 100), new SkillSnapshot(2L, "B", 1L, "S1", false, 0),
                new SkillSnapshot(3L, "C", 2L, "S2", false, 0)));
        assertThat(r.subjects().get(0).percentage()).isEqualTo(50.0); // (100 + 0) / 2
        assertThat(r.overallPercentage()).isEqualTo(25.0);            // (50 + 0) / 2
        assertThat(r.subjects().get(1).classification()).isEqualTo("NOT_ASSESSED");
    }

    @Test
    void weakAndStrongUseThresholdsOnAssessedSkillsOnly() {
        ReadinessDto r = calc.calculate(List.of(
                new SkillSnapshot(1L, "Low", 1L, "S1", true, 20), new SkillSnapshot(2L, "High", 1L, "S1", true, 90),
                new SkillSnapshot(3L, "Untouched", 1L, "S1", false, 0)));
        assertThat(r.weakSkills()).extracting("skillName").containsExactly("Low");
        assertThat(r.strongSkills()).extracting("skillName").containsExactly("High");
        assertThat(r.recommendations()).isNotEmpty();
        assertThat(r.disclaimer()).containsIgnoringCase("not a guarantee");
    }
}
