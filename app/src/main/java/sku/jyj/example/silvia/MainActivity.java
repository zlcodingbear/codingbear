package sku.jyj.example.silvia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button btnLogout, btnVoiceSettings, btnVoiceConversation, btnSettings;
    private TextView usernameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogout = findViewById(R.id.btn_main_Logout);
        btnVoiceSettings = findViewById(R.id.btn_voice_settings);
        btnVoiceConversation = findViewById(R.id.btn_voice_conversation);
        btnSettings = findViewById(R.id.btn_main_settings);
        usernameTextView = findViewById(R.id.usernameTextView);

        btnLogout.setOnClickListener(v -> {
            btnLogout.setEnabled(false);  // 버튼 비활성화
            new LogoutTask().execute();
        });

        btnVoiceSettings.setOnClickListener(v -> {
            // 목소리 설정 버튼 클릭 시 VoiceChoiceActivity로 이동
            Intent intent = new Intent(MainActivity.this, VoiceChoiceActivity.class);
            startActivity(intent);
        });

        btnVoiceConversation.setOnClickListener(v -> {
            // 실비랑 음성대화 버튼 클릭 시 VoiceChatBotActivity로 이동
            Intent intent = new Intent(MainActivity.this, VoiceChatBotActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            // 설정 버튼 클릭 시 SettingsActivity로 이동
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        new FetchDataTask().execute();
    }

    @Override
    public void onBackPressed() {
        // 뒤로 가기 버튼을 누르면 로그아웃 처리
        new LogoutTask().execute();
    }

    private class FetchDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnLogout.setEnabled(false);  // 데이터 로드 중 로그아웃 버튼 비활성화
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

            SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
            String sessionId = sharedPreferences.getString("session_id", null);

            if (sessionId == null) {
                return "No session ID found";
            }

            JSONObject json = new JSONObject();
            try {
                json.put("session_id", sessionId);
            } catch (Exception e) {
                Log.e("FetchDataTask", "JSON exception: " + e.getMessage(), e);
                return "JSON exception";
            }

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url("http://lovelace0124.asuscomm.com:5003/data")
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    Log.e("FetchDataTask", "Request failed with status: " + response.code());
                    return "Request failed";
                }
            } catch (Exception e) {
                Log.e("FetchDataTask", "Exception occurred: " + e.getMessage(), e);
                return "Exception occurred";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            btnLogout.setEnabled(true);  // 데이터 로드 후 로그아웃 버튼 활성화
            if ("No session ID found".equals(result) || "Request failed".equals(result) || "Exception occurred".equals(result) || "JSON exception".equals(result)) {
                Toast.makeText(MainActivity.this, "데이터를 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                JSONObject jsonResponse = new JSONObject(result);
                boolean success = jsonResponse.getBoolean("success");

                if (success) {
                    JSONObject data = jsonResponse.getJSONObject("data");
                    String username = data.getString("username");

                    usernameTextView.setText("환영합니다, " + username + "님!");
                } else {
                    Toast.makeText(MainActivity.this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.e("FetchDataTask", "JSON parsing error: " + e.getMessage(), e);
                Toast.makeText(MainActivity.this, "데이터를 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class LogoutTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnLogout.setEnabled(false);  // 로그아웃 작업 중 로그아웃 버튼 비활성화
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

            SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
            String sessionId = sharedPreferences.getString("session_id", null);

            if (sessionId == null) {
                return "No session ID found";
            }

            JSONObject json = new JSONObject();
            try {
                json.put("session_id", sessionId);
            } catch (Exception e) {
                Log.e("LogoutTask", "JSON exception: " + e.getMessage(), e);
                return "JSON exception";
            }

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url("http://lovelace0124.asuscomm.com:5003/logout")
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    Log.e("LogoutTask", "Request failed with status: " + response.code());
                    return "Request failed";
                }
            } catch (Exception e) {
                Log.e("LogoutTask", "Exception occurred: " + e.getMessage(), e);
                return "Exception occurred";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            btnLogout.setEnabled(true);  // 로그아웃 작업 후 로그아웃 버튼 활성화
            if ("No session ID found".equals(result) || "Request failed".equals(result) || "Exception occurred".equals(result) || "JSON exception".equals(result)) {
                Toast.makeText(MainActivity.this, "로그아웃 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                JSONObject jsonResponse = new JSONObject(result);
                boolean success = jsonResponse.getBoolean("success");

                if (success) {
                    SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("session_id");
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.e("LogoutTask", "JSON parsing error: " + e.getMessage(), e);
                Toast.makeText(MainActivity.this, "로그아웃 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
