package com.cms.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSeekBar;


public class MyTimeProgressBar extends AppCompatSeekBar {
    Paint paint = new Paint();
    Path path = new Path();

    public MyTimeProgressBar(Context context) {
        super(context);
    }

    public MyTimeProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTimeProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(){
        path.moveTo(-30, 30);
        path.lineTo(30, 30);
        path.lineTo(0, 0);
        path.close();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        int thumb_x = (int) (( (double)this.getProgress()/this.getMax() ) * (double)this.getWidth());
        float middle = (float) (this.getHeight());

        paint.setColor(getContext().getResources().getColor(android.R.color.holo_blue_dark));
        paint.setTextSize(20);
        c.drawText("" + this.getProgress(), thumb_x, middle, paint);

        path.offset(thumb_x, middle);
        c.drawPath(path, paint);
    }
}
