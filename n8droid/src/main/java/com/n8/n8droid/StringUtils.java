package com.n8.n8droid;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by nate7313 on 8/7/15.
 */
public class StringUtils {
  private static final String TAG = StringUtils.class.getSimpleName();

  public static final String NULL_STRING = "NULL_STRING";

  public static final String LIST_DELIMITER = ",";

  /**
   * Returns whether or not the string is not empty.
   *
   * @param str The String to check for emptiness
   * @return Whether or not the string is not empty
   */
  public static boolean isNotEmpty(String str) {
    return !isEmpty(str);
  }

  /**
   * Returns whether or not the string is empty.
   *
   * Note: returns true if the string has a value of "null". This is a workaround for a bug in the ArcGIS Android SDK
   * (actually in the jackson JSON library): https://devtopia.esri.com/runtime/java-android-api/issues/624?source=cc
   *
   * @param str The String to check for emptiness
   * @return Whether or not the string is empty
   */
  public static boolean isEmpty(String str) {
    return str == null || str.isEmpty() || str.equalsIgnoreCase("null");
  }

  /**
   * Returns whether or not the string is null.If string is a 'null' string, it returns true.
   *
   * @param str The String to check for null
   * @return Whether or not the string is null
   */
  public static final boolean isNull(String str) {
    return str == null || str.equalsIgnoreCase("null");
  }

  /**
   * Guarantees the returned String is not null by returning either the given string if it is not null, or defaultValue
   * otherwise. Will turn "null" into defaultValue as well.
   *
   * @param str The String to check for null
   * @param defaultValue A default value to return if the given String is null or "null"
   * @return A guaranteed non-null string
   */
  public static String notNull(String str, String defaultValue) {
    return str == null || str.equalsIgnoreCase("null") ? defaultValue : str;
  }

  public static final boolean endsWithIgnoreCase(String str, String suffix) {
    int suffixCount = suffix.length();
    return str.regionMatches(true, str.length() - suffixCount, suffix, 0, suffixCount);
  }

  /**
   * Guarantees the returned String is not null by returning either the given string if it is not null, or an empty
   * string otherwise. Will turn "null" into the empty string as well.
   *
   * @param str The String to check for null or "null"
   * @return A guaranteed non-null string
   */
  public static String notNull(String str) {
    return notNull(str, "");
  }

  /**
   * Returns true if the two strings are equal. Returns false if either string is null.
   * @param string1 First string to compare
   * @param string2 Second string to compare
   * @return True if the two strings are equal; false otherwise
   */
  public static boolean areEqual(String string1, String string2) {
    if (string1 == null || string2 == null) {
      return false;
    }

    return string1.equals(string2);
  }

  /**
   * Returns true if the two strings are equal while ignoring case. Returns false if either string is null.
   * @param string1 First string to compare
   * @param string2 Second string to compare
   * @return True if the two strings are equal while ignoring case; false otherwise
   */
  public static boolean areEqualIgnoreCase(String string1, String string2) {
    if (string1 == null || string2 == null) {
      return false;
    }

    return string1.equalsIgnoreCase(string2);
  }

  /**
   * Convert an inputStream into a String.
   *
   * @param is InputStream from which to extract the string
   * @return String contained in the InputStream
   */
  public static String convertStreamToString(InputStream is) {
    StringBuilder content = new StringBuilder();

    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String line = null;
      while ((line = reader.readLine()) != null) {
        content.append(line);
        content.append("\n");
      }
    } catch (IOException e) {
      Log.w(TAG, "convertStreamToString exception", e);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        Log.w(TAG, "convertStreamToString exception", e);
      }
    }
    return content.toString();
  }

  /**
   * Concatenate the strings into a single String using a StringBuilder
   *
   * @param strings Strings to concatenate
   * @return A String containing a concatenation of all the strings in parameter
   */
  public static String concat(String... strings) {
    if (strings == null || strings.length == 0) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (String str : strings) {
      sb.append(str);
    }
    return sb.toString();
  }

  /**
   * Convenience method to iterate through a List of Strings, checking if the List contains a String that equals the
   * comparison string in a case insensitive check.
   *
   * @param list     The List<String> to do the case insensitive check on
   * @param contains The String to compare against
   * @return If the List contains the comaprison string regardless of case
   * @see String#equalsIgnoreCase(String)
   */
  public static boolean containsIgnoreCase(List<String> list, String contains) {
    ObjectUtils.requireNonNull(contains, "The comparison string cannot be null.");

    if (list == null) {
      return false;
    }

    for (String str : list) {
      if (contains.equalsIgnoreCase(str)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Given a list of strings, this method joins them together along with a default delimiter string (See
   * {@link #LIST_DELIMITER}). If you want to define a different delimiter, use {@link #joinListToString(java.util.List, String)}.
   *
   * @param list The list of strings to join together using a default delimiter
   * @return A string of joined together sub-strings from the list
   */
  public static String joinListToString(List<String> list) {
    return joinListToString(list, LIST_DELIMITER);
  }

  /**
   * Given a list of strings, this method joins them together along with the provided delimiter string.
   *
   * @param list      The list of strings to join together
   * @param delimiter The string used to delimit each string entry from the list
   * @return A string of joined together sub-strings from the list
   */
  public static String joinListToString(List<String> list, String delimiter) {
    ObjectUtils.requireNonNull(delimiter, "Delimiter string cannot be null");

    if (list == null || list.isEmpty()) {
      return "";
    }

    StringBuilder joinedList = new StringBuilder();

    int size = list.size();
    for (int i = 0; i < size; i++) {
      joinedList.append(list.get(i));

      if (i < size - 1) {
        joinedList.append(delimiter);
      }
    }

    return joinedList.toString();
  }

  /**
   * Splits the given string into a list of strings using the a default delimiter (See {@link #LIST_DELIMITER}). If you
   * want to define a different delimiter, use {@link #splitStringToList(String, String)}.
   *
   * @param toSplit A string to be split by the delimiter
   * @return A list of strings from the provided split string
   */
  public static List<String> splitStringToList(String toSplit) {
    return splitStringToList(toSplit, StringUtils.LIST_DELIMITER);
  }

  /**
   * Splits the given string into a list of strings using the provided delimiter.
   *
   * @param toSplit   A string to be split by the delimiter
   * @param delimiter A string used to split the given string
   * @return A list of strings from the provided split string
   */
  public static List<String> splitStringToList(String toSplit, String delimiter) {
    if (StringUtils.isEmpty(toSplit)) {
      return Collections.emptyList();
    }

    return Arrays.asList(toSplit.split(delimiter));
  }
}
