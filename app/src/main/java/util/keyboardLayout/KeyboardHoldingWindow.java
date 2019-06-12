package util.keyboardLayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.CountDownTimer;

import util.ScreenInfo;

/**
 * Created by PCPC on 2016-08-04.
 * HoldBoard interface class
 * Edge-touch sensitive smartwatch required
 * If you do not have a edge-touch sensitive device, should be modify code related a function, inputEdge(int,int).
 */
public class KeyboardHoldingWindow extends KeyboardLayout {


    protected int line1X, line1Y, line2X, line2Y, lineStartX, lineEndX , lineStartY, lineEndY, line0, line3;
    protected Paint linePaint;

    protected Path leftArr, rightArr;
    protected Paint paintArrowL, paintArrowR;
    protected Point guideTextL, guideTextR;
    protected String leftGuideText;
    protected String rightGuideText;
    protected Paint paintArrowL_text, paintArrowR_text;


    protected Rect window;
    protected Paint windowPaint;
    private int windowSize;

    private float keyboardRatio;
    private int xOffset1, xOffset2, xOffset3;
    private int windowBlock1, windowBlock2;


    private float widthR, heightR;
    private int activatedID3;

    private float convertX, convertY;
    private CountDownTimer cdt;

    private boolean gripActivated;
    private float currentRatio;
    private int prevX, prevY;

    private int tempFocusedID;
    private int prevScrID;

    public int alphaLevel = 130;
    public boolean isDefaultOppaque255 = true;

    public char currentTarget;
    public boolean modeDefault;
    public KeyboardHoldingWindow(Context c){
        initKeyboard(KEYBOARD_SPLITBOARD);

        modeDefault = true;
        currentTarget = ' ';
        currentRatio = 1;
        gripActivated = false;
        windowPaint = new Paint();
        windowPaint.setColor(Color.RED);
        windowPaint.setAlpha(0);
        windowPaint.setStyle(Paint.Style.STROKE);
        windowPaint.setStrokeWidth(10);
        linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(10);
        //line0 = xOffset;

        keyboardRatio = 0.4f;


        windowSize = keySize*3 + gap *2;
        windowSize *= keyboardRatio;

        window = new Rect(0,0,0,0);
        int temp =  ScreenInfo.screenWidth/2 - windowSize*5/2 - gap;

        xOffset1  = temp  - windowSize - gap;
        //xOffset1 = 0;
        xOffset2 = xOffset1 + windowSize + gap ;
        xOffset3 = xOffset2 + windowSize + gap ;
        line3 = xOffset3 + windowSize + gap ;
        xOffset = xOffset2;
        keyboardResize(keyboardRatio);
        window.set(xOffset3, yOffset, xOffset3 + windowSize, yOffset + keyboardHeight);

        line1X = window.left + gap + keySize;
        line2X = line1X + gap + keySize;
        line1Y = window.top+ gap + keySize;
        line2Y = line1Y + gap + keySize;
        activatedID3 = -1;
        widthR = (float)3 / ScreenInfo.screenWidth;
        heightR = (float)3 / ScreenInfo.screenHeight;
        convertX = (float)window.width() / ScreenInfo.screenWidth;
        convertY = (float)window.height() / ScreenInfo.screenHeight;


        initGuideArrow();


        cdt = new CountDownTimer(20, 2) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(isActivated()) this.cancel();
            }

            @Override
            public void onFinish() {
                tempFocusedID = focusedID;
                focusedID = -1;
            }
        };

        setGradientKeys(c);


    }

    public void initGuideArrow(){

        int len = windowSize / 4;
        leftArr = new Path();
        leftArr.moveTo(window.centerX() - len, window.bottom+gap);
        leftArr.lineTo(window.left, window.bottom + gap + keySize / 4);
        leftArr.lineTo(window.centerX() - len, window.bottom + gap + keySize / 2);
        leftArr.lineTo(window.centerX() - len, window.bottom + gap + keySize / 2 - keySize / 8);
        leftArr.lineTo(window.centerX() - len / 3, window.bottom + gap + keySize/2 - keySize/8);
        leftArr.lineTo(window.centerX() - len/3, window.bottom + gap + keySize/8);
        leftArr.lineTo(window.centerX() - len, window.bottom + gap + keySize/8);
        leftArr.lineTo(window.centerX() - len, window.bottom + gap);

        rightArr = new Path();
        rightArr.moveTo(window.centerX() + len, window.bottom + gap);
        rightArr.lineTo(window.right, window.bottom + gap + keySize / 4);
        rightArr.lineTo(window.centerX() + len, window.bottom + gap + keySize / 2);
        rightArr.lineTo(window.centerX() + len, window.bottom + gap + keySize / 2 - keySize / 8);
        rightArr.lineTo(window.centerX() + len / 3, window.bottom + gap + keySize / 2 - keySize / 8);
        rightArr.lineTo(window.centerX() + len / 3, window.bottom + gap + keySize / 8);
        rightArr.lineTo(window.centerX() + len, window.bottom + gap + keySize / 8);
        rightArr.lineTo(window.centerX() + len, window.bottom+gap);

        paintArrowL = new Paint();
        paintArrowL.setStrokeWidth(gap);
        paintArrowL.setColor(Color.RED);
        paintArrowL.setStyle(Paint.Style.FILL);
        paintArrowR = new Paint();
        paintArrowR.setStrokeWidth(gap);
        paintArrowR.setColor(Color.RED);
        paintArrowR.setStyle(Paint.Style.FILL);


        leftGuideText = "Delete";
        rightGuideText = "Space";

        guideTextL = new Point(window.centerX() - len/3 - 2*gap, window.bottom+gap + keySize/4 + keySize/8 - gap *2);
        guideTextR = new Point(window.centerX() + len/3 + 2*gap, window.bottom+gap + keySize/4 + keySize/8 - gap*2);
        paintArrowL_text = new Paint();
        paintArrowL_text.setTextSize(keySize / 3);
        paintArrowL_text.setColor(Color.WHITE);
        paintArrowL_text.setTextAlign(Paint.Align.RIGHT);
        paintArrowR_text = new Paint();
        paintArrowR_text.setTextSize(keySize / 3);
        paintArrowR_text.setColor(Color.WHITE);
        paintArrowR_text.setTextAlign(Paint.Align.LEFT);

    }

    public void drawArrow(Canvas c){

        c.drawPath(leftArr, paintArrowL);
        c.drawPath(rightArr, paintArrowR);
        c.drawText(leftGuideText, guideTextL.x, guideTextL.y, paintArrowL_text);
        c.drawText(rightGuideText, guideTextR.x, guideTextR.y, paintArrowR_text);
    }

    public void drawWindow(Canvas c){
        if(windowPaint.getAlpha() > 100) {
            c.drawRect(window, windowPaint);
            c.drawLine(line1X, window.top, line1X, window.bottom, linePaint);
            c.drawLine(line2X, window.top, line2X, window.bottom, linePaint);
            c.drawLine(window.left, line1Y, window.right, line1Y, linePaint);
            c.drawLine(window.left, line2Y, window.right, line2Y, linePaint);
        }else{
            drawArrow(c);
        }
    }

    //QWERTY-like layout initializer
    public void setKeyLayoutDefault(){

        String keyArray = "qwertyuioasdfghjkpzxcvbnml.";
        for(i = 0; i < numberOfKey; i++){
            keys[i] = String.valueOf((char)(keyArray.charAt(i)));

        }
    }
    //UFO layout initializer, it is designed for less keyboard switching, and also considering memorability of keyboard layout
    public void setKeyLayoutUFO(){

        String keyArray = "ufotheqvwbpmarsx.zljyingcdk";
        for(i = 0; i < numberOfKey; i++){
            keys[i] = String.valueOf((char)(keyArray.charAt(i)));

        }
    }

    public void changeKeyboardMode(){
        if(modeDefault){
            setKeyLayoutUFO();
            modeDefault = false;
        }else{
            setKeyLayoutDefault();
            modeDefault = true;
        }
    }

    public int getxOffset(){
        return xOffset;
    }
    public int getyOffset(){
        return yOffset;
    }

    public char moveScreen(int xPos, int yPos){
        boolean isActivated = activatedID > -1;
        int temp = focusedID;
        if(isActivated){
            int xID = (int)(xPos * widthR);
            int yID = (int)(yPos * heightR);
            if(xID > 2) xID = 2;

            prevScrID = yID* 9 + xID;
            focusedID = prevScrID + activatedID3;
            if(focusedID != temp){
                //keysBoundPaint[prevFocusedID].setColor(Color.LTGRAY);
                try {
                    keysPaint[temp].setColor(Color.WHITE);
                    //keysPaint[temp].setAlpha(alphaLevel);
                    gradientKeysBound[temp].clearColorFilter();
                    keysPaint[focusedID].setColor(Color.RED);
                    gradientKeysBound[focusedID].setColorFilter(Color.WHITE, PorterDuff.Mode.ADD);
                }catch (Exception e){
                    focusedID = -1;
                }
            }
        }

        if(focusedID < 0){
            return (char)0;
        }else return  keys[focusedID].charAt(0);
    }

    public char downScreen(int xPos, int yPos){
        boolean isActivated = activatedID > -1;
        if(isActivated){
            int xID = (int)(xPos * widthR);
            int yID = (int)(yPos * heightR);
            if(xID > 2) xID = 2;
            prevScrID = yID* 9 + xID;
            focusedID = prevScrID + activatedID3;
            /*
            keysPaint[prevScrID].setAlpha(255);
            keysPaint[prevScrID + 3].setAlpha(255);
            keysPaint[prevScrID + 6].setAlpha(255);*/
            try {
                keysPaint[focusedID].setColor(Color.RED);
                gradientKeysBound[focusedID].setColorFilter(Color.WHITE, PorterDuff.Mode.ADD);
            }catch (Exception e){
                focusedID = -1;
            }

        }

        if(focusedID < 0){
            return (char)0;
        }else return  keys[focusedID].charAt(0);
    }
    public char upScreen(int xPos, int yPos){
        boolean isActivated = activatedID > -1;
        int temp = focusedID;
        if(isActivated){
            try {
                keysPaint[focusedID].setColor(Color.WHITE);
                gradientKeysBound[focusedID].clearColorFilter();
            }catch(Exception e){
            }
        }
        focusedID = -1;
        if(temp < 0){
            return (char)0;
        }else return  keys[temp].charAt(0);
    }

    public void doneActivate(){
        boolean isActivated = activatedID > -1;
        int temp = focusedID;
        if(isActivated){
            try {
                keysPaint[focusedID].setColor(Color.WHITE);
                gradientKeysBound[focusedID].clearColorFilter();
            }catch(Exception e){
            }
        }
        focusedID = -1;
    }

    //To handle a touch input from an edge-touch
    public boolean inputEdge(int where, int position) {
        if (position > xOffset1) {
            if (position < xOffset2) {
                if (activatedID != 0) {
                    windowPaint.setColor(Color.RED);
                    linePaint.setColor(Color.LTGRAY);
                    activatedID = 0;
                    activatedID3 = 0;

                    keyboardReposition(xOffset3, yOffset);
                    //window.set(xOffset, yOffset, xOffset + windowSize, yOffset + keyboardHeight);

                    keysPaint[0].setAlpha(255);
                    gradientKeysBound[0].setAlpha(255);
                    keysPaint[1].setAlpha(255);
                    gradientKeysBound[1].setAlpha(255);
                    keysPaint[2].setAlpha(255);
                    gradientKeysBound[2].setAlpha(255);
                    keysPaint[9].setAlpha(255);
                    gradientKeysBound[9].setAlpha(255);
                    keysPaint[10].setAlpha(255);
                    gradientKeysBound[10].setAlpha(255);
                    keysPaint[11].setAlpha(255);
                    gradientKeysBound[11].setAlpha(255);
                    keysPaint[18].setAlpha(255);
                    gradientKeysBound[18].setAlpha(255);
                    keysPaint[19].setAlpha(255);
                    gradientKeysBound[19].setAlpha(255);
                    keysPaint[20].setAlpha(255);
                    gradientKeysBound[20].setAlpha(255);

                    keysPaint[3].setAlpha(alphaLevel);
                    gradientKeysBound[3].setAlpha(alphaLevel);
                    keysPaint[4].setAlpha(alphaLevel);
                    gradientKeysBound[4].setAlpha(alphaLevel);
                    keysPaint[5].setAlpha(alphaLevel);
                    gradientKeysBound[5].setAlpha(alphaLevel);
                    keysPaint[12].setAlpha(alphaLevel);
                    gradientKeysBound[12].setAlpha(alphaLevel);
                    keysPaint[13].setAlpha(alphaLevel);
                    gradientKeysBound[13].setAlpha(alphaLevel);
                    keysPaint[14].setAlpha(alphaLevel);
                    gradientKeysBound[14].setAlpha(alphaLevel);
                    keysPaint[21].setAlpha(alphaLevel);
                    gradientKeysBound[21].setAlpha(alphaLevel);
                    keysPaint[22].setAlpha(alphaLevel);
                    gradientKeysBound[22].setAlpha(alphaLevel);
                    keysPaint[23].setAlpha(alphaLevel);
                    gradientKeysBound[23].setAlpha(alphaLevel);

                    keysPaint[6].setAlpha(alphaLevel);
                    gradientKeysBound[6].setAlpha(alphaLevel);
                    keysPaint[7].setAlpha(alphaLevel);
                    gradientKeysBound[7].setAlpha(alphaLevel);
                    keysPaint[8].setAlpha(alphaLevel);
                    gradientKeysBound[8].setAlpha(alphaLevel);
                    keysPaint[15].setAlpha(alphaLevel);
                    gradientKeysBound[15].setAlpha(alphaLevel);
                    keysPaint[16].setAlpha(alphaLevel);
                    gradientKeysBound[16].setAlpha(alphaLevel);
                    keysPaint[17].setAlpha(alphaLevel);
                    gradientKeysBound[17].setAlpha(alphaLevel);
                    keysPaint[24].setAlpha(alphaLevel);
                    gradientKeysBound[24].setAlpha(alphaLevel);
                    keysPaint[25].setAlpha(alphaLevel);
                    gradientKeysBound[25].setAlpha(alphaLevel);
                    keysPaint[26].setAlpha(alphaLevel);
                    gradientKeysBound[26].setAlpha(alphaLevel);

                    if (focusedID > -1) {
                        //keysBoundPaint[prevFocusedID].setColor(Color.LTGRAY);
                        keysPaint[focusedID].setColor(Color.WHITE);
                        gradientKeysBound[focusedID].setAlpha(alphaLevel);
                        gradientKeysBound[focusedID].clearColorFilter();
                    }
                }
            } else if (position < xOffset3) {
                if (activatedID != 1) {
                    windowPaint.setColor(Color.RED);
                    linePaint.setColor(Color.LTGRAY);
                    activatedID = 1;
                    activatedID3 = 3;

                    keyboardReposition(xOffset2, yOffset);
                    //window.set(windowBlock1, yOffset, windowBlock1 + windowSize, yOffset + keyboardHeight);
                    keysPaint[0].setAlpha(alphaLevel);
                    gradientKeysBound[0].setAlpha(alphaLevel);
                    keysPaint[1].setAlpha(alphaLevel);
                    gradientKeysBound[1].setAlpha(alphaLevel);
                    keysPaint[2].setAlpha(alphaLevel);
                    gradientKeysBound[2].setAlpha(alphaLevel);
                    keysPaint[9].setAlpha(alphaLevel);
                    gradientKeysBound[9].setAlpha(alphaLevel);
                    keysPaint[10].setAlpha(alphaLevel);
                    gradientKeysBound[10].setAlpha(alphaLevel);
                    keysPaint[11].setAlpha(alphaLevel);
                    gradientKeysBound[11].setAlpha(alphaLevel);
                    keysPaint[18].setAlpha(alphaLevel);
                    gradientKeysBound[18].setAlpha(alphaLevel);
                    keysPaint[19].setAlpha(alphaLevel);
                    gradientKeysBound[19].setAlpha(alphaLevel);
                    keysPaint[20].setAlpha(alphaLevel);
                    gradientKeysBound[20].setAlpha(alphaLevel);

                    keysPaint[3].setAlpha(255);
                    gradientKeysBound[3].setAlpha(255);
                    keysPaint[4].setAlpha(255);
                    gradientKeysBound[4].setAlpha(255);
                    keysPaint[5].setAlpha(255);
                    gradientKeysBound[5].setAlpha(255);
                    keysPaint[12].setAlpha(255);
                    gradientKeysBound[12].setAlpha(255);
                    keysPaint[13].setAlpha(255);
                    gradientKeysBound[13].setAlpha(255);
                    keysPaint[14].setAlpha(255);
                    gradientKeysBound[14].setAlpha(255);
                    keysPaint[21].setAlpha(255);
                    gradientKeysBound[21].setAlpha(255);
                    keysPaint[22].setAlpha(255);
                    gradientKeysBound[22].setAlpha(255);
                    keysPaint[23].setAlpha(255);
                    gradientKeysBound[23].setAlpha(255);

                    keysPaint[6].setAlpha(alphaLevel);
                    gradientKeysBound[6].setAlpha(alphaLevel);
                    keysPaint[7].setAlpha(alphaLevel);
                    gradientKeysBound[7].setAlpha(alphaLevel);
                    keysPaint[8].setAlpha(alphaLevel);
                    gradientKeysBound[8].setAlpha(alphaLevel);
                    keysPaint[15].setAlpha(alphaLevel);
                    gradientKeysBound[15].setAlpha(alphaLevel);
                    keysPaint[16].setAlpha(alphaLevel);
                    gradientKeysBound[16].setAlpha(alphaLevel);
                    keysPaint[17].setAlpha(alphaLevel);
                    gradientKeysBound[17].setAlpha(alphaLevel);
                    keysPaint[24].setAlpha(alphaLevel);
                    gradientKeysBound[24].setAlpha(alphaLevel);
                    keysPaint[25].setAlpha(alphaLevel);
                    gradientKeysBound[25].setAlpha(alphaLevel);
                    keysPaint[26].setAlpha(alphaLevel);
                    gradientKeysBound[26].setAlpha(alphaLevel);
                    if (focusedID > -1) {
                        //keysBoundPaint[prevFocusedID].setColor(Color.LTGRAY);
                        keysPaint[focusedID].setColor(Color.WHITE);
                        gradientKeysBound[focusedID].setAlpha(alphaLevel);
                        gradientKeysBound[focusedID].clearColorFilter();
                    }
                }
            } else if (position < line3) {
                if (activatedID != 2) {
                    windowPaint.setColor(Color.RED);
                    linePaint.setColor(Color.LTGRAY);
                    activatedID = 2;
                    activatedID3 = 6;

                    keyboardReposition(xOffset1, yOffset);
                    //window.set(windowBlock2, yOffset, windowBlock2 + windowSize, yOffset + keyboardHeight);
                    keysPaint[0].setAlpha(alphaLevel);
                    gradientKeysBound[0].setAlpha(alphaLevel);
                    keysPaint[1].setAlpha(alphaLevel);
                    gradientKeysBound[1].setAlpha(alphaLevel);
                    keysPaint[2].setAlpha(alphaLevel);
                    gradientKeysBound[2].setAlpha(alphaLevel);
                    keysPaint[9].setAlpha(alphaLevel);
                    gradientKeysBound[9].setAlpha(alphaLevel);
                    keysPaint[10].setAlpha(alphaLevel);
                    gradientKeysBound[10].setAlpha(alphaLevel);
                    keysPaint[11].setAlpha(alphaLevel);
                    gradientKeysBound[11].setAlpha(alphaLevel);
                    keysPaint[18].setAlpha(alphaLevel);
                    gradientKeysBound[18].setAlpha(alphaLevel);
                    keysPaint[19].setAlpha(alphaLevel);
                    gradientKeysBound[19].setAlpha(alphaLevel);
                    keysPaint[20].setAlpha(alphaLevel);
                    gradientKeysBound[20].setAlpha(alphaLevel);

                    keysPaint[3].setAlpha(alphaLevel);
                    gradientKeysBound[3].setAlpha(alphaLevel);
                    keysPaint[4].setAlpha(alphaLevel);
                    gradientKeysBound[4].setAlpha(alphaLevel);
                    keysPaint[5].setAlpha(alphaLevel);
                    gradientKeysBound[5].setAlpha(alphaLevel);
                    keysPaint[12].setAlpha(alphaLevel);
                    gradientKeysBound[12].setAlpha(alphaLevel);
                    keysPaint[13].setAlpha(alphaLevel);
                    gradientKeysBound[13].setAlpha(alphaLevel);
                    keysPaint[14].setAlpha(alphaLevel);
                    gradientKeysBound[14].setAlpha(alphaLevel);
                    keysPaint[21].setAlpha(alphaLevel);
                    gradientKeysBound[21].setAlpha(alphaLevel);
                    keysPaint[22].setAlpha(alphaLevel);
                    gradientKeysBound[22].setAlpha(alphaLevel);
                    keysPaint[23].setAlpha(alphaLevel);
                    gradientKeysBound[23].setAlpha(alphaLevel);

                    keysPaint[6].setAlpha(255);
                    gradientKeysBound[6].setAlpha(255);
                    keysPaint[7].setAlpha(255);
                    gradientKeysBound[7].setAlpha(255);
                    keysPaint[8].setAlpha(255);
                    gradientKeysBound[8].setAlpha(255);
                    keysPaint[15].setAlpha(255);
                    gradientKeysBound[15].setAlpha(255);
                    keysPaint[16].setAlpha(255);
                    gradientKeysBound[16].setAlpha(255);
                    keysPaint[17].setAlpha(255);
                    gradientKeysBound[17].setAlpha(255);
                    keysPaint[24].setAlpha(255);
                    gradientKeysBound[24].setAlpha(255);
                    keysPaint[25].setAlpha(255);
                    gradientKeysBound[25].setAlpha(255);
                    keysPaint[26].setAlpha(255);
                    gradientKeysBound[26].setAlpha(255);
                    if (focusedID > -1) {
                        //keysBoundPaint[prevFocusedID].setColor(Color.LTGRAY);
                        keysPaint[focusedID].setColor(Color.WHITE);
                        gradientKeysBound[focusedID].setAlpha(alphaLevel);
                        gradientKeysBound[focusedID].clearColorFilter();
                    }
                }
            }else{
                activatedID = -1;
                if (focusedID > -1) {
                    //keysBoundPaint[prevFocusedID].setColor(Color.LTGRAY);
                    keysPaint[focusedID].setColor(Color.WHITE);
                    gradientKeysBound[focusedID].setAlpha(alphaLevel);
                    gradientKeysBound[focusedID].clearColorFilter();
                }
                windowPaint.setColor(Color.argb(0, 0, 0, 0));
                linePaint.setColor(windowPaint.getColor());
                keyboardReposition(xOffset2, yOffset);

            }
        } else {
            boundReset();
            cdt.start();
            //focusedID = -1;
        }
        return true;
    }


    public int[] scrCursorOnKeyboard(int x, int y){
        int[] result = new int[2];
        result[0] = (int)(window.left + x *convertX);
        result[1] = (int)(window.top + y*convertY);
        return result;
    }
    public boolean isActivated(){
        return activatedID > -1;
    }

    public Rect getWindow(){
        return window;
    }



    public void keyboard_line_CDgainAdjust(int offset, float ratio){
        int defaultUnit = keySize * 3 + gap*3;
        int unit = (int)(defaultUnit * ratio);
        /*
        line0 = offset -1;
        line1 = line0 + 1 + unit;
        line2 = line1 + unit;
        line3 = line2 + unit + 1;
        */
        line1X = offset + unit;
        line2X = line1X + unit;
    }

    public void keyboard_line_CDgainDefault(){
        line0 = 0;
        line1X = xOffset + keySize * 3 + gap*3;
        line2X = xOffset + keySize * 6 + gap*6;
        line3 = ScreenInfo.screenWidth+1;
    }

    @Override
    public void keyboardResize(float viewRatio){
        currentRatio = viewRatio;
        keyboardResize(xOffset, yOffset, viewRatio);
        windowSize = keySize*3 + gap *2;
        linePaint.setStrokeWidth(gap * 2);
        windowPaint.setStrokeWidth(gap*2);

        convertX = (float)window.width() / ScreenInfo.screenWidth;
        convertY = (float)window.height() / ScreenInfo.screenHeight;
    }

    public boolean onTouch(int x, int y){
        if(gripActivated || contains(x,y)){
            if(!gripActivated){
                gripActivated = true;
                windowPaint.setColor(Color.MAGENTA);
                prevX = x;
                prevY = y;
            }else{
                keyboardReposition(x - prevX, y - prevY);
                prevX = x;
                prevY = y;
            }
            return true;
        }else{
            gripActivated = false;
            windowPaint.setColor(Color.argb(0,0,0,0));
            return false;
        }

    }

    public void keyboardReposition(int repoX, int repoY){
        xOffset = repoX;
        //yOffset = repoY;
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

    public void onTouchUp(){
        if(gripActivated) {
            gripActivated = false;
            windowPaint.setColor(Color.argb(0,0,0,0));
        }
    }
    public boolean contains(int x, int y){
        if(x > xOffset && x < line3){
            if(y > window.top && y < window.bottom){
                return true;
            }
        }
        return false;
    }
    public void boundReset(){
        activatedID = -1;
        activatedID3 = -1;
        int alpha;
        if(isDefaultOppaque255){
            alpha = 255;
        }else{
            alpha = alphaLevel;
        }
        for(int i = 0 ; i < numberOfPhysicalKey; i++){
            gradientKeysBound[i].setAlpha(alpha);
            gradientKeysBound[i].clearColorFilter();
            keysPaint[i].setColor(Color.argb(255,255,255,255));
        }
        keyboardReposition(xOffset2, 0);

        //keyboardResize(xOffset, yOffset, keyboardRatio);
        windowPaint.setColor(Color.argb(0, 0, 0, 0));
        linePaint.setColor(windowPaint.getColor());
    }


    private void setWindow(){

    }
    public void resetPaintWindow() {
        switch (activatedID) {
            case 0:
                keysPaint[0].setAlpha(255);
                gradientKeysBound[0].setAlpha(255);
                keysPaint[1].setAlpha(255);
                gradientKeysBound[1].setAlpha(255);
                keysPaint[2].setAlpha(255);
                gradientKeysBound[2].setAlpha(255);
                keysPaint[9].setAlpha(255);
                gradientKeysBound[9].setAlpha(255);
                keysPaint[10].setAlpha(255);
                gradientKeysBound[10].setAlpha(255);
                keysPaint[11].setAlpha(255);
                gradientKeysBound[11].setAlpha(255);
                keysPaint[18].setAlpha(255);
                gradientKeysBound[18].setAlpha(255);
                keysPaint[19].setAlpha(255);
                gradientKeysBound[19].setAlpha(255);
                keysPaint[20].setAlpha(255);
                gradientKeysBound[20].setAlpha(255);

                keysPaint[3].setAlpha(alphaLevel);
                gradientKeysBound[3].setAlpha(alphaLevel);
                keysPaint[4].setAlpha(alphaLevel);
                gradientKeysBound[4].setAlpha(alphaLevel);
                keysPaint[5].setAlpha(alphaLevel);
                gradientKeysBound[5].setAlpha(alphaLevel);
                keysPaint[12].setAlpha(alphaLevel);
                gradientKeysBound[12].setAlpha(alphaLevel);
                keysPaint[13].setAlpha(alphaLevel);
                gradientKeysBound[13].setAlpha(alphaLevel);
                keysPaint[14].setAlpha(alphaLevel);
                gradientKeysBound[14].setAlpha(alphaLevel);
                keysPaint[21].setAlpha(alphaLevel);
                gradientKeysBound[21].setAlpha(alphaLevel);
                keysPaint[22].setAlpha(alphaLevel);
                gradientKeysBound[22].setAlpha(alphaLevel);
                keysPaint[23].setAlpha(alphaLevel);
                gradientKeysBound[23].setAlpha(alphaLevel);

                keysPaint[6].setAlpha(alphaLevel);
                gradientKeysBound[6].setAlpha(alphaLevel);
                keysPaint[7].setAlpha(alphaLevel);
                gradientKeysBound[7].setAlpha(alphaLevel);
                keysPaint[8].setAlpha(alphaLevel);
                gradientKeysBound[8].setAlpha(alphaLevel);
                keysPaint[15].setAlpha(alphaLevel);
                gradientKeysBound[15].setAlpha(alphaLevel);
                keysPaint[16].setAlpha(alphaLevel);
                gradientKeysBound[16].setAlpha(alphaLevel);
                keysPaint[17].setAlpha(alphaLevel);
                gradientKeysBound[17].setAlpha(alphaLevel);
                keysPaint[24].setAlpha(alphaLevel);
                gradientKeysBound[24].setAlpha(alphaLevel);
                keysPaint[25].setAlpha(alphaLevel);
                gradientKeysBound[25].setAlpha(alphaLevel);
                keysPaint[26].setAlpha(alphaLevel);
                gradientKeysBound[26].setAlpha(alphaLevel);

                break;
            case 1:

                keysPaint[0].setAlpha(alphaLevel);
                gradientKeysBound[0].setAlpha(alphaLevel);
                keysPaint[1].setAlpha(alphaLevel);
                gradientKeysBound[1].setAlpha(alphaLevel);
                keysPaint[2].setAlpha(alphaLevel);
                gradientKeysBound[2].setAlpha(alphaLevel);
                keysPaint[9].setAlpha(alphaLevel);
                gradientKeysBound[9].setAlpha(alphaLevel);
                keysPaint[10].setAlpha(alphaLevel);
                gradientKeysBound[10].setAlpha(alphaLevel);
                keysPaint[11].setAlpha(alphaLevel);
                gradientKeysBound[11].setAlpha(alphaLevel);
                keysPaint[18].setAlpha(alphaLevel);
                gradientKeysBound[18].setAlpha(alphaLevel);
                keysPaint[19].setAlpha(alphaLevel);
                gradientKeysBound[19].setAlpha(alphaLevel);
                keysPaint[20].setAlpha(alphaLevel);
                gradientKeysBound[20].setAlpha(alphaLevel);

                keysPaint[3].setAlpha(255);
                gradientKeysBound[3].setAlpha(255);
                keysPaint[4].setAlpha(255);
                gradientKeysBound[4].setAlpha(255);
                keysPaint[5].setAlpha(255);
                gradientKeysBound[5].setAlpha(255);
                keysPaint[12].setAlpha(255);
                gradientKeysBound[12].setAlpha(255);
                keysPaint[13].setAlpha(255);
                gradientKeysBound[13].setAlpha(255);
                keysPaint[14].setAlpha(255);
                gradientKeysBound[14].setAlpha(255);
                keysPaint[21].setAlpha(255);
                gradientKeysBound[21].setAlpha(255);
                keysPaint[22].setAlpha(255);
                gradientKeysBound[22].setAlpha(255);
                keysPaint[23].setAlpha(255);
                gradientKeysBound[23].setAlpha(255);

                keysPaint[6].setAlpha(alphaLevel);
                gradientKeysBound[6].setAlpha(alphaLevel);
                keysPaint[7].setAlpha(alphaLevel);
                gradientKeysBound[7].setAlpha(alphaLevel);
                keysPaint[8].setAlpha(alphaLevel);
                gradientKeysBound[8].setAlpha(alphaLevel);
                keysPaint[15].setAlpha(alphaLevel);
                gradientKeysBound[15].setAlpha(alphaLevel);
                keysPaint[16].setAlpha(alphaLevel);
                gradientKeysBound[16].setAlpha(alphaLevel);
                keysPaint[17].setAlpha(alphaLevel);
                gradientKeysBound[17].setAlpha(alphaLevel);
                keysPaint[24].setAlpha(alphaLevel);
                gradientKeysBound[24].setAlpha(alphaLevel);
                keysPaint[25].setAlpha(alphaLevel);
                gradientKeysBound[25].setAlpha(alphaLevel);
                keysPaint[26].setAlpha(alphaLevel);
                gradientKeysBound[26].setAlpha(alphaLevel);
                break;
            case 2:
                keysPaint[0].setAlpha(alphaLevel);
                gradientKeysBound[0].setAlpha(alphaLevel);
                keysPaint[1].setAlpha(alphaLevel);
                gradientKeysBound[1].setAlpha(alphaLevel);
                keysPaint[2].setAlpha(alphaLevel);
                gradientKeysBound[2].setAlpha(alphaLevel);
                keysPaint[9].setAlpha(alphaLevel);
                gradientKeysBound[9].setAlpha(alphaLevel);
                keysPaint[10].setAlpha(alphaLevel);
                gradientKeysBound[10].setAlpha(alphaLevel);
                keysPaint[11].setAlpha(alphaLevel);
                gradientKeysBound[11].setAlpha(alphaLevel);
                keysPaint[18].setAlpha(alphaLevel);
                gradientKeysBound[18].setAlpha(alphaLevel);
                keysPaint[19].setAlpha(alphaLevel);
                gradientKeysBound[19].setAlpha(alphaLevel);
                keysPaint[20].setAlpha(alphaLevel);
                gradientKeysBound[20].setAlpha(alphaLevel);

                keysPaint[3].setAlpha(alphaLevel);
                gradientKeysBound[3].setAlpha(alphaLevel);
                keysPaint[4].setAlpha(alphaLevel);
                gradientKeysBound[4].setAlpha(alphaLevel);
                keysPaint[5].setAlpha(alphaLevel);
                gradientKeysBound[5].setAlpha(alphaLevel);
                keysPaint[12].setAlpha(alphaLevel);
                gradientKeysBound[12].setAlpha(alphaLevel);
                keysPaint[13].setAlpha(alphaLevel);
                gradientKeysBound[13].setAlpha(alphaLevel);
                keysPaint[14].setAlpha(alphaLevel);
                gradientKeysBound[14].setAlpha(alphaLevel);
                keysPaint[21].setAlpha(alphaLevel);
                gradientKeysBound[21].setAlpha(alphaLevel);
                keysPaint[22].setAlpha(alphaLevel);
                gradientKeysBound[22].setAlpha(alphaLevel);
                keysPaint[23].setAlpha(alphaLevel);
                gradientKeysBound[23].setAlpha(alphaLevel);

                keysPaint[6].setAlpha(255);
                gradientKeysBound[6].setAlpha(255);
                keysPaint[7].setAlpha(255);
                gradientKeysBound[7].setAlpha(255);
                keysPaint[8].setAlpha(255);
                gradientKeysBound[8].setAlpha(255);
                keysPaint[15].setAlpha(255);
                gradientKeysBound[15].setAlpha(255);
                keysPaint[16].setAlpha(255);
                gradientKeysBound[16].setAlpha(255);
                keysPaint[17].setAlpha(255);
                gradientKeysBound[17].setAlpha(255);
                keysPaint[24].setAlpha(255);
                gradientKeysBound[24].setAlpha(255);
                keysPaint[25].setAlpha(255);
                gradientKeysBound[25].setAlpha(255);
                keysPaint[26].setAlpha(255);
                gradientKeysBound[26].setAlpha(255);
                break;
            default:
                boundReset();
                break;
        }

    }

}
