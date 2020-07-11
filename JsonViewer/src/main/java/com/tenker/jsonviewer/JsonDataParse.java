package com.tenker.jsonviewer;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class JsonDataParse {
  public static int TEXT_COLOR = Color.parseColor("#9ACD32");
  public static int NUMBER_COLOR = Color.parseColor("#8968CD");
  public static int BOOLEAN_COLOR = Color.parseColor("#FFCC11");
  public static int NULL_COLOR = Color.parseColor("#999999");

  public class JsonData {
    public static final int OBJ = 1;
    public static final int ARRAY = 2;
    public static final int NULL = 3;
    public static final int PRIMITIVE = 4;
    public static final int SYMBOL = 5;

    public int type;
    public int deep;
    public JsonElement jsonElement;
    public String key = "";
    public List<JsonData> contains;
    private boolean isExpand;
    public JsonData parent;

    public void addContains(JsonData jsonData) {
      if (contains == null) {
        contains = new ArrayList<>();
      }
      contains.add(jsonData);
    }

    public void removeMyContains(List<JsonData> jsonDatas) {
      if (contains == null || contains.isEmpty()) {
        return;
      }
      jsonDatas.removeAll(contains);
      isExpand = false;
      for (JsonData jsonData : contains) {
        jsonData.removeMyContains(jsonDatas);
      }
    }

    public boolean isContainMyData(List<JsonData> jsonDatas) {
      if (contains == null || contains.isEmpty()) {
        return false;
      }
      if (jsonDatas.containsAll(contains)) {
        return true;
      } else {
        for (JsonData jsonData : contains) {
          boolean containMyData = jsonData.isContainMyData(jsonDatas);
          if (containMyData) {
            return containMyData;
          }
        }
      }
      return false;
    }

    public void expandMe(List<JsonData> jsonData, int deep) {
      if (contains == null || contains.isEmpty()) {
        return;
      }
      jsonData.addAll(jsonData.indexOf(this) + 1, contains);
      if (deep > 0) {
        for (JsonData innerData : contains) {
          innerData.expandMe(jsonData, deep - 1);
        }
      }
      isExpand = true;
    }

    public CharSequence getDescribe() {
      StringBuilder describe = new StringBuilder();
      for (int i = 0; i < deep; i++) {
        describe.append("        ");
      }

      if (key != null && !key.trim().isEmpty()) {

        if (type != JsonData.SYMBOL) {
          describe.append("\"");
        }

        describe.append(key);

        if (type != JsonData.SYMBOL) {
          describe.append("\"");
        }

        if (jsonElement != null) {
          describe.append(" : ");
        }
      }
      if (jsonElement != null) {
        if (jsonElement.isJsonArray()) {
          if (isExpand) {
            return stringHighlight(
                describe.append("- [").toString(),
                Collections.singletonList("-"),
                Collections.singletonList(Color.RED));
          } else {
            return stringHighlight(
                describe
                    .append(String.format("+ ARRAY[%s]", jsonElement.getAsJsonArray().size()))
                    .toString(),
                Collections.singletonList("+ ARRAY"),
                Collections.singletonList(Color.RED));
          }
        } else if (jsonElement.isJsonNull()) {
          return stringHighlight(
              describe.append("null").toString(),
              Collections.singletonList("null"),
              Collections.singletonList(NULL_COLOR));
        } else if (jsonElement.isJsonObject()) {
          if (isExpand) {
            return stringHighlight(
                describe.append("- {").toString(),
                Collections.singletonList("-"),
                Collections.singletonList(Color.RED));
          } else {
            return stringHighlight(
                describe.append("+ OBJECT{...}").toString(),
                Collections.singletonList("+ OBJECT"),
                Collections.singletonList(Color.RED));
          }
        } else if (jsonElement.isJsonPrimitive()) {
          JsonPrimitive asJsonPrimitive = jsonElement.getAsJsonPrimitive();
          String value = asJsonPrimitive.getAsString();
          if (asJsonPrimitive.isBoolean()) {
            return stringHighlight(
                describe.append(value).toString(),
                Collections.singletonList(value),
                Collections.singletonList(BOOLEAN_COLOR));
          } else if (asJsonPrimitive.isNumber()) {
            return stringHighlight(
                describe.append(value).toString(),
                Collections.singletonList(value),
                Collections.singletonList(NUMBER_COLOR));
          } else if (asJsonPrimitive.isString()) {
            value = "\"" + value + "\"";
            return stringHighlight(
                describe.append(value).toString(),
                Collections.singletonList(value),
                Collections.singletonList(TEXT_COLOR));
          }
          return describe.append(value).toString();
        }
      }
      return describe.toString();
    }
  }

  public JsonData parse(JsonElement jsonElement) {
    JsonData jsonData = new JsonData();
    parseJsonElement(jsonElement, jsonData);
    return jsonData;
  }

  public void parseJsonElement(JsonElement jsonElementParam, JsonData jsonDataParam) {
    jsonDataParam.jsonElement = jsonElementParam;
    if (jsonElementParam.isJsonObject()) {
      jsonDataParam.type = JsonData.OBJ;
      JsonObject asJsonObject = jsonElementParam.getAsJsonObject();
      Set<Entry<String, JsonElement>> entries = asJsonObject.entrySet();
      for (Entry<String, JsonElement> entry : entries) {
        JsonData jsonData = new JsonData();
        jsonData.parent = jsonDataParam;
        jsonData.deep = jsonDataParam.deep + 1;
        jsonData.key = entry.getKey();
        jsonDataParam.addContains(jsonData);
        parseJsonElement(entry.getValue(), jsonData);
      }
      JsonData jsonData = new JsonData();
      jsonData.type = JsonData.SYMBOL;
      jsonData.deep = jsonDataParam.deep;
      jsonData.key = "}";
      jsonDataParam.addContains(jsonData);
    } else if (jsonElementParam.isJsonArray()) {
      jsonDataParam.type = JsonData.ARRAY;
      JsonArray asJsonArray = jsonElementParam.getAsJsonArray();
      for (JsonElement json : asJsonArray) {
        JsonData jsonData = new JsonData();
        jsonData.deep = jsonDataParam.deep + 1;
        jsonData.parent = jsonDataParam;
        jsonDataParam.addContains(jsonData);
        parseJsonElement(json, jsonData);
      }
      JsonData jsonData = new JsonData();
      jsonData.type = JsonData.SYMBOL;
      jsonData.deep = jsonDataParam.deep;
      jsonData.key = "]";
      jsonDataParam.addContains(jsonData);

    } else if (jsonElementParam.isJsonNull()) {
      jsonDataParam.type = JsonData.NULL;
    } else if (jsonElementParam.isJsonPrimitive()) {
      jsonDataParam.type = JsonData.PRIMITIVE;
    }
  }

  public static SpannableStringBuilder stringHighlight(
      String str, List<String> highlight, List<Integer> color) {
    SpannableStringBuilder ssb = new SpannableStringBuilder(str);
    for (int i = 0; i < highlight.size(); i++) {
      String s = highlight.get(i);
      int index = str.indexOf(s);
      if (index != -1) {
        ssb.setSpan(
            new ForegroundColorSpan(color.get(i)),
            index,
            index + s.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    }
    return ssb;
  }
}
