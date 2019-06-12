package util.PhraseSets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import util.StdRandom;

/**
 * Created by PCPC on 2016-08-08.
 */
public class ExamplePhrases {
    ArrayList<String> phrases;
    public ExamplePhrases(){
        phrases = new ArrayList<String>();


//        File f = new File("res/phrase.txt");
//        readFile(f);
    }
    public void readTxt(InputStream is){
        int i;
        String str;
        try{
            BufferedReader bf  = new BufferedReader(new InputStreamReader(is));
            while(true){
                str = bf.readLine();
                if (str != null) {
                    phrases.add(str.toLowerCase());
                }else{
                    break;
                }
            }
        }catch (IOException e){

        }
    }

    public String getRandomPhrase(){

        StdRandom radom;
        int random = (int)(StdRandom.random() * phrases.size());
        return phrases.get(random);
    }
    public String getPhrase(int i){
        if(i > phrases.size()){
            return "(INVALID INPUT)";
        }else return phrases.get(i);
    }




}
