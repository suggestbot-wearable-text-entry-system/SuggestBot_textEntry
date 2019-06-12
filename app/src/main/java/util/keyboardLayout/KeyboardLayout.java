package util.keyboardLayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import hcil.kaist.watchconnector_holdDemo.R;
import util.ScreenInfo;

/**
 * Created by PCPC on 2016-07-10.
 */
public class KeyboardLayout {
    //keyboard control params
    public static final int KEYBOARD_SPLITBOARD = 10;
    int keyboardMode;
    int keyboardWidth, keyboardHeight;
    int leftMargin, topMargin;
    protected Drawable[] gradientKeysBound;

    //side keyboard
    protected int[] keyDepth;
    protected boolean[] keyExistAt;

    //screen keyboard
    protected String[] qwertyKey;
    protected int qwertykeyVSize = 32;
    protected int qwertykeyHSize = 26;

    protected Rect keyboardBound;
    protected Paint boundPaint;
    int keyHeight;

    //keys
    protected int numberOfPhysicalKey = 9;
    protected int xOffset;
    protected int yOffset = 0;
    protected int keySize;
    protected  int gap;
    protected int numberOfKey = 27;
    protected Rect[] keysBound;
    protected Paint[] keysBoundPaint;
    protected String[] keys;
    protected Point[] keysPosition;
    protected Paint[] keysPaint;
    int yNum;
    int xNum;

    //key control
    protected int activatedID;
    protected int focusedID;


    protected int i,k,n,m,x,y;
    public void KeyboardLayout(int screenWidth, int screenHeight){

    }

    public int getMode(){
        return keyboardMode;
    }

    public void initKeyboard(int mode){
        focusedID = -1;
        keyboardMode = mode;


        splitboard();
    }

    public void splitboard(){
        xNum = 9;
        yNum = 3;
        numberOfPhysicalKey = 27;
        numberOfKey = 27;
        keysBound = new Rect[numberOfPhysicalKey];
        keysBoundPaint = new Paint[numberOfPhysicalKey];

        gap = 3;
        //keySize = (ScreenInfo.screenWidth - 8*gap)/9;
        keySize = (ScreenInfo.screenWidth - 8*gap)/9;
        keyHeight = (int)(keySize * 0.7);
        keyboardHeight = keyHeight * yNum + gap *(yNum - 1);
        keyboardWidth = keySize * xNum + gap * (xNum-1);
        xOffset = 0;
        yOffset = 0;

        int leftPos, topPos;
        for(y = 0 ; y < yNum; y++){
            topPos = yOffset + (y * keyHeight) + y*gap;
            for(x = 0 ; x < xNum; x++){
                //left, top, right, bottom
                leftPos = xOffset + (x * keySize) + x*gap;
                keysBound[y*xNum+ x]= new Rect(leftPos, topPos, leftPos + keySize, topPos + keyHeight);
            }
        }


        for(i = 0; i < numberOfPhysicalKey; i++){
            keysBoundPaint[i]  = new Paint();
            keysBoundPaint[i].setStrokeWidth(0);
            keysBoundPaint[i].setStyle(Paint.Style.FILL_AND_STROKE);
            keysBoundPaint[i].setColor(Color.LTGRAY);

        }

        keys = new String[numberOfKey];
        keysPaint = new Paint[numberOfKey];
        keysPosition = new Point[numberOfKey];

        String keyArray = "qwertyuioasdfghjkpzxcvbnml.";
        for(i = 0; i < numberOfKey; i++){
            keys[i] = String.valueOf((char)(keyArray.charAt(i)));

            keysPaint[i] = new Paint();
            keysPaint[i].setTextAlign(Paint.Align.CENTER);
            keysPaint[i].setColor(Color.BLACK);
            keysPaint[i].setTextSize(1);
            keysPaint[i].setTypeface(Typeface.SERIF);

        }
        //(float)(keysBound[i].height() * 0.9);
        int margin = (int)(keyHeight - keysPaint[0].getTextSize() ) /2;
        for(i = 0; i<  numberOfKey;i++){
                keysPosition[i]= new Point(keysBound[i].centerX(), (int)(keysBound[i].centerY()));
        }

        activatedID = -1;
        keyboardResize(xOffset, yOffset, 1);

    }

    public int getKeyboardHeight(){
        return keyboardHeight;

    }

    public int getKeyboardWidth(){
        return keyboardWidth;
    }

    public void drawKeyboard(Canvas c){
        //c.drawRect(keyboardBound, boundPaint);

        for(k = 0; k < numberOfPhysicalKey; k++){
            gradientKeysBound[k].draw(c);
        }
        for(i = 0; i < numberOfKey; i++) {
            //keysPaint[i].setTextSize(30);
            c.drawText(keys[i],keysPosition[i].x, keysPosition[i].y, keysPaint[i]);
        }
    }


    public boolean isActivated(){
        return activatedID > -1;
    }

    public void keyboardResize(float viewRatio){
        keyboardResize(xOffset, yOffset, viewRatio);
    }
    public void keyboardResize(int xOffset, int yOffset, float viewRatio){
        this.xOffset = xOffset;
        this.yOffset = yOffset;

        gap = (int)(3 * viewRatio);
        if(gap < 1) gap = 1;
        //keySize = (ScreenInfo.screenWidth - 8*gap)/9;
        keySize = (int)((ScreenInfo.screenWidth - 8*gap)/9 * viewRatio);
        if(viewRatio > 0.8) {
            keyHeight = (int) (keySize * 0.7);
        }
        else{
            keyHeight = keySize;
        }
        keyboardHeight = keyHeight * yNum + gap *(yNum - 1);
        keyboardWidth = keySize * xNum + gap * (xNum-1);

        int leftPos, topPos;
        for(y = 0 ; y < yNum; y++){
            topPos = yOffset + (y * keyHeight) + y*gap;
            for(x = 0 ; x < xNum; x++){
                //left, top, right, bottom
                leftPos = xOffset + (x * keySize) + x*gap;
                int id = y*xNum+ x;
                keysBound[id]= new Rect(leftPos, topPos, leftPos + keySize, topPos + keyHeight);

            }
        }


        for(i = 0; i < numberOfKey; i++){
            //matching to the textSize on textView
            keysPaint[i].setTextSize(30);
            keysPaint[i].setTypeface(Typeface.SERIF);
        }

        int margin = (int)(keyHeight - keysPaint[0].getTextSize() ) /2;
        for(i = 0; i<  numberOfKey;i++){
            keysPosition[i]= new Point(keysBound[i].centerX(), (int)(keysBound[i].centerY() + margin));
        }
        if(gradientKeysBound != null){
            for (int i = 0; i < numberOfPhysicalKey; i++){
                gradientKeysBound[i].setBounds(keysBound[i]);
            }
        }

    }

    public void moveOffset(int x, int y){

    }

    public void setGradientKeys(Context c){
        gradientKeysBound = new Drawable[numberOfPhysicalKey];
        for(int i = 0 ; i < 9; i++){
            gradientKeysBound[i*3 + 0] = c.getResources().getDrawable(R.drawable.gradation_rb_left);
            gradientKeysBound[i*3 + 2] = c.getResources().getDrawable(R.drawable.gradation_rb_right);
            gradientKeysBound[i*3 + 1] = c.getResources().getDrawable(R.drawable.gradation_rb_center);
        }
        for(int i = 0; i < numberOfPhysicalKey; i++){
            gradientKeysBound[i].setBounds(keysBound[i]);
            keysPaint[i].setColor(Color.WHITE);

        }
    }


    public int dpToPixel(int dp){

        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ScreenInfo.displayMetrics);


    }

}
