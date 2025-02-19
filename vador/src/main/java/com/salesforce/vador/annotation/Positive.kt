package com.salesforce.vador.annotation

import com.salesforce.vador.types.ValidatorAnnotation1
import java.lang.reflect.Field

class Positive<FailureT> : ValidatorAnnotation1<Int, FailureT> {
  override fun validate(field: Field, value: Int, failure: FailureT, none: FailureT): FailureT {
    if (value < 1) {
      return failure
    }
    return none
  }
}
