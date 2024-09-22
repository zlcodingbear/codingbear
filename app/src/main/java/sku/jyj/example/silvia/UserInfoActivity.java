package sku.jyj.example.silvia;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserInfoActivity extends AppCompatActivity {
    private TextView memberNameTextView;
    private TextView memberBirthTextView;
    private TextView memberPhoneTextView;
    private TextView guardianNameTextView;
    private TextView guardianPhoneTextView;

    private Button btn_userinfo_change;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        memberNameTextView = findViewById(R.id.memberNameTextView);
        memberBirthTextView = findViewById(R.id.memberBirthTextView);
        memberPhoneTextView = findViewById(R.id.memberPhoneTextView);
        guardianNameTextView = findViewById(R.id.guardianNameTextView);
        guardianPhoneTextView = findViewById(R.id.guardianPhoneTextView);
        btn_userinfo_change = findViewById(R.id.btn_userinfo_change);

        // AsyncTask 실행
        new RetrieveUserInfoTask().execute();

        //회원정보 수정으로 이동
        btn_userinfo_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserInfoActivity.this, UserInfoChangeActivity.class);
                startActivity(intent);
            }
        });
    }

    private class RetrieveUserInfoTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                // 플라스크 서버 URL 설정
                URL url = new URL("http://lovelace0124.asuscomm.com:5003/userShowData");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    // JSON 형식으로 받은 회원 정보를 파싱하여 TextView에 표시
                    JSONObject jsonObject = new JSONObject(result);
                    memberNameTextView.setText(jsonObject.getString("member_name"));
                    memberBirthTextView.setText(jsonObject.getString("member_birth"));
                    memberPhoneTextView.setText(jsonObject.getString("member_phone_no"));
                    guardianNameTextView.setText(jsonObject.getString("guardian_name"));
                    guardianPhoneTextView.setText(jsonObject.getString("guardian_phone_no"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // 회원 정보를 받아오지 못한 경우 처리
                memberNameTextView.setText("Failed to retrieve user info.");
            }
        }
    }
}
