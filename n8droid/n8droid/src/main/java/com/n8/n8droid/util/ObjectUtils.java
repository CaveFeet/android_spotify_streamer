package com.n8.n8droid.util;

public class ObjectUtils {

  public static void requireState(boolean stateCondition, String errorMessage) throws IllegalStateException {
    if (!stateCondition) {
      throw new IllegalStateException(errorMessage);
    }
  }

  public static <T> T requireNonNull(T object, String errorMessage) throws NullPointerException {
    if (object == null) {
      throw new NullPointerException(errorMessage);
    }

    return object;
  }

  public static void requireArgumentSate(boolean argumentCondition, String errorMessage) throws IllegalArgumentException {
    if (!argumentCondition) {
      throw new IllegalStateException(errorMessage);
    }
  }
}
