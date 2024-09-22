package sku.jyj.example.silvia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBar;

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

public class RegisterActivity extends AppCompatActivity {

    private static final String urls = "http://lovelace0124.asuscomm.com:5003/userJoin";

    private Button btn_register;
    private EditText input_name, input_birth, input_phoneNo, input_guardianName, input_guardianPhoneNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        btn_register = findViewById(R.id.btn_register);
        input_name = findViewById(R.id.input_name);
        input_birth = findViewById(R.id.input_birth);
        input_phoneNo = findViewById(R.id.input_phoneNo);
        input_guardianName = findViewById(R.id.input_guardianName);
        input_guardianPhoneNo = findViewById(R.id.input_guardianPhoneNo);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input_name.getText().toString().isEmpty() || input_birth.getText().toString().isEmpty() ||
                    input_phoneNo.getText().toString().isEmpty() || input_guardianName.getText().toString().isEmpty() ||
                    input_guardianPhoneNo.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    sendServer(input_name.getText().toString(), input_birth.getText().toString(), input_phoneNo.getText().toString(),
                               input_guardianName.getText().toString(), input_guardianPhoneNo.getText().toString());
                }
            }
        });
    }

    public void sendServer(String name, String birth, String phoneNo, String guardianName, String guardianPhoneNo) {
        class sendData extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    JSONObject responseJson = new JSONObject(s);
                    boolean success = responseJson.getBoolean("success");
                    if (success) {
                        Toast.makeText(RegisterActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                        // 로그인 화면으로 이동
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "회원가입 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(RegisterActivity.this, "회원가입 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject jsonInput = new JSONObject();

                    jsonInput.put("memberName", name);
                    jsonInput.put("memberPhoneNo", phoneNo);
                    jsonInput.put("memberBirth", birth);
                    jsonInput.put("guardianName", guardianName);
                    jsonInput.put("guardianPhoneNo", guardianPhoneNo);

                    RequestBody reqBody = RequestBody.create(
                            jsonInput.toString(),
                            MediaType.parse("application/json; charset=utf-8")
                    );

                    Request request = new Request.Builder()
                            .post(reqBody)
                            .url(urls)
                            .build();

                    Response responses = client.newCall(request).execute();
                    return responses.body().string();

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        sendData sendData = new sendData();
        sendData.execute();
    }
}