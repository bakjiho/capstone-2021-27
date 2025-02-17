package com.example.datadamoa;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkActivity extends AppCompatActivity {

    TextView tvTitle;
    TextView tvContent, tvprice, tvquantity, tvtotalprice;
    ImageView ivSample1, ivSample2, ivSample3, ivSample4, ivSample5;
    Context context;
    qaListView qalv;
    ArrayList<qaWork>  arrqa = new ArrayList<>();
    qaViewAdapter qaAdapter;
    EditText etQuestion;
    Button btQuestionSubmit;

    int board_idx = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        tvTitle = (TextView)findViewById(R.id.boardtitle);
        tvContent = (TextView)findViewById(R.id.boardcontent);
        tvprice = (TextView)findViewById(R.id.boardprice);
        tvquantity = (TextView)findViewById(R.id.boardquantity);

        context = WorkActivity.this;

        ivSample1 = (ImageView)findViewById(R.id.work_sample_1);
        ivSample2 = (ImageView)findViewById(R.id.work_sample_2);
        ivSample3 = (ImageView)findViewById(R.id.work_sample_3);
        ivSample4 = (ImageView)findViewById(R.id.work_sample_4);
        ivSample5 = (ImageView)findViewById(R.id.work_sample_5);

        etQuestion = (EditText)findViewById(R.id.work_question);
        btQuestionSubmit = (Button)findViewById(R.id.work_question_submit);

        qaAdapter = new qaViewAdapter(arrqa);
        qalv = (qaListView)findViewById(R.id.qa_listview);

        qalv.setAdapter(qaAdapter);

        btQuestionSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            String serverUri = "http://capstone.louissoft.kr:3000/view_board/?board_idx=" + board_idx;
                            URL url = new URL(serverUri);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setDoInput(true);
                            connection.setDoOutput(true);
                            SharedPreferences sf = getSharedPreferences("cookie",MODE_PRIVATE);
                            connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                            connection.setRequestProperty("Cookie", sf.getString("cookie", ""));

                            StringBuffer buffer = new StringBuffer();
                            buffer.append("qa_text").append("=").append(etQuestion.getText().toString());

                            OutputStreamWriter outStream = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                            PrintWriter writer = new PrintWriter(outStream);
                            writer.write(buffer.toString());
                            writer.flush();
                            InputStream is = connection.getInputStream();
                            StringBuilder sb = new StringBuilder();
                            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                            String result;
                            while ((result = br.readLine()) != null) {
                                sb.append(result + "\n");
                            }
                            result = sb.toString();
                            if(result.equals("error")) {
                                // Error 발생
                                WorkActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        AlertDialog alertDialog = new AlertDialog.Builder(WorkActivity.this).create();
                                        alertDialog.setTitle("질문 등록 실패");
                                        alertDialog.setMessage("질문 등록에 실패하였습니다. 관리자에게 문의하여 주십시오.");
                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "확인",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        alertDialog.show();
                                    }
                                });
                            }
                            else {
                                WorkActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        etQuestion.setText("");
                                        Toast.makeText(getApplicationContext(), "질문이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                                        getQA(board_idx + "");
                                    }
                                });
                            }
                        }
                        catch(Exception ex) {

                        }
                    }
                }.start();
            }
        });

        Intent intent = this.getIntent();
        board_idx = intent.getIntExtra("board_idx", 1);
        getBoard(board_idx + "");
        getQA(board_idx + "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_upload_photo:
                Log.d("WorkActivity", "Upload photo click");
                Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
                intent.putExtra("board_idx", board_idx);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    void getBoard(String _boardidx) {
        new Thread() {
            @Override
            public void run() {
                String serverUri = "http://capstone.louissoft.kr:3000/view_board/api/detail/" + _boardidx;
                try {
                    Log.d("getBoard", "Start1");
                    URL url = new URL(serverUri);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
//                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    Log.d("getBoard", "start2");

                    if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                        BufferedReader br = new BufferedReader(isr);
                        final StringBuffer buffer = new StringBuffer();
                        String line = br.readLine();
                        while (true){
                            if(line== null){
                                break;
                            }
                            buffer.append(line);
                            line = br.readLine();
                        }
                        Log.d("result", buffer.toString());

                        try {
                            JSONArray ja = new JSONArray(buffer.toString());

                            int board_idx = Integer.parseInt(ja.getJSONObject(0).getString("idx"));
                            String title = ja.getJSONObject(0).getString("title");
                            int price = Integer.parseInt(ja.getJSONObject(0).getString("price"));
                            String content = ja.getJSONObject(0).getString("content");
                            int quantity = Integer.parseInt(ja.getJSONObject(0).getString("quantity"));
                   //         int date = Integer.parseInt(ja.getJSONObject(0).getString("date"));
                            int total_price = Integer.parseInt(ja.getJSONObject(0).getString("total_price"));
                            String exampleFiles = ja.getJSONObject(0).getString("example_file_idx");
                            String[] exampleFile = exampleFiles.split(";");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 이 곳에 UI작업을 한다
                                    tvquantity.setText("[ "+ quantity +" 개 ]");
                                    tvTitle.setText(title);
                                    tvContent.setText(content);
                                    tvprice.setText(price+"  ");

                                    Glide.with(context).load("http://capstone.louissoft.kr:3000/file/" + exampleFile[0]).into(ivSample1);
                                    Glide.with(context).load("http://capstone.louissoft.kr:3000/file/" + exampleFile[1]).into(ivSample2);
                                    Glide.with(context).load("http://capstone.louissoft.kr:3000/file/" + exampleFile[2]).into(ivSample3);
                                    Glide.with(context).load("http://capstone.louissoft.kr:3000/file/" + exampleFile[3]).into(ivSample4);
                                    Glide.with(context).load("http://capstone.louissoft.kr:3000/file/" + exampleFile[4]).into(ivSample5);

//                                    Glide.with(context).load("https://upload.wikimedia.org/wikipedia/commons/0/0e/Tree_example_VIS.jpg").into(ivSample1);


                                }
                            });



                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
//
                    }

                }
                catch (MalformedURLException e) {
                    //
                    Log.d("getBoadListError", e.toString());
                } catch (IOException e) {
                    //
                    Log.d("getBoadListError", e.toString());
                }
            }
        }.start();
    }
    void getQA(String _boardidx) {
        new Thread() {
            @Override
            public void run() {
                String serverUri = "http://capstone.louissoft.kr:3000/view_board/api/" + _boardidx;
                try {
                    Log.d("getBoardQA", "Start");
                    URL url = new URL(serverUri);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
//                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    Log.d("getQA", "start2");

                    if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                        BufferedReader br = new BufferedReader(isr);
                        final StringBuffer buffer = new StringBuffer();
                        String line = br.readLine();
                        while (true){
                            if(line== null){
                                break;
                            }
                            buffer.append(line);
                            line = br.readLine();
                        }
                        Log.d("result", buffer.toString());

                        try {
                            JSONArray ja = new JSONArray(buffer.toString());
                            arrqa.clear();
                            for(int i = 0; i < ja.length(); i++) {
                                qaWork w = new qaWork();
                                 w.boardidx = Integer.parseInt(ja.getJSONObject(i).getString("idx"));
                                 w.question = ja.getJSONObject(i).getString("question");
                                 w.answer = ja.getJSONObject(i).getString("answer");
                                 w.questNick = ja.getJSONObject(i).getString("questioner_nickname");
                                 arrqa.add(w);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 이 곳에 UI작업을 한다
                                    qaAdapter.refreshData(arrqa);
                                    qaAdapter.notifyDataSetChanged();
                                }
                            });



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }
                catch (MalformedURLException e) {
                    //
                    Log.d("getBoadListError", e.toString());
                } catch (IOException e) {
                    //
                    Log.d("getBoadListError", e.toString());
                }
            }
        }.start();
    }

}