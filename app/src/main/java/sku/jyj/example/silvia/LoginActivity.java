package sku.jyj.example.silvia;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private static final String LOGIN_URL = "http://lovelace0124.asuscomm.com:5003/login";
    private EditText inputLoginName, inputLoginBirth, inputLoginPhoneNo;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputLoginName = findViewById(R.id.input_loginName);
        inputLoginBirth = findViewById(R.id.input_loginBirth);
        inputLoginPhoneNo = findViewById(R.id.input_loginPhoneNo);
        btnLogin = findViewById(R.id.btn_loginToTTS);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (validateInputs()) {
                    btnLogin.setEnabled(false);  // 버튼 비활성화
                    new LoginTask().execute();
                }*/
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean validateInputs() {
        if (inputLoginName.getText().toString().trim().isEmpty() ||
            inputLoginBirth.getText().toString().trim().isEmpty() ||
            inputLoginPhoneNo.getText().toString().trim().isEmpty()) {
            Toast.makeText(LoginActivity.this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private class LoginTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnLogin.setEnabled(false);  // 버튼 비활성화
        }

        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            MediaType JSON = MediaType.get("application/json; charset=utf-8");

            JSONObject json = new JSONObject();
            try {
                json.put("loginName", inputLoginName.getText().toString());
                json.put("loginBirth", inputLoginBirth.getText().toString());
                json.put("loginPhoneNo", inputLoginPhoneNo.getText().toString());
            } catch (JSONException e) {
                Log.e("LoginActivity", "JSON exception: " + e.getMessage(), e);
                return null;
            }

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(LOGIN_URL)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    Log.e("LoginActivity", "Request failed with status: " + response.code());
                    return null;
                }
            } catch (IOException e) {
                Log.e("LoginActivity", "IOException: " + e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            btnLogin.setEnabled(true);  // 버튼 활성화

            if (result == null) {
                Toast.makeText(LoginActivity.this, "로그인 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d("LoginActivity", "서버 응답: " + result);  // 서버 응답을 로그로 출력

            try {
                JSONObject jsonResponse = new JSONObject(result);
                boolean success = jsonResponse.getBoolean("success");

                if (success) {
                    String sessionId = jsonResponse.getString("session_id");

                    // 세션 ID를 SharedPreferences에 저장
                    SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("session_id", sessionId);
                    editor.apply();

                    // 세션 ID 로그 출력
                    Log.d("LoginActivity", "저장된 세션 ID: " + sessionId);

                    // 로그인 성공 시 MainActivity로 이동
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = jsonResponse.getString("message");
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e("LoginActivity", "JSON parsing error: " + e.getMessage(), e);
                Toast.makeText(LoginActivity.this, "로그인 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
