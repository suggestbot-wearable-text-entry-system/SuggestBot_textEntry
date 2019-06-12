package Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import Event.OnWatchTouchEventListener;
import Event.WatchTouchEvent_Ext;
import hcil.kaist.watchconnector_holdDemo.MainActivity;
import util.ScreenInfo;
import util.keyboardLayout.KeyboardHoldingWindow;

/**
 * Created by PCPC on 2016-07-12.
 */
public class KeyboardView extends View implements OnWatchTouchEventListener {
    //const
    public static final int up = 2;
    public static final int down = 6;
    public static final int right = 4;
    public static final int left = 0;

    //System
    Handler mainHandler;
    Context context;

    //Cursor
    WatchTouchEvent_Ext prevEvent, currentEvent;
    protected Paint paintScreenCursor;
    protected Paint paintScreenCursorBound;
    int cursorSize, cursorBoundSize;
    public Paint paintEdgeCursor;

    protected int[] scrCursorPosOnKeyboard;

    //mode
    public boolean isGesture;

    //Drawing Element
    private Rect wholeScreen;
    private Paint paintWholeScreen;
    private Paint paintWait;

    //keyboard

    public KeyboardHoldingWindow keyboard;

    public KeyboardView(Context paramContext) {
        super(paramContext);
    }

    public KeyboardView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    public KeyboardView(Context paramContext, AttributeSet paramAttributeSet,
                        int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
    }

    public void initialize(Context c, Handler h){
        context = c;
        mainHandler = h;
        isGesture = false;
        cursorSize = ScreenInfo.screenHeight < ScreenInfo.screenWidth ? ScreenInfo.screenHeight : ScreenInfo.screenWidth;
        cursorSize = (int)(cursorSize * 0.01);
        cursorBoundSize = cursorSize * 3;
        paintScreenCursor = new Paint();
        paintScreenCursor.setColor(Color.argb(250, 255, 160, 050));
        paintScreenCursor.setStyle(Paint.Style.FILL);
        paintScreenCursorBound = new Paint();
        paintScreenCursorBound.setColor(Color.argb(150, 255, 160, 50));
        paintScreenCursorBound.setStyle(Paint.Style.STROKE);
        paintScreenCursorBound.setStrokeWidth(cursorSize);

        paintEdgeCursor = new Paint();
        paintEdgeCursor.setColor(Color.argb(255, 55, 155, 255));
        paintEdgeCursor.setStyle(Paint.Style.FILL);

        scrCursorPosOnKeyboard = new int[2];

        prevEvent = new WatchTouchEvent_Ext();

        wholeScreen = new Rect(0,0,ScreenInfo.screenWidth, ScreenInfo.screenHeight);
        paintWholeScreen = new Paint();
        paintWholeScreen.setColor(Color.argb(200,0,0,0));


        keyboard = new KeyboardHoldingWindow(c);

    }
    @Override
    public void onDraw(Canvas c){
        keyboard.drawKeyboard(c);
        keyboard.drawWindow(c);
        drawCursor(c);

        Thread.currentThread();
        try {
            if(prevEvent.state_screen > WatchTouchEvent_Ext.SCREEN_STATE_UP) {
                paintScreenCursorBound.setAlpha(255);
            }else{
                int alp = paintScreenCursorBound.getAlpha();
                if(alp > 0){
                    alp -= 5;
                }
                paintScreenCursorBound.setAlpha(alp);
            }
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        postInvalidate();

    }

    public void changeKeyboardMode(){
        keyboard.changeKeyboardMode();
    }



    public void drawCursor(Canvas c){
        if(prevEvent.state_screen > WatchTouchEvent_Ext.SCREEN_STATE_UP) {
            if(keyboard.isActivated()){
                c.drawCircle(scrCursorPosOnKeyboard[0], scrCursorPosOnKeyboard[1], cursorSize, paintScreenCursor);
                c.drawCircle(scrCursorPosOnKeyboard[0], scrCursorPosOnKeyboard[1], cursorBoundSize, paintScreenCursorBound);
            }else {
                c.drawCircle(prevEvent.xPos, prevEvent.yPos, cursorSize, paintScreenCursor);
                c.drawCircle(prevEvent.xPos, prevEvent.yPos, cursorBoundSize, paintScreenCursorBound);
            }
        }else{
            c.drawCircle(scrCursorPosOnKeyboard[0], scrCursorPosOnKeyboard[1], cursorSize, paintScreenCursor);
            c.drawCircle(scrCursorPosOnKeyboard[0], scrCursorPosOnKeyboard[1], cursorBoundSize, paintScreenCursorBound);
        }
    }

    // To handle a MSG from a Smartwatch touch input event
    @Override
    public WatchTouchEvent_Ext OnWatchTouchEvent(WatchTouchEvent_Ext e) {

        //keyboard.inputEdge(0, e.ePos);
        switch (e.state_screen){
            case WatchTouchEvent_Ext.SCREEN_STATE_DOWN:
                isGesture = false;
                e.selected = keyboard.downScreen(e.xPos, e.yPos);
                if(keyboard.isActivated()){
                    calcScrCursorOnKeyboard(e.xPos, e.yPos);
                }
                prevEvent = e;
                break;
            case WatchTouchEvent_Ext.SCREEN_STATE_UP:
                e.selected = keyboard.upScreen(e.xPos, e.yPos);
                if(!isGesture && Character.isAlphabetic(e.selected)) {
                    mainHandler.obtainMessage(MainActivity.MESSAGE_INPUT, String.valueOf(e.selected)).sendToTarget();
                    e.gesture = 10;
                }else if(isGesture){
                    switch (e.gesture){
                        case 0: //left swipe, deleting
                            e.selected = '-';
                            break;
                        case 2: // right swipe, spacing
                            e.selected = '>';
                            break;
                        case 9: // Enter key gesture,
                            e.selected = '^';
                            break;
                    }
                }
                prevEvent = e;
                break;
            case WatchTouchEvent_Ext.SCREEN_STATE_MOVE:
                e.selected = keyboard.moveScreen(e.xPos, e.yPos);
                if(keyboard.isActivated()){
                    calcScrCursorOnKeyboard(e.xPos, e.yPos);
                }
                break;
        }
        return e;
    }

    public void onEdgeTouchEvent(int ePos){
        keyboard.inputEdge(0, ePos);
    }

    private void calcScrCursorOnKeyboard(int x, int y){
        scrCursorPosOnKeyboard = keyboard.scrCursorOnKeyboard(x,y);
    }


    public void gestureInput(){
        isGesture = true;
        keyboard.upScreen(-1, -1);
        prevEvent.state_screen = WatchTouchEvent_Ext.SCREEN_STATE_UP;
    }

}
