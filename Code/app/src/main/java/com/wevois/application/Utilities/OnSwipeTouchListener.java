package com.wevois.application.Utilities;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.wevois.application.Interface.SwipeListenerInterface;

public class OnSwipeTouchListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;
    SwipeListenerInterface swipeListenerInterface;

    public OnSwipeTouchListener(Context ctx, SwipeListenerInterface swipeListenerInterface) {
        gestureDetector = new GestureDetector(ctx, new GestureListener(this));
        this.swipeListenerInterface = swipeListenerInterface;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

}