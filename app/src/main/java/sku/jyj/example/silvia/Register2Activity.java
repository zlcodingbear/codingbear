package sku.jyj.example.silvia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Register2Activity extends AppCompatActivity {

    // Flask 서버 호출
    private static final String urls = "http://lovelace0124.asuscomm.com:5003/userJoin";

    private Button btn_next2;
    private EditText input_guardianName, input_guardianPhoneNo;
    private String name, birth, phoneNo;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        Toolbar toolbar5 = findViewById(R.id.toolbar5);
        setSupportActionBar(toolbar5);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_next2 = findViewById(R.id.btn_next2);
        input_guardianName = findViewById(R.id.input_guardianName);
        input_guardianPhoneNo = findViewById(R.id.input_guardianPhoneNo);

        // RegisterActivity에서 전달된 데이터를 받습니다.
        Intent intent = getIntent();
        if (intent != null) {
            name = intent.getStringExtra("name");
            birth = intent.getStringExtra("birth");
            phoneNo = intent.getStringExtra("phoneNo");
        }

        btn_next2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //서버로 회원가입정보 전송
                if (input_guardianName.getText().toString().isEmpty() || input_guardianPhoneNo.getText().toString().isEmpty()) {
                    Toast.makeText(Register2Activity.this, "Please enter guardian information", Toast.LENGTH_SHORT).show();
                } else {
                    sendServer(name, birth, phoneNo);
                }
            }
        });
    }

    public void sendServer(String name, String birth, String phoneNo){ // 서버로 데이터 전송하기 위한 함수
        class sendData extends AsyncTask<Void, Void, String> { // 백그라운드 ��� 생성

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // 백그라운드 작업 전에 실행되는 부분
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s != null && s.equals("success")) {
                    Toast.makeText(Register2Activity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                } else {
                    // 에러 처리
                    Toast.makeText(Register2Activity.this, "회원가입 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                // 백그라운드 작업 중 갱신이 필요한 경우 실행되는 부분
            }

            @Override
            protected void onCancelled(String s) {
                super.onCancelled(s);
                // 백그라운드 작업이 취소된 경우 실행되는 부분
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                // 백그라운 작이 된 경우 실행되는 부분
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    OkHttpClient client = new OkHttpClient(); // OkHttp를 사용하도록 OkHttpClient 를 생성

                    JSONObject jsonInput = new JSONObject();  // JSON 객체 생성

                    jsonInput.put("memberName", name);
                    jsonInput.put("memberPhoneNo", phoneNo);
                    jsonInput.put("memberBirth", birth);
                    jsonInput.put("guardianName", input_guardianName.getText().toString());
                    jsonInput.put("guardianPhoneNo", input_guardianPhoneNo.getText().toString());

                    RequestBody reqBody = RequestBody.create(
                            jsonInput.toString(),
                            MediaType.parse("application/json; charset=utf-8")
                    );

                    Request request = new Request.Builder()
                            .post(reqBody)
                            .url(urls)
                            .build();

                    Response responses = client.newCall(request).execute(); // 요청을 실행 (동기 처리 : execute(), 비동기 처리 : enqueue())
                    String responseString = responses.body().string();
                    Log.d("ServerResponse", responseString); // 서버 응답 로그 추가
                    if (responseString.equals("success")) {
                        return "success";
                    } else {
                        return null;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        // AsyncTask 실행
        sendData sendData = new sendData();
        sendData.execute(); // 웹 서버에 데이터 전송
    }

}
