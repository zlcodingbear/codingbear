package sku.jyj.example.silvia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VoiceChoiceActivity extends AppCompatActivity {

    private Button btn_voiceadd, btn_setting01;
    private TextView textView45;
    private RecyclerView recyclerView;
    private VoiceChoiceAdapter voiceChoiceAdapter;
    private List<File> voiceFiles;
    private String memberNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voicechoice);

        btn_voiceadd = findViewById(R.id.btn_voiceadd);
        btn_setting01 = findViewById(R.id.btn_setting01);
        textView45 = findViewById(R.id.textView45);

        String content = textView45.getText().toString();
        SpannableString spannableString = new SpannableString(content);

        String word = "원하는 목소리";
        int start = content.indexOf(word);
        int end = start + word.length();

        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF8C00")), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView45.setText(spannableString);

        recyclerView = findViewById(R.id.rv01);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        voiceFiles = new ArrayList<>();
        voiceChoiceAdapter = new VoiceChoiceAdapter(this, voiceFiles, new VoiceChoiceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(File file) {
                // 선택한 파일을 서버에 전송하여 참조 파일로 설정
                new SetVoiceReferenceTask().execute(file.getName());
            }

            @Override
            public void onDeleteClick(File file) {
                // 삭제 후 파일 목록 갱신
                new DeleteVoiceFileTask().execute(file.getName());
            }

            @Override
            public void onPlayClick(File file) {
                // 재생
                new PlayVoiceFileTask().execute(file.getName());
            }
        });
        recyclerView.setAdapter(voiceChoiceAdapter);

        btn_voiceadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VoiceChoiceActivity.this, VoiceInputActivity.class);
                intent.putExtra("member_no", memberNo);
                startActivity(intent);
            }
        });

        btn_setting01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VoiceChoiceActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        loadVoiceFiles();
    }

    private void loadVoiceFiles() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String sessionId = sharedPreferences.getString("session_id", null);
        if (sessionId != null) {
            new FetchVoiceFilesTask().execute(sessionId);
        }
    }

    private class FetchVoiceFilesTask extends AsyncTask<String, Void, List<File>> {
        @Override
        protected List<File> doInBackground(String... params) {
            String sessionId = params[0];
            List<File> files = new ArrayList<>();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            Request request = new Request.Builder()
                    .url("http://lovelace0124.asuscomm.com:5003/voice_files?session_id=" + sessionId)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        JSONArray filesArray = jsonResponse.getJSONArray("files");
                        for (int i = 0; i < filesArray.length(); i++) {
                            JSONObject fileObj = filesArray.getJSONObject(i);
                            String fileName = fileObj.getString("file_name");
                            String filePath = fileObj.getString("file_path");
                            files.add(new File(filePath));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return files;
        }

        @Override
        protected void onPostExecute(List<File> files) {
            voiceFiles.clear();
            voiceFiles.addAll(files);
            voiceChoiceAdapter.notifyDataSetChanged();
        }
    }

    private class SetVoiceReferenceTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String fileName = params[0];
            SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
            String sessionId = sharedPreferences.getString("session_id", null);

            if (sessionId == null) {
                return false;
            }

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            JSONObject json = new JSONObject();
            try {
                json.put("session_id", sessionId);
                json.put("file_name", fileName);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("http://lovelace0124.asuscomm.com:5003/set_voice_reference")
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                return response.isSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(VoiceChoiceActivity.this, "목소리 참조 설정 성공", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(VoiceChoiceActivity.this, "목소리 참조 설정 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteVoiceFileTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String fileName = params[0];
            SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
            String sessionId = sharedPreferences.getString("session_id", null);

            if (sessionId == null) {
                return false;
            }

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            JSONObject json = new JSONObject();
            try {
                json.put("session_id", sessionId);
                json.put("file_name", fileName);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("http://lovelace0124.asuscomm.com:5003/delete_voice_file")
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                return response.isSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(VoiceChoiceActivity.this, "목소리 파일 삭제 성공", Toast.LENGTH_SHORT).show();
                loadVoiceFiles();
            } else {
                Toast.makeText(VoiceChoiceActivity.this, "목소리 파일 삭제 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class PlayVoiceFileTask extends AsyncTask<String, Void, File> {
        @Override
        protected File doInBackground(String... params) {
            String fileName = params[0];
            SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
            String sessionId = sharedPreferences.getString("session_id", null);

            if (sessionId == null) {
                return null;
            }

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            Request request = new Request.Builder()
                    .url("http://lovelace0124.asuscomm.com:5003/play_voice_file?session_id=" + sessionId + "&file_name=" + fileName)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    // 파일을 다운로드하여 로컬에 저장
                    File tempFile = File.createTempFile("voice", "tmp", getCacheDir());
                    InputStream inputStream = response.body().byteStream();
                    FileOutputStream outputStream = new FileOutputStream(tempFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();
                    return tempFile;
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(File file) {
            if (file != null) {
                try {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    FileInputStream fis = new FileInputStream(file);
                    mediaPlayer.setDataSource(fis.getFD());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Toast.makeText(VoiceChoiceActivity.this, "파일 재생 시작", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(VoiceChoiceActivity.this, "파일 재생 실패", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(VoiceChoiceActivity.this, "파일 재생 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
