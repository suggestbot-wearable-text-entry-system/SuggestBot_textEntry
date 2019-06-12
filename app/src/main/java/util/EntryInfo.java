package util;


/**
 * Created by PCPC on 2016-08-28.
 */
public class EntryInfo extends Object {
    private int inputDelay;
    private char what;

    public EntryInfo(char c, int time){
        inputDelay = time;
        what = c;
    }

    @Override
    public String toString(){
        return ""+what +","+inputDelay;
    }
}
