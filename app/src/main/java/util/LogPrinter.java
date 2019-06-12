package util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import Event.WatchTouchEvent_Ext;

/**
 * Created by PCPC on 2016-08-28.
 * To record text entry results
 */
public class LogPrinter {
    private String filePath = "/expHoldLog";
    private String fileName1 = "/Mode_hold_exp1_result_";
    private String fileName2 = "/Mode_hold_exp1_keyInput_";
    private String fileName3 = "/Mode_hold_exp1_rawData_";

    private String goalString;
    private int block, trial;

    public FileOutputStream fos1;
    public FileOutputStream fos2;
    public FileOutputStream fos3;

    private File f1,f2,f3;


    public void printTouchInfo(ArrayList<WatchTouchEvent_Ext> touchHistory){
        int size = touchHistory.size();
        int nth = 0;
        long startTime = touchHistory.get(0).eventTime;
        for(int i = 0; i < size; i++){
            print3(""+block + ","+ trial+","+ (touchHistory.get(i).eventTime - startTime) +","+touchHistory.get(i).ePos+","+touchHistory.get(i).state_screen +","+touchHistory.get(i).xPos + ","+touchHistory.get(i).yPos + ","+touchHistory.get(i).gesture + ","+touchHistory.get(i).selected+"\n");
            //if(touchHistory.get(i).metaData > 0) nth++;
            Log.d("touch info", touchHistory.get(i).toString());
        }
    }
    public void printTextEntryInfo(ArrayList<EntryInfo> entryHistory){
        for(int i = 0; i < entryHistory.size(); i++){
            print2(""+block + ","+ trial+","+ goalString+"," +i+","+entryHistory.get(i).toString() +"\n");
        }
    }

    public void printStart(){
        fileOpen();
        print1("block,trial,Example,result,WPM,CER,UER,completionTime\n");
        print2("block,trial,Example,nth,input,inputTime\n");
        print3("block,trial,eventTime,edgePos,screenState,posX,posY,gesture,inputed\n");
    }

    public void printResult(String result){
        print1(""+block + ","+ trial+","+ result +"\n");
    }

    public void printExample(){
        print1(""+block + ","+ trial+","+ goalString+",,,,,\n");
        print2(""+block + ","+ trial+","+ goalString+",,\n");
    }
    public void print1(String str) {
        if (fos1 != null) {
            try {
                fos1.write(str.getBytes());
            } catch (IOException e) {
                Log.e("Printing Error", str);
                e.printStackTrace();
            }
        }
    }
    public void print2(String str) {
        if (fos2 != null) {
            try {
                fos2.write(str.getBytes());
            } catch (IOException e) {
                Log.e("Printing Error", str);
                e.printStackTrace();
            }
        }
    }
    public void print3(String str) {
        if (fos3 != null) {
            try {
                fos3.write(str.getBytes());
            } catch (IOException e) {
                Log.e("Printing Error", str);
                e.printStackTrace();
            }
        }
    }


    public void setGoalString(String goal){
        goalString = goal;
    }

    public void fileOpen(){
        Calendar today = Calendar.getInstance(Locale.KOREA);
        String sdcard = Environment.getExternalStorageState();

        if (!sdcard.equals(Environment.MEDIA_MOUNTED)) {
            // SD카드 UNMOUNTED
            filePath = "" + Environment.getRootDirectory().getAbsolutePath()
                    + filePath; // 내부저장소의 주소를 얻어옴


        } else {
            // SD카드 MOUNT
            filePath = ""
                    + Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + filePath; // 외부저장소의
            // 주소를
            // 얻어옴
        }

        File rootCheck = new File(filePath);
        if (!rootCheck.exists()) { // 최상위 루트폴더 미 존재시
            rootCheck.mkdir();
            rootCheck = new File(filePath);
            if (!rootCheck.exists()) { // 하위 메모저장폴더 미 존재시
                rootCheck.mkdir();
            }
        }

        int num = 1;
        f1 = new File(rootCheck + fileName1 + num + ".csv");
        while(isFile(f1)){
            num++;
            f1 = new File(rootCheck + fileName1 + num + ".csv");
        }
        f2 = new File(rootCheck + fileName2 + num + ".csv");
        f3 = new File(rootCheck + fileName3 + num + ".csv");
        try {
            fos1 = new FileOutputStream(f1, true);
            fos2 = new FileOutputStream(f2, true);
            fos3 = new FileOutputStream(f3, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }


    private boolean isFile(File file){
        boolean result;
        if(file!=null&&file.exists()&&file.isFile()){
            result=true;
        }else{
            result=false;
        }
        return result;

    }


    public void fileClose() {
        try {
            fos1.close();
            fos2.close();
            fos3.close();
            fos3 = null;
            fos2 = null;
            fos1 = null;
            f1 = null;
            f2 = null;
            f3 = null;


        }catch(IOException e){
            Log.e("Exception", e.getMessage());
        }

    }

    public void setBlock(int b){
        block = b;
    }
    public void setTrial(int t)
    {
        trial = t;
    }
}
