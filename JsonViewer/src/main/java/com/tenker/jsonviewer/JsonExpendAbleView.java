package com.tenker.jsonviewer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.tenker.jsonviewer.JsonDataParse.JsonData;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class JsonExpendAbleView extends FrameLayout {

  public TextView title;
  public TextView content;

  public boolean expand = false;
  private RecyclerView list;
  private View wrapContent;
  private List<JsonData> jsonDatas;
  private JsonDataAdapter jsonDataAdapter;

  public JsonExpendAbleView(Context context) {
    super(context);
    initView();
  }

  public JsonExpendAbleView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView();
  }

  public JsonExpendAbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView();
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
  }

  private void initView() {
    inflate(getContext(), R.layout.debug_tool_expand_able_view, this);
    title = findViewById(R.id.title);
    content = findViewById(R.id.content);
    wrapContent = findViewById(R.id.wrap_content);
    list = findViewById(R.id.list);
    list.setLayoutManager(new LinearLayoutManager(getContext()));
    title.setOnClickListener(v -> setExpand(!expand));
  }

  public void setExpand(boolean expand) {
    this.expand = expand;
    if (expand) {
      wrapContent.setVisibility(VISIBLE);
    } else {
      wrapContent.setVisibility(GONE);
    }
  }

  public void renderData(List<JsonData> jsonData) {
    this.jsonDatas = jsonData;
    content.setVisibility(INVISIBLE);
    jsonDataAdapter = new JsonDataAdapter();
    list.setAdapter(jsonDataAdapter);
    list.setVisibility(VISIBLE);
  }

  public void renderJson(String title, String jsonString) {
    this.title.setText(title);
    JsonDataParse.JsonData jsonData;
    try {
      jsonData = new JsonDataParse().parse(JsonParser.parseString(jsonString));
    } catch (JsonParseException exception) {
      this.title.setText("JsonParseException");
      jsonData =
          new JsonDataParse().parse(JsonParser.parseString(new Gson().toJson(exception)));
    }
    List<JsonDataParse.JsonData> jsonDataList = new ArrayList<>();
    jsonDataList.add(jsonData);
    jsonData.expandMe(jsonDataList, 3);
    renderData(jsonDataList);
    setExpand(true);
  }

  class JsonDataAdapter extends RecyclerView.Adapter<JsonDataViewHolder> {

    @NonNull
    @Override
    public JsonDataViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
      return new JsonDataViewHolder(
          LayoutInflater.from(viewGroup.getContext())
              .inflate(R.layout.debug_tool_json_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull JsonDataViewHolder jsonDataViewHolder, int i) {
      jsonDataViewHolder.bindDataAndRefresh(jsonDatas.get(i));
    }

    @Override
    public int getItemCount() {
      return jsonDatas.size();
    }
  }

  Gson prettyJson = new GsonBuilder().setPrettyPrinting().create();

  class JsonDataViewHolder extends RecyclerView.ViewHolder {

    private JsonData jsonData;
    TextView text;

    public JsonDataViewHolder(@NonNull View itemView) {
      super(itemView);
      text = itemView.findViewById(R.id.text);
      itemView.setOnLongClickListener(
          v -> {
            if (jsonData.isContainMyData(jsonDatas)) {
              jsonData.removeMyContains(jsonDatas);
              jsonData.expandMe(jsonDatas, 100);
              jsonDataAdapter.notifyDataSetChanged();
            } else {
              jsonData.expandMe(jsonDatas, 100);
              jsonDataAdapter.notifyDataSetChanged();
            }

            copyData();
            return true;
          });
      itemView.setOnClickListener(
          v -> {
            if (jsonData.isContainMyData(jsonDatas)) {
              jsonData.removeMyContains(jsonDatas);
              jsonDataAdapter.notifyDataSetChanged();
            } else {
              jsonData.expandMe(jsonDatas, 1);
              jsonDataAdapter.notifyDataSetChanged();
            }
          });
    }

    private void copyData() {
      ClipboardManager clipboardManager =
          (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
      if (clipboardManager != null) {
        String prettyJson = null;
        if (!jsonData.jsonElement.isJsonPrimitive()) {
          prettyJson = JsonExpendAbleView.this.prettyJson.toJson(jsonData.jsonElement);
        } else {
          prettyJson = jsonData.jsonElement.getAsString();
        }
        try {
          // 创建普通字符型ClipData
          ClipData mClipData = ClipData.newPlainText("prettyJson", prettyJson);
          // 将ClipData内容放到系统剪贴板里。
          clipboardManager.setPrimaryClip(mClipData);
          Toast.makeText(getContext(), "复制成功!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
          Toast.makeText(getContext(), "复制失败!", Toast.LENGTH_SHORT).show();
        }
      }
    }

    public void bindDataAndRefresh(JsonData jsonData) {
      this.jsonData = jsonData;
      text.setText(jsonData.getDescribe());
    }
  }
}
