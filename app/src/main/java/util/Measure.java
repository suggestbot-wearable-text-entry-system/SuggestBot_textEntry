package util;

import java.util.ArrayList;

import Event.WatchTouchEvent_Ext;


/**
 * Created by PCPC on 2016-08-08.
 */
public class Measure {

    private int block, trial;


    private long startTime;
    private long endTime;
    private long forWPM;
    private String goalString;
    private int MSD;

    private final char C = 0;
    private final char INF = 1;
    private final char IF = 2;
    private float TER, UER, CER; //total error rate, uncorrected error rate, corrected error rate
    private float WPM;
    private int[] userInputCount;
    private int backSpaceCount;
    private ArrayList<EntryInfo> userInput;
    private ArrayList<WatchTouchEvent_Ext> watchTouchEvents;
    private long currentTime;

    private boolean isRecording;
    LogPrinter logPrinter;
    public Measure(){
        backSpaceCount = 0;
        userInputCount = new int[3];
        WPM = -1;
        block = 0;
        trial = 0;

        watchTouchEvents = new ArrayList<WatchTouchEvent_Ext>();
        userInput = new ArrayList<EntryInfo>();
        logPrinter = new LogPrinter();
        logPrinter.setBlock(block);
        logPrinter.setTrial(trial);
        logPrinter.printStart();
        recordingDone();
    }
    public void setGoalString(String str){
        goalString = str;
    }

    public void userInputBackspace(){
        backSpaceCount++;
    }

    public void start(long current){
        startTime = current;
        backSpaceCount = 0;
        WPM = -1;
    }

    public void done(char input, long current){
        currentTime = current;
        userInput.add(new EntryInfo(input, (int)(currentTime - endTime)));
        endTime = currentTime;
        forWPM = endTime;
    }

    public int getCompletionTime(){
        return (int)(endTime - startTime);
    }
    public float getWPM(int numOfChar){
        if(WPM > -1){
            return WPM;
        }else {
            float duration = (float)(forWPM - startTime)/ 1000;
            WPM = ((float) (numOfChar - 1) / duration) / 5 * 60;
            return WPM;
        }
    }

    /*

        function msd(A, B) {
            for i = 0 to |A| // |A| = the length of A
                    D[i, 0] = i
            for j = 0 to |B|
                    D[0, j] = j
            for i = 1 to |A|
            for j = 1 to |B|
                    D[i, j] = min(right, down, diagonal)
            return D[|A|, |B|]
        }
        function r(a, b) {
            if a = b return 0
            otherwise return 1
        }
     */
    public int getMSD(String given, String entry){
        int gLen = given.length();
        int eLen = entry.length();
        int i, k;
        int[][] mat = new int[gLen+1][eLen+1];
        int right, down, diagonal;
        for(i = 0; i <= gLen; i++){
            mat[i][0] = i;
        }
        for(k = 0; k <= eLen; k++){
            mat[0][k] = k;
        }
        for(i = 1; i <= gLen ; i++){
            for(k = 1; k <= eLen; k++){
                right = mat[i-1][k] + 1;
                down = mat[i][k-1]+1;
                diagonal = mat[i-1][k-1] + diagonal(given.charAt(i-1), entry.charAt(k-1));
                int min = right < down ? right : down;
                min = min < diagonal ? min : diagonal;
                mat[i][k] = min;
            }
        }
        MSD = mat[gLen][eLen];
        return mat[gLen][eLen];
    }
    public int diagonal(char a, char b){
        if(a == b) return 0;
        else return 1;
    }

    public int getMSD(){
        return MSD;
    }

    public float errorRate_MSD(String given, String entry){
        int gLen = given.length();
        int eLen = entry.length();
        int maxLen = gLen > eLen ? gLen : eLen;
        return (float)getMSD(given, entry)/maxLen * 100;
    }

    //
    public void userInputAnalyse(int maxLength){
        userInputCount[INF] = getMSD();
        userInputCount[C] = maxLength - getMSD();
        userInputCount[IF] = backSpaceCount;

        CER = (float)(userInputCount[IF]) / (userInputCount[C] + userInputCount[IF]+ userInputCount[INF]) * 100;
        UER = (float)(userInputCount[INF]) / (userInputCount[C] + userInputCount[IF]+ userInputCount[INF]) * 100;
        TER = CER + UER;
    }

    public float getCER(){
        return CER;
    }
    public float getUER(){
        return UER;
    }
    public float getTER(){
        return TER;
    }



    public void setBlock(int b){
        block = b;
        logPrinter.setBlock(block);
    }
    public void setTrial(int b){
        trial = b;
        logPrinter.setTrial(trial);
    }

    public void startRecording(){

        isRecording = true;
    }
    public void recordResult(String userInputResult){
        logPrinter.printResult(goalString + "," + userInputResult + "," + WPM + "," + CER + "," + UER + "," + (forWPM - startTime));
    }

    public void recordSideTouchEvent(){
        logPrinter.printTouchInfo(watchTouchEvents);

    }

    public void recordUserInput(){
        logPrinter.printTextEntryInfo(userInput);
    }

    public void recordExample(String example){
        isRecording = true;
        goalString = example;
        logPrinter.setGoalString(example);
        //logPrinter.printExample();
    }

    public void addWatchTouchEvent(WatchTouchEvent_Ext e){
        watchTouchEvents.add(new WatchTouchEvent_Ext(e));
    }
    public void recordingDone(){
        isRecording = false;
        watchTouchEvents.clear();
        userInput.clear();
    }

    public ArrayList getWatchTouchEvents(){
        return watchTouchEvents;
    }
    public void fileClose(){
        logPrinter.fileClose();
    }
    public void fileOpen(){
        logPrinter.fileOpen();
    }
}
