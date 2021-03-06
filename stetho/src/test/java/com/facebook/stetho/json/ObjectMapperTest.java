/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.stetho.json;

import android.os.Build;
import com.facebook.stetho.json.annotation.JsonProperty;
import com.facebook.stetho.json.annotation.JsonValue;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ObjectMapper}
 */
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN)
@RunWith(RobolectricTestRunner.class)
public class ObjectMapperTest {

  private ObjectMapper mObjectMapper;

  @Before
  public void setup() {
    mObjectMapper = new ObjectMapper();
  }

  @Test
  public void testJsonProperty() throws IOException, JSONException {
    JsonPropertyString c = new JsonPropertyString();
    c.testString = "test";
    String expected = "{\"testString\":\"test\"}";

    JSONObject jsonObject = mObjectMapper.convertValue(c, JSONObject.class);
    String str = jsonObject.toString();
    assertEquals(expected, str);

    JsonPropertyString jsonPropertyString = mObjectMapper.convertValue(
        new JSONObject(expected),
        JsonPropertyString.class);
    assertEquals(c, jsonPropertyString);
  }

  @Test
  public void testNestedProperty() throws JSONException {
    NestedJsonProperty njp = new NestedJsonProperty();
    njp.child1 = new JsonPropertyString();
    njp.child2 = new JsonPropertyInt();

    njp.child1.testString = "testString";
    njp.child2.i = 4;

    String expected = "{\"child1\":{\"testString\":\"testString\"},\"child2\":{\"i\":4}}";
    NestedJsonProperty parsed = mObjectMapper.convertValue(
        new JSONObject(expected),
        NestedJsonProperty.class);
    assertEquals(njp, parsed);

    JSONObject jsonObject = mObjectMapper.convertValue(njp, JSONObject.class);
    assertEquals(expected, jsonObject.toString());
  }

  @Test
  public void testEnumProperty() throws JSONException {
    JsonPropertyEnum jpe = new JsonPropertyEnum();
    jpe.enumValue = TestEnum.VALUE_TWO;

    String expected = "{\"enumValue\":\"two\"}";

    JsonPropertyEnum parsed = mObjectMapper.convertValue(
        new JSONObject(expected),
        JsonPropertyEnum.class);
    assertEquals(jpe, parsed);

    JSONObject jsonObject = mObjectMapper.convertValue(jpe, JSONObject.class);
    assertEquals(expected, jsonObject.toString());
  }

  @Test
  public void testListString() throws JSONException {
    JsonPropertyStringList jpsl = new JsonPropertyStringList();
    List<String> values = new ArrayList<String>();
    jpsl.stringList = values;
    values.add("one");
    values.add("two");
    values.add("three");

    String expected = "{\"stringList\":[\"one\",\"two\",\"three\"]}";
    JsonPropertyStringList jsonPropertyStringList = mObjectMapper.convertValue(
        new JSONObject(expected),
        JsonPropertyStringList.class);
    assertEquals(jpsl, jsonPropertyStringList);

    JSONObject jsonObject = mObjectMapper.convertValue(jpsl, JSONObject.class);
    String str = jsonObject.toString();

    assertEquals(expected, str);
  }

  @Test
  public void testSerializeMultitypedList() throws JSONException {
    List<Object> list = new ArrayList<Object>();
    list.add("foo");
    list.add(Collections.singletonList("bar"));
    JsonPropertyMultitypedList javaObj = new JsonPropertyMultitypedList();
    javaObj.multitypedList = list;

    String expected = "{\"multitypedList\":[\"foo\",[\"bar\"]]}";
    JSONObject jsonObj = mObjectMapper.convertValue(javaObj, JSONObject.class);
    String str = jsonObj.toString();

    assertEquals(expected, str);
  }

  @Test
  public void testSerializeListOfLists() throws JSONException {
    List<List<String>> listOfLists = new ArrayList<List<String>>();
    listOfLists.add(Collections.singletonList("foo"));
    ArrayList<String> sublist2 = new ArrayList<String>();
    sublist2.add("1");
    sublist2.add("2");
    listOfLists.add(sublist2);
    JsonPropertyListOfLists javaObj = new JsonPropertyListOfLists();
    javaObj.listOfLists = listOfLists;

    String expected = "{\"listOfLists\":[[\"foo\"],[\"1\",\"2\"]]}";
    JSONObject jsonObj = mObjectMapper.convertValue(javaObj, JSONObject.class);
    String str = jsonObj.toString();

    assertEquals(expected, str);
  }

  public static class NestedJsonProperty {
    @JsonProperty(required = true)
    public JsonPropertyString child1;

    @JsonProperty
    public JsonPropertyInt child2;

    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof NestedJsonProperty)) {
        return false;
      }
      return Objects.equals(child1, ((NestedJsonProperty) o).child1) &&
          Objects.equals(child2, ((NestedJsonProperty) o).child2);
    }
  }

  public static class JsonPropertyString {
    @JsonProperty
    public String testString;

    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof JsonPropertyString)) {
        return false;
      }
      return Objects.equals(testString, ((JsonPropertyString) o).testString);
    }
  }

  public static class JsonPropertyInt {
    @JsonProperty
    public int i;

    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof JsonPropertyInt)) {
        return false;
      }
      return Objects.equals(i, ((JsonPropertyInt) o).i);
    }
  }

  public static class JsonPropertyEnum {
    @JsonProperty
    public TestEnum enumValue;

    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof JsonPropertyEnum)) {
        return false;
      }
      return Objects.equals(enumValue, ((JsonPropertyEnum) o).enumValue);
    }
  }

  public static class JsonPropertyStringList {
    @JsonProperty
    public List<String> stringList;

    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof JsonPropertyStringList)) {
        return false;
      }
      JsonPropertyStringList rhs = (JsonPropertyStringList) o;
      if (stringList == null || rhs.stringList == null) {
        return stringList == rhs.stringList;
      }
      if (stringList.size() != rhs.stringList.size()) {
        return false;
      }
      ListIterator<String> myIter = stringList.listIterator();
      ListIterator<String> rhsIter = rhs.stringList.listIterator();
      while (myIter.hasNext()) {
        if (!Objects.equals(myIter.next(), rhsIter.next())) {
          return false;
        }
      }

      return true;
    }
  }

  public enum TestEnum {
    VALUE_ONE("one"),
    VALUE_TWO("two"),
    VALUE_THREE("three");

    private final String mValue;

    private TestEnum(String str) {
      mValue = str;
    }

    @JsonValue
    public String getValue() {
      return mValue;
    }
  }

  private static class JsonPropertyMultitypedList {
    @JsonProperty
    public List<Object> multitypedList;
  }

  private static class JsonPropertyListOfLists {
    @JsonProperty
    public List<List<String>> listOfLists;
  }
}
