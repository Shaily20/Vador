package org.revcloud.vader.runner.spec;

import consumer.failure.ValidationFailure;
import io.vavr.collection.HashSet;
import lombok.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.Runner;
import org.revcloud.vader.runner.ValidationConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static consumer.failure.ValidationFailure.INVALID_COMBO_1;
import static consumer.failure.ValidationFailure.INVALID_COMBO_2;
import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.getFailureWithParams;
import static consumer.failure.ValidationFailureMessage.MSG_WITH_PARAMS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class Spec2Test {
    @Test
    void spec2Test() {
        final var invalidComboSpec = "invalidComboSpec";
        final var validComboMap = Map.of(
                1, Set.of("1", "one"),
                2, Set.of("2", "two")
        );
        final var validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpec(spec ->
                spec._2().nameForTest(invalidComboSpec)
                        .orFailWith(INVALID_COMBO_1)
                        .when(Bean::getValue)
                        .then(Bean::getValueStr)
                        .shouldRelateWith(validComboMap)
                        .orFailWithFn((value, valueStr) -> getFailureWithParams(MSG_WITH_PARAMS, value, valueStr)))
                .prepare();

        final var invalidBean1 = new Bean(1, "a", null, null);
        assertFalse(validationConfig.getSpecWithName(invalidComboSpec).map(spec -> spec.test(invalidBean1)).orElse(true));

        final var invalidBean2 = new Bean(2, "b", null, null);
        assertFalse(validationConfig.getSpecWithName(invalidComboSpec).map(spec -> spec.test(invalidBean2)).orElse(true));

        final var validBean1 = new Bean(1, "one", null, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidComboSpec).map(spec -> spec.test(validBean1)).orElse(false));

        final var validBean2 = new Bean(2, "two", null, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidComboSpec).map(spec -> spec.test(validBean2)).orElse(false));
    }

    @Test
    void spec2TestWithNullValue() {
        final var invalidComboSpec = "invalidComboSpec";
        final var validComboMap = Map.of(
                BillingTerm.OneTime, HashSet.of(null, 1).toJavaSet(),
                BillingTerm.Month, Set.of(2)
        );
        final var validationConfig = ValidationConfig.<Bean2, ValidationFailure>toValidate().withSpec(spec ->
                spec._2().nameForTest(invalidComboSpec)
                        .when(Bean2::getBt)
                        .then(Bean2::getValueStr)
                        .shouldRelateWith(validComboMap)
                        .orFailWithFn((value, valueStr) -> getFailureWithParams(MSG_WITH_PARAMS, value, valueStr)))
                .prepare();
        final var validBean = new Bean2(BillingTerm.OneTime, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidComboSpec).map(spec -> spec.test(validBean)).orElse(false));

        final var inValidBean = new Bean2(BillingTerm.Month, null);
        assertFalse(validationConfig.getSpecWithName(invalidComboSpec).map(spec -> spec.test(inValidBean)).orElse(true));
    }

    @Test
    void multiSpec2Test() {
        final var validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().specify(spec -> List.of(
                spec.<Integer, String>_2().when(Bean::getValue)
                        .matches(is(1))
                        .then(Bean::getValueStr)
                        .shouldMatch(either(is("one")).or(is("1")))
                        .orFailWith(INVALID_COMBO_1),
                spec.<Integer, String>_2().when(Bean::getValue)
                        .matches(is(2))
                        .then(Bean::getValueStr)
                        .shouldMatch(either(is("two")).or(is("2")))
                        .orFailWith(INVALID_COMBO_2)))
                .prepare();

        final var invalidBean1 = new Bean(1, "a", null, null);
        final var failureResult1 = Runner.validateAndFailFast(invalidBean1, ValidationFailure::getValidationFailureForException, validationConfig);
        assertThat(failureResult1).contains(INVALID_COMBO_1);

        final var invalidBean2 = new Bean(2, "b", null, null);
        final var failureResult2 = Runner.validateAndFailFast(invalidBean2, ignore -> NONE, validationConfig);
        assertThat(failureResult2).contains(INVALID_COMBO_2);

        final var validBean1 = new Bean(1, "one", null, null);
        final var noneResult1 = Runner.validateAndFailFast(validBean1, ignore -> NONE, validationConfig);
        assertThat(noneResult1).isEmpty();

        final var validBean2 = new Bean(2, "two", null, null);
        final var noneResult2 = Runner.validateAndFailFast(validBean2, ValidationFailure::getValidationFailureForException, validationConfig);
        assertThat(noneResult2).isEmpty();
    }

    @Test
    void spec2WithName() {
        final var invalidCombo1 = "invalidCombo1";
        final var invalidCombo2 = "invalidCombo2";
        final var validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().specify(spec -> List.of(
                spec.<Integer, String>_2().nameForTest(invalidCombo1)
                        .orFailWith(INVALID_COMBO_1)
                        .when(Bean::getValue).matches(is(1))
                        .then(Bean::getValueStr).shouldMatch(either(is("one")).or(is("1"))),
                spec.<Integer, String>_2().nameForTest(invalidCombo2)
                        .orFailWith(INVALID_COMBO_2)
                        .when(Bean::getValue).matches(is(2))
                        .then(Bean::getValueStr).shouldMatch(either(is("two")).or(is("2")))))
                .prepare();
        final var invalidBean1 = new Bean(1, "a", null, null);
        assertFalse(validationConfig.getSpecWithName(invalidCombo1).map(spec -> spec.test(invalidBean1)).orElse(true));

        final var invalidBean2 = new Bean(2, "b", null, null);
        assertFalse(validationConfig.getSpecWithName(invalidCombo2).map(spec -> spec.test(invalidBean2)).orElse(true));

        final var validBean1 = new Bean(1, "one", null, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidCombo1).map(spec -> spec.test(validBean1)).orElse(false));

        final var validBean2 = new Bean(2, "two", null, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidCombo2).map(spec -> spec.test(validBean2)).orElse(false));
    }

    @Value
    private static class Bean {
        Integer value;
        String valueStr;
        Integer dependentValue1;
        Integer dependentValue2;
    }

    private enum BillingTerm {
        OneTime, Month
    }

    @Value
    private static class Bean2 {
        BillingTerm bt;
        String valueStr;
    }
}
