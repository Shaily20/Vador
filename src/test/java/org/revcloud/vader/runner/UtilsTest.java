package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.DUPLICATE_ITEM;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static consumer.failure.ValidationFailure.NULL_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.revcloud.vader.runner.Utils.handleNullValidatablesAndDuplicates;

import com.force.swag.id.ID;
import consumer.failure.ValidationFailure;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.Value;
import org.junit.jupiter.api.Test;

class UtilsTest {

  @Test
  void filterInvalidatablesAndFailDuplicates() {
    final List<Bean> invalidValidatables = List.of(null, null);
    final var duplicateValidatables =
        List.of(
            new Bean(new ID("802xx000001ni4xAAA")),
            new Bean(new ID("802xx000001ni4x")),
            new Bean(new ID("802xx000001ni4x")));
    final var validatables =
        invalidValidatables
            .appendAll(duplicateValidatables)
            .appendAll(
                List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesWith(container -> container.getId().get18CharIdIfValid())
            .andFailDuplicatesWith(DUPLICATE_ITEM)
            .prepare();
    final var results =
        List.ofAll(
            handleNullValidatablesAndDuplicates(
                validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig));

    final var failedInvalids = results.take(2);
    assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);
    final var failedDuplicates = results.drop(2).take(3);
    assertThat(failedDuplicates).allMatch(r -> r.getLeft() == DUPLICATE_ITEM);

    final var valids = results.drop(5);
    assertTrue(valids.forAll(Either::isRight));
    valids.forEachWithIndex(
        (r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId().toString()));
  }

  @Test
  void failInvalidatablesAndFilterDuplicates() {
    final List<Bean> invalidValidatables = List.of(null, null);
    final var duplicateValidatables =
        List.of(
            new Bean(new ID("802xx000001ni4xAAA")),
            new Bean(new ID("802xx000001ni4x")),
            new Bean(new ID("802xx000001ni4x")));
    final var validatables =
        invalidValidatables
            .appendAll(duplicateValidatables)
            .appendAll(
                List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesWith(container -> container.getId().get18CharIdIfValid())
            .prepare();
    final var results =
        List.ofAll(
            handleNullValidatablesAndDuplicates(
                validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig));

    assertThat(results).hasSize(5);
    final var failedInvalids = results.take(2);
    assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);

    final var valids = results.drop(2);
    assertTrue(valids.forAll(Either::isRight));
    valids.forEachWithIndex(
        (r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId().toString()));
  }

  @Test
  void failInvalidatablesAndNullKeysAndFilterDuplicates() {
    final List<Bean> invalidValidatables = List.of(null, null);
    final var duplicateValidatables =
        List.of(
            new Bean(new ID("802xx000001ni4xAAA")),
            new Bean(new ID("802xx000001ni4x")),
            new Bean(new ID("802xx000001ni4x")));
    final var validatablesWithNullKeys = List.of(new Bean(null), new Bean(null));
    final var validatables =
        invalidValidatables
            .appendAll(duplicateValidatables)
            .appendAll(validatablesWithNullKeys)
            .appendAll(
                List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesWith(
                container ->
                    container.getId() == null ? null : container.getId().get18CharIdIfValid())
            .andFailNullKeysWith(NULL_KEY)
            .prepare();
    final var results =
        List.ofAll(
            handleNullValidatablesAndDuplicates(
                validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig));

    assertThat(results).hasSize(validatables.size() - duplicateValidatables.size());
    final var failedInvalids = results.take(2);
    assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);

    final var nullKeyInvalids = results.drop(2).take(2);
    assertThat(nullKeyInvalids).allMatch(r -> r.getLeft() == NULL_KEY);

    final var valids = results.drop(4);
    assertTrue(valids.forAll(Either::isRight));
    valids.forEachWithIndex(
        (r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId().toString()));
  }

  @Test
  void failInvalidatablesAndPassNullKeysAndFilterDuplicates() {
    final List<Bean> invalidValidatables = List.of(null, null);
    final var duplicateValidatables =
        List.of(
            new Bean(new ID("802xx000001ni4xAAA")),
            new Bean(new ID("802xx000001ni4x")),
            new Bean(new ID("802xx000001ni4x")));
    final var validatablesWithNullKeys = List.of(new Bean(null), new Bean(null));
    final var validatables =
        invalidValidatables
            .appendAll(duplicateValidatables)
            .appendAll(validatablesWithNullKeys)
            .appendAll(
                List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesWith(
                container ->
                    container.getId() == null ? null : container.getId().get18CharIdIfValid())
            .prepare();
    final var results =
        List.ofAll(
            handleNullValidatablesAndDuplicates(
                validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig));

    assertThat(results).hasSize(validatables.size() - duplicateValidatables.size());
    final var failedInvalids = results.take(2);
    assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);

    final var nullKeyInvalids = results.drop(2).take(2);
    assertThat(nullKeyInvalids).allMatch(r -> r.get().equals(new Bean(null)));

    final var valids = results.drop(4);
    assertTrue(valids.forAll(Either::isRight));
    valids.forEachWithIndex(
        (r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId().toString()));
  }

  @Test
  void filterInvalidatablesAndFailDuplicatesForAllOrNoneInvalidValidatables() {
    final List<Bean> invalidValidatables = List.of(null, null);
    final var duplicateValidatables =
        List.of(
            new Bean(new ID("802xx000001ni4xAAA")),
            new Bean(new ID("802xx000001ni4x")),
            new Bean(new ID("802xx000001ni4x")));
    final var validatables =
        invalidValidatables
            .appendAll(duplicateValidatables)
            .appendAll(
                List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesWith(container -> container.getId().toString())
            .andFailDuplicatesWith(DUPLICATE_ITEM)
            .prepare();
    final var result =
        Utils.findFistNullValidatableOrDuplicate(
            validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);
    assertThat(result).contains(NOTHING_TO_VALIDATE);
  }

  @Test
  void filterInvalidatablesAndDuplicatesForAllOrNone() {
    final var duplicateValidatables =
        List.of(new Bean(new ID("0")), new Bean(new ID("0")), new Bean(new ID("0")));
    final var validatables =
        duplicateValidatables.appendAll(
            List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesWith(container -> container.getId().toString())
            .prepare();
    final var result =
        Utils.findFistNullValidatableOrDuplicate(
            validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);
    assertThat(result).isEmpty();
  }

  @Test
  void filterInvalidatablesAndDuplicatesAndFailNullKeysForAllOrNone() {
    final var duplicateValidatables =
        List.of(new Bean(new ID("0")), new Bean(new ID("0")), new Bean(new ID("0")));
    final var nullKeyValidatables = List.of(new Bean(null), new Bean(null));
    final var validatables =
        duplicateValidatables
            .appendAll(nullKeyValidatables)
            .appendAll(
                List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesWith(
                container -> container.getId() == null ? null : container.getId().toString())
            .andFailNullKeysWith(NULL_KEY)
            .prepare();
    final var result =
        Utils.findFistNullValidatableOrDuplicate(
            validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);
    assertThat(result).contains(NULL_KEY);
  }

  @Test
  void filterInvalidatablesAndDuplicatesForAllOrNoneDuplicate() {
    final var duplicateValidatables =
        List.of(new Bean(new ID("0")), new Bean(new ID("0")), new Bean(new ID("0")));
    final var validatables =
        duplicateValidatables.appendAll(
            List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesWith(container -> container.getId().toString())
            .andFailDuplicatesWith(DUPLICATE_ITEM)
            .prepare();
    final var result =
        Utils.findFistNullValidatableOrDuplicate(
            validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);
    assertThat(result).contains(DUPLICATE_ITEM);
  }

  @Test
  void filterInvalidatablesAndDuplicatesForAllOrNoneAllValid() {
    final var validatables =
        List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3")));
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesWith(container -> container.getId().toString())
            .andFailDuplicatesWith(DUPLICATE_ITEM)
            .prepare();
    final var result =
        Utils.findFistNullValidatableOrDuplicate(
            validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);
    assertThat(result).isEmpty();
  }

  @Value
  private static class Bean {
    ID id;
  }
}
