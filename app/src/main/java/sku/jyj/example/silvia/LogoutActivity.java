package sku.jyj.example.silvia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class LogoutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 로그아웃 작업을 시작합니다.
        new LogoutTask(this).execute();
    }

    private static class LogoutTask extends AsyncTask<Void, Void, String> {
        private Context mContext;

        public LogoutTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 여기에 로딩 표시 등을 추가할 수 있습니다.
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

            // SharedPreferences에서 세션 ID 가져오기
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("user_session", Context.MODE_PRIVATE);
            String sessionId = sharedPreferences.getString("session_id", null);

            if (sessionId == null) {
                return "No session ID found";
            }

            JSONObject json = new JSONObject();
            try {
                json.put("session_id", sessionId);
            } catch (Exception e) {
                return "JSON exception: " + e.getMessage();
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
                    return "Request failed with status: " + response.code();
                }
            } catch (Exception e) {
                return "Exception occurred: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == null) {
                Toast.makeText(mContext, "로그아웃 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (result.equals("No session ID found")) {
                Toast.makeText(mContext, "세션 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (result.startsWith("Request failed") || result.startsWith("Exception occurred")) {
                Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonResponse = new JSONObject(result);
                boolean success = jsonResponse.getBoolean("success");

                if (success) {
                    // 로그아웃 성공 처리
                    Toast.makeText(mContext, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                    // 로그인 상태 초기화
                    SharedPreferences preferences = mContext.getSharedPreferences("user_session", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();

                    // 로그인 액티비티로 이동하도록 설정
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).finish();
                    }
                } else {
                    // 로그아웃 실패 처리
                    Toast.makeText(mContext, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(mContext, "로그아웃 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
