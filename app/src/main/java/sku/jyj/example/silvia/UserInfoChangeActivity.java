package sku.jyj.example.silvia;

import androidx.appcompat.app.AppCompatActivity;
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

public class UserInfoChangeActivity extends AppCompatActivity {

    private static final String urls = "http://lovelace0124.asuscomm.com:5003/userInfoChange";
    private String primaryKey; // 추가: 수정할 회원의 Primary Key
    private EditText input_changeName, input_changeBirth, input_changeGuardianName, input_changeGuardianPhoneNo, input_changePhoneNo;
    private Button changeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfochange);

        input_changePhoneNo = findViewById(R.id.input_changePhoneNo);
        input_changeName = findViewById(R.id.input_changeName);
        input_changeBirth = findViewById(R.id.input_changeBirth);
        input_changeGuardianName = findViewById(R.id.input_changeGuardianName);
        input_changeGuardianPhoneNo = findViewById(R.id.input_changeGuardianPhoneNo);
        changeButton = findViewById(R.id.changebutton);

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 수정된 정보를 서버로 전송
                Log.d("changebutton", "Button clicked");
                sendServer();

                //엑티비티 종료
                finish();
            }
        });
    }

        public void sendServer() { // 서버로 데이터 전송하기 위한 함수
            class sendData extends AsyncTask<Void, Void, String> { // 백그라운드 쓰레드 생성

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    // 백그라운드 작업 전에 실행되는 부분
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);
                    // 백그라운드 작업이 끝난 후에 실행되는 부분
                    if (result.equals("success")) {
                        // 회원 정보가 수정되었다는 토스트 메시지 출력
                        Toast.makeText(UserInfoChangeActivity.this, "회원 정보가 수정되었습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        // 오류 메시지 출력
                        Toast.makeText(UserInfoChangeActivity.this, "오류가 발생했습니다", Toast.LENGTH_SHORT).show();
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
                    // 백그라운드 작업이 취소된 경우 실행되는 부분
                }

                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        OkHttpClient client = new OkHttpClient(); // OkHttp를 사용하도록 OkHttpClient 객체를 생성

                        JSONObject jsonInput = new JSONObject();  // JSON 객체 생성

                        jsonInput.put("changePhoneNo", input_changePhoneNo.getText().toString());
                        jsonInput.put("changeName", input_changeName.getText().toString());
                        jsonInput.put("changeBirth", input_changeBirth.getText().toString());
                        jsonInput.put("changeGuardianName", input_changeGuardianName.getText().toString());
                        jsonInput.put("changeGuardianPhoneNo", input_changeGuardianPhoneNo.getText().toString());

                        RequestBody reqBody = RequestBody.create(
                                jsonInput.toString(),
                                MediaType.parse("application/json; charset=utf-8")
                        );

                        Request request = new Request.Builder()
                                .post(reqBody)
                                .url(urls)
                                .build();

                        Response responses = client.newCall(request).execute(); // 요청을 실행 (동기 처리 : execute(), 비동기 처리 : enqueue())
                        if (responses.isSuccessful()) {
                            // 요청 성공 시
                            return "success";
                        } else {
                            // 요청 실패 시
                            return "failure";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return "failure";
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "failure";
                    }
                }
            }

            // AsyncTask 실행
            sendData sendData = new sendData();
            sendData.execute(); // 웹 서버에 데이터 전송
        }
    }
