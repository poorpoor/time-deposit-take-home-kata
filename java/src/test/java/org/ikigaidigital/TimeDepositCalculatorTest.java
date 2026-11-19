package org.ikigaidigital;

import org.ikigaidigital.domain.policy.InterestPolicy;
import org.ikigaidigital.domain.policy.InterestPolicyRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeDepositCalculatorTest {

    private final TimeDepositCalculator calculator = new TimeDepositCalculator();

    @Nested
    @DisplayName("Basic plan interest rules")
    class BasicPlanTests {

        @Test
        @DisplayName("accrues no interest during the first 30 days (days 0, 29, 30)")
        void accruesNoInterestForFirst30Days() {
            List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "basic", 1200.00, 0),
                new TimeDeposit(2, "basic", 1200.00, 29),
                new TimeDeposit(3, "basic", 1200.00, 30)
            );

            calculator.updateBalance(deposits);

            assertThat(deposits)
                .extracting(TimeDeposit::getBalance)
                .containsExactly(1200.00, 1200.00, 1200.00);
        }

        @Test
        @DisplayName("accrues 1% annual / 12 monthly interest starting from day 31 with no upper day limit")
        void accruesInterestAfterDay30() {
            List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "basic", 1200.00, 31),
                new TimeDeposit(2, "basic", 1234567.00, 45),
                new TimeDeposit(3, "basic", 1200.00, 366),
                new TimeDeposit(4, "basic", 1200.00, 1000)
            );

            calculator.updateBalance(deposits);

            assertThat(deposits)
                .extracting(TimeDeposit::getBalance)
                .containsExactly(1201.00, 1235595.81, 1201.00, 1201.00);
        }
    }

    @Nested
    @DisplayName("Student plan interest rules")
    class StudentPlanTests {

        @Test
        @DisplayName("accrues no interest during the first 30 days (days 0, 29, 30)")
        void accruesNoInterestForFirst30Days() {
            List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "student", 1200.00, 0),
                new TimeDeposit(2, "student", 1200.00, 29),
                new TimeDeposit(3, "student", 1200.00, 30)
            );

            calculator.updateBalance(deposits);

            assertThat(deposits)
                .extracting(TimeDeposit::getBalance)
                .containsExactly(1200.00, 1200.00, 1200.00);
        }

        @Test
        @DisplayName("accrues 3% annual / 12 monthly interest from day 31 through day 365 inclusive")
        void accruesInterestBetweenDay31AndDay365() {
            List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "student", 1200.00, 31),
                new TimeDeposit(2, "student", 1200.00, 365)
            );

            calculator.updateBalance(deposits);

            assertThat(deposits)
                .extracting(TimeDeposit::getBalance)
                .containsExactly(1203.00, 1203.00);
        }

        @Test
        @DisplayName("accrues no interest from day 366 onward (after 1 year)")
        void accruesNoInterestFromDay366Onward() {
            List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "student", 1200.00, 366),
                new TimeDeposit(2, "student", 1200.00, 400)
            );

            calculator.updateBalance(deposits);

            assertThat(deposits)
                .extracting(TimeDeposit::getBalance)
                .containsExactly(1200.00, 1200.00);
        }
    }

    @Nested
    @DisplayName("Premium plan interest rules")
    class PremiumPlanTests {

        @Test
        @DisplayName("accrues no interest through day 45 (days 0, 30, 31, 44, 45)")
        void accruesNoInterestThroughDay45() {
            List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "premium", 1200.00, 0),
                new TimeDeposit(2, "premium", 1200.00, 30),
                new TimeDeposit(3, "premium", 1200.00, 31),
                new TimeDeposit(4, "premium", 1200.00, 44),
                new TimeDeposit(5, "premium", 1200.00, 45)
            );

            calculator.updateBalance(deposits);

            assertThat(deposits)
                .extracting(TimeDeposit::getBalance)
                .containsExactly(1200.00, 1200.00, 1200.00, 1200.00, 1200.00);
        }

        @Test
        @DisplayName("accrues 5% annual / 12 monthly interest starting from day 46 with no upper day limit")
        void accruesInterestAfterDay45() {
            List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "premium", 1200.00, 46),
                new TimeDeposit(2, "premium", 1200.00, 366)
            );

            calculator.updateBalance(deposits);

            assertThat(deposits)
                .extracting(TimeDeposit::getBalance)
                .containsExactly(1205.00, 1205.00);
        }
    }

    @Nested
    @DisplayName("Rounding and edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("rounds monthly interest to 2 decimal places using HALF_UP")
        void roundsMonthlyInterestHalfUpToTwoDecimalPlaces() {
            List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "basic", 100.00, 31),     // 0.0833... -> 0.08
                new TimeDeposit(2, "premium", 1000.00, 46),  // 4.1666... -> 4.17
                new TimeDeposit(3, "basic", 6.00, 31)        // 0.005     -> 0.01
            );

            calculator.updateBalance(deposits);

            assertThat(deposits)
                .extracting(TimeDeposit::getBalance)
                .containsExactly(100.08, 1004.17, 6.01);
        }

        @Test
        @DisplayName("handles empty list without error")
        void handlesEmptyList() {
            List<TimeDeposit> empty = Collections.emptyList();

            calculator.updateBalance(empty);

            assertThat(empty).isEmpty();
        }

        @Test
        @DisplayName("keeps zero balance unchanged across all plans")
        void keepsZeroBalanceUnchanged() {
            List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "basic", 0.00, 60),
                new TimeDeposit(2, "student", 0.00, 60),
                new TimeDeposit(3, "premium", 0.00, 60)
            );

            calculator.updateBalance(deposits);

            assertThat(deposits)
                .extracting(TimeDeposit::getBalance)
                .containsExactly(0.00, 0.00, 0.00);
        }

        @Test
        @DisplayName("leaves unknown plan types unchanged with zero interest")
        void leavesUnknownPlanTypesUnchanged() {
            List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "unknown", 5000.00, 100)
            );

            calculator.updateBalance(deposits);

            assertThat(deposits.get(0).getBalance()).isEqualTo(5000.00);
        }

        @Test
        @DisplayName("updates mixed batch independently without leaking interest across items")
        void updatesMixedBatchIndependently() {
            List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "basic", 1200.00, 31),
                new TimeDeposit(2, "student", 1200.00, 366),
                new TimeDeposit(3, "premium", 1200.00, 46),
                new TimeDeposit(4, "basic", 1200.00, 30),
                new TimeDeposit(5, "student", 1200.00, 100)
            );

            calculator.updateBalance(deposits);

            assertThat(deposits)
                .extracting(TimeDeposit::getBalance)
                .containsExactly(1201.00, 1200.00, 1205.00, 1200.00, 1203.00);
        }

        @Test
        @DisplayName("avoids IEEE-754 floating-point drift during interest rounding and addition")
        void avoidsFloatingPointDrift() {
            List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "basic", 100.10, 45),  // 100.10 + 0.08 = 100.18 (not 100.17999999999999)
                new TimeDeposit(2, "basic", 18.00, 31)    // 18 * 0.01 / 12 = 0.015 -> HALF_UP 0.02 -> 18.02
            );

            calculator.updateBalance(deposits);

            assertThat(deposits)
                .extracting(TimeDeposit::getBalance)
                .containsExactly(100.18, 18.02);
        }
    }

    @Nested
    @DisplayName("Policy extensibility")
    class ExtensibilityTests {

        @Test
        @DisplayName("supports custom InterestPolicy implementations without modifying TimeDepositCalculator")
        void supportsCustomInterestPolicy() {
            InterestPolicy vipPolicy = new InterestPolicy() {
                @Override
                public String planType() {
                    return "vip";
                }

                @Override
                public BigDecimal calculateMonthlyInterest(TimeDeposit timeDeposit) {
                    return InterestPolicy.monthlyInterest(timeDeposit.getBalance(), new BigDecimal("0.06"));
                }
            };

            TimeDepositCalculator customCalculator = new TimeDepositCalculator(
                new InterestPolicyRegistry(List.of(vipPolicy))
            );
            List<TimeDeposit> deposits = List.of(new TimeDeposit(1, "vip", 1200.00, 10));

            customCalculator.updateBalance(deposits);

            assertThat(deposits.get(0).getBalance()).isEqualTo(1206.00);
        }

        @Test
        @DisplayName("rejects duplicate policy registrations for the same plan type")
        void rejectsDuplicatePlanTypeRegistration() {
            InterestPolicy first = new InterestPolicy() {
                @Override
                public String planType() {
                    return "vip";
                }

                @Override
                public BigDecimal calculateMonthlyInterest(TimeDeposit timeDeposit) {
                    return BigDecimal.ZERO;
                }
            };
            InterestPolicy duplicate = new InterestPolicy() {
                @Override
                public String planType() {
                    return "vip";
                }

                @Override
                public BigDecimal calculateMonthlyInterest(TimeDeposit timeDeposit) {
                    return BigDecimal.ONE;
                }
            };

            assertThatThrownBy(() -> new InterestPolicyRegistry(List.of(first, duplicate)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vip");
        }
    }
}
