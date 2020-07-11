package com.tenker.jsonviewer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/** Author:liaozhuohong<br> */
public class FreeTouchHorizontalScrollView extends HorizontalScrollView {

  public FreeTouchHorizontalScrollView(Context context) {
    super(context);
  }

  public FreeTouchHorizontalScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FreeTouchHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    onTouchEvent(ev);
    return super.dispatchTouchEvent(ev);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    super.onInterceptTouchEvent(ev);
    return false;
  }

  @Override
  public boolean onInterceptHoverEvent(MotionEvent event) {
    return super.onInterceptHoverEvent(event);
  }
}
