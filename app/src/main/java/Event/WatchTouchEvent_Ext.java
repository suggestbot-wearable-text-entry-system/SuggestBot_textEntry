package Event;

/**
 * Created by PCPC on 2017-01-23.
 * MSG object for containing MSG from a watch
 */

public class WatchTouchEvent_Ext {
    public static final int SCREEN_STATE_UP = 0;
    public static final int SCREEN_STATE_DOWN = 1;
    public static final int SCREEN_STATE_MOVE = 2;

    public static final int EDGE_STATE_UP = 0;
    public static final int EDGE_STATE_DOWN = 1;
    public static final int EDGE_STATE_MOVE = 2;

    public int state_screen;
    public int xPos;
    public int yPos;
    public int ePos; //edge position
    public int gesture;
    public char selected;

    public long eventTime;

    public WatchTouchEvent_Ext(){
        xPos = -1;
        yPos = -1;
        ePos = -1;
        gesture = -1;
        selected = (char)0;
        state_screen  = SCREEN_STATE_UP;
        eventTime = System.currentTimeMillis();
    }
    public WatchTouchEvent_Ext(WatchTouchEvent_Ext e){
        xPos = e.xPos;
        yPos = e.yPos;
        ePos = e.ePos;
        gesture = e.gesture;
        selected = e.selected;
        state_screen  = e.state_screen;
        eventTime = e.eventTime;
    }
    public WatchTouchEvent_Ext(int gesture){
        xPos = -1;
        yPos = -1;
        ePos = -1;
        this.gesture = gesture;
        selected = (char)0;
        state_screen  = SCREEN_STATE_UP;
        eventTime = System.currentTimeMillis();
    }

    public WatchTouchEvent_Ext(int screenState, int x, int y, int e){
        state_screen = screenState;
        xPos = x;
        yPos = y;
        ePos = e;
        selected = (char)0;

        gesture = -1;
        eventTime = System.currentTimeMillis();
    }

    @Override
    public String toString(){
        return "xPos: " + xPos + " yPos: " + yPos + " ePos: "+ePos;
    }
}
