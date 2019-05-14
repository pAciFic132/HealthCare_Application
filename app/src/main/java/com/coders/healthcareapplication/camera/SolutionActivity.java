package com.coders.healthcareapplication.camera;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;


import com.coders.healthcareapplication.newtork_task.BodyRgb_Download;
import com.coders.healthcareapplication.newtork_task.Exerciseinfo_call;
import com.coders.healthcareapplication.view.ContentListActivity;
import com.coders.healthcareapplication.view.PopupContentActivity;
import com.coders.healthcareapplication.R;
import com.coders.healthcareapplication.adapter.ContentListAdapter;
import com.coders.healthcareapplication.newtork_task.RequestHttpURLConnection;
import com.coders.healthcareapplication.view.AdminMainActivity;
import com.coders.healthcareapplication.view.PopupSolutionActivity;
import com.orbbec.astra.*;
import com.orbbec.astra.android.AstraAndroidContext;

import org.w3c.dom.Text;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import java.util.ArrayList;

public class SolutionActivity extends AppCompatActivity {

    private TextView exercise_info;
    private TextView exericse_title;

    private static final String TAG="ContentListActivity";
    private ArrayList<String> contents=new ArrayList<>();
    static ArrayAdapter exercise_adapter;
    public ArrayList<String> exercise_info_list=new ArrayList<>();
    public ListView listView;
    private TextView list_item;

    private Executor ex;
    private ImageView camView;
    private TextView feedbackView;
    private ImageView videoView;

    private RGBData rgbData;
    private ExerFileController efc;
    private BodyData bodyData;

    private boolean thread_stop;
    private boolean isDataLoading;

    private AstraAndroidContext aac;

    static {
        System.loadLibrary("native-lib");
    }
    public native String stringFromJNI();


    private String path= Environment.getExternalStorageDirectory().getPath();
    private String category;
    private String exercise_name;
    private String exercise_desc;
    private String image_title;
    private String video_title;
    private String body_title;
    private String rgb_title;

    private Button back;
    private Button btn_convert_to_admin_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        Intent intent=getIntent();
        category=intent.getStringExtra("category");
        exercise_name=intent.getStringExtra("exercisename");
        exercise_desc=intent.getStringExtra("exercise_desc");
        image_title=intent.getStringExtra("image_title");
        video_title=intent.getStringExtra("video_title");
        body_title=intent.getStringExtra("body_title");
        rgb_title=intent.getStringExtra("rgb_title");

        camView = (ImageView) findViewById(R.id.user_cam);
        feedbackView = (TextView) findViewById(R.id.user_feedback);
        videoView = (ImageView) findViewById(R.id.user_video);

        rgbData = new RGBData();
        bodyData = new BodyData();
        efc = new ExerFileController();

        exericse_title = (TextView) findViewById(R.id.view_exercise_title_solution);
        exericse_title.setText(exercise_name);


        //exercise_info_list=new ArrayList<>();
        /*list view dynamic creating notification is in ContentListApdapter*/
        listView=findViewById(R.id.exercise_list_sol);
        //exercise_adapter=new ArrayAdapter(this,R.layout.item_list_solution,ContentListAdapter.exercises);
        //android.R.layout.simple_list_item_1
        exercise_adapter=new ArrayAdapter(this,R.layout.item_list_solution, ContentListAdapter.exercises){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                /// Get the Item from ListView
                View view = super.getView(position, convertView, parent);
                //TextView tv = (TextView) view.findViewById(android.R.id.text1);
                TextView tv = (TextView) view.findViewById(R.id.btn_list_solu);
                // Set the text size 25 dip for ListView each item
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,17);
                // Return the view
                return view;
            }
        };

        listView.setAdapter(exercise_adapter);

        /*list item click event handler*/
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = "http://"+getString(R.string.ip)+":"+getString(R.string.port)+"/HealthCare/exerciseinfo";
                String value=ContentListAdapter.exercises.get(position);
                ContentValues cvalue=new ContentValues();
                cvalue.put("exercise_name",value);
                Log.i(value,"value check");
                Exerciseinfo_call networkTask2 = new Exerciseinfo_call(url, SolutionActivity.this,cvalue);
                networkTask2.execute();
                //list_item.invalidate();
            }
        });

        /*check button instance*/
        back=(Button)findViewById(R.id.btn_back_solution);
        /*make event listenr*/
        back.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v){
                        onBackPressed();
                    }
                }
        );

        /*check button instance*/
        btn_convert_to_admin_list=(Button)findViewById(R.id.btn_convert_admin_solution);
        /*make event listenr*/
        btn_convert_to_admin_list.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v){
                        Log.i("Act.btn_convert2Ad_List","onClick");
                        /*인텐트 생성 후 명시적 다음 액티비티 호출*/
                        Intent intentToAdminMain=new Intent(SolutionActivity.this, AdminMainActivity.class);
                        startActivity(intentToAdminMain);
                    }
                }
        );

//        String url_body_data="http://"+getString(R.string.ip)+":"+getString(R.string.port)+"/HealthCare/txt/"+body_title;
//        Log.i("url",url_body_data);
//        BodyRgb_Download networkTask_body=new BodyRgb_Download(url_body_data,"bodyData.txt",path);
//        networkTask_body.execute();
//
//        String url_rgb_data="http://"+getString(R.string.ip)+":"+getString(R.string.port)+"/HealthCare/txt/"+rgb_title;
//        Log.i("url",url_rgb_data);
//        BodyRgb_Download networkTask_rgb=new BodyRgb_Download(url_body_data,"rgbData.txt",path);
//        networkTask_rgb.execute();

    }

    public void putInfo(){
        Intent intentToPopup=new Intent(getApplicationContext(), PopupSolutionActivity.class);
        intentToPopup.putExtra("category",exercise_info_list.get(0));
        intentToPopup.putExtra("exercisename",exercise_info_list.get(1));
        intentToPopup.putExtra("exercise_desc",exercise_info_list.get(2));
        intentToPopup.putExtra("image_title",exercise_info_list.get(3));
        intentToPopup.putExtra("video_title",exercise_info_list.get(4));
        intentToPopup.putExtra("body_title",exercise_info_list.get(5));
        intentToPopup.putExtra("rgb_title",exercise_info_list.get(6));
        startActivity(intentToPopup);
    }

    @Override
    protected void onResume(){
        super.onResume();

        isDataLoading = false;

        feedbackStr = "카메라 셋팅 중";
        textHandler.sendMessage(textHandler.obtainMessage());

//        aac = new AstraAndroidContext(getApplicationContext());
//        aac.initialize();
//        aac.openAllDevices();
//
//        thread_stop = false;
//
//        // Executor class
//        ex = new Executor(){
//            @Override
//            public void execute(@NonNull Runnable r) {
//                new Thread (r).start();
//            }
//        };
//        // Execute the Runnable object
//        ex.execute(new SolutionActivity.UpdateRunnable());
    }

    @Override
    protected void onPause(){
        super.onPause();

        thread_stop = true;
    }

    /* 피드백 텍스트뷰 핸들러 */
    private String feedbackStr = "";
    final Handler textHandler = new Handler(){
        public void handleMessage(Message msg){
            feedbackView.setText(feedbackStr);
        }
    };

    /* 캠 이미지뷰 핸들러 */
    private Bitmap camBitmap;
    final Handler camHandler = new Handler(){
        public void handleMessage(Message msg){
            camView.setImageBitmap(camBitmap);
            Drawable drawable  = null;
        }
    };

    /* 영상 이미지뷰 핸들러 */
    private Bitmap videoBitmap;
    final Handler videoHandler = new Handler(){
        public void handleMessage(Message msg){
            videoView.setImageBitmap(videoBitmap);
        }
    };

    /* 실제 구동 소스 코드 */
    private class UpdateRunnable implements Runnable {
        int cnt = 0;
        int nowScore = 0;
        int isBodyTracking = 0;
        int final_score = 0;

        @Override
        public void run() {
            try {
                TimeUnit.MILLISECONDS.sleep(3000);

                efc.openLoggingFile();

                if(isDataLoading == false){
                    feedbackStr = "데이터 로딩 중";
                    textHandler.sendMessage(textHandler.obtainMessage());

                    efc.openRGBFile_R();
                    rgbData.readRGBData(efc.getRgbin(), efc.getOut());
                    efc.closeRGBFile_R();

                    isDataLoading = true;
                }

                feedbackStr = "사용자의 신체를 탐색 중 (신체 탐색이 완료되고 1초 뒤부터 자세 교정 시작)";
                textHandler.sendMessage(textHandler.obtainMessage());

                efc.openBodyFile_R();

                StreamSet streamSet = StreamSet.open();
                StreamReader reader = streamSet.createReader();

                ColorStream colorStream = ColorStream.get(reader);
                Iterator ismi = colorStream.getAvailableModes().iterator();
                while(ismi.hasNext()){
                    ImageStreamMode ism = (ImageStreamMode)ismi.next();
                    ismi.remove();
                    colorStream.setMode(ism);
                    if(colorStream.getMode().getFormat().getCode()==PixelFormat.YUVY.getCode() && colorStream.getMode().getHeight() == 480 && colorStream.getMode().getWidth() == 640){
                        break;
                    }
                }
                colorStream.start();

                BodyStream bodyStream = BodyStream.get(reader);
                bodyStream.start();

                reader.addFrameListener(new StreamReader.FrameListener() {
                    public void onFrameReady(StreamReader reader, ReaderFrame frame) {
                        BodyFrame bodyFrame = BodyFrame.get(frame);

                        if(bodyFrame.getBodies().toString()!="[]"){
                            if(isBodyTracking == 0)  isBodyTracking = 1;
                            Body body = bodyFrame.getBodies().iterator().next();
                            nowScore = bodyData.bodyDataCompare(efc.getBodyReader(), body, cnt);
                        }

                        ColorFrame colorFrame = ColorFrame.get(frame);
                        camBitmap = rgbData.rgbToArgb(colorFrame.getByteBuffer(), colorFrame.getWidth(), colorFrame.getHeight(), efc.getOut(), 0, cnt);
                        camHandler.sendMessage(camHandler.obtainMessage());
                    }
                });

                while (!thread_stop) {
                    Astra.update();
                    TimeUnit.MILLISECONDS.sleep(1);

                    if(isBodyTracking == 2) {
                        cnt = cnt + 1;

                        if(cnt < rgbData.getNUMBEROFFRAME()){
                            videoBitmap = rgbData.getVideoBitmap(cnt);
                            videoHandler.sendMessage(videoHandler.obtainMessage());
                        }

                        final_score += nowScore;
                        feedbackStr = Integer.toString(cnt) + " " + Integer.toString(bodyData.getNowFileCnt()) + " : " + Integer.toString(nowScore);
                        textHandler.sendMessage(textHandler.obtainMessage());
                        if (cnt == rgbData.getNUMBEROFFRAME()) {
                            break;
                        }
                    }
                    else if(isBodyTracking == 1){
                        feedbackStr = "신체 탐색 완료. 1초 뒤 시작";
                        textHandler.sendMessage(textHandler.obtainMessage());
                        TimeUnit.MILLISECONDS.sleep(1000);
                        isBodyTracking = 2;
                    }
                }

                final_score = final_score / cnt + 5;
                if(final_score > 100)
                    final_score = 100;
                feedbackStr = "Score : " + Integer.toString(final_score);
                textHandler.sendMessage(textHandler.obtainMessage());

                bodyStream.stop();
                colorStream.stop();
                streamSet.close();
            } catch (Throwable e) {
                efc.getOut().println(e);
            } finally {
                aac.terminate();
                try {
                    efc.closeLoggingFile();
                } catch (Throwable e){ }
            }
        }
    }

}