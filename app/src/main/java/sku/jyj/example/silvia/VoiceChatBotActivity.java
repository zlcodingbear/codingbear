package sku.jyj.example.silvia;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VoiceChatBotActivity extends AppCompatActivity {
    private static final String TAG = "VoiceChatBotActivity";
    private static final String BASE_URL = "http://lovelace0124.asuscomm.com:5003";
    private static final String UPLOAD_URL = BASE_URL + "/chatting";
    private static final String CHECK_STATUS_URL = BASE_URL + "/check_status";
    private static final String GET_RESPONSE_URL = BASE_URL + "/get_voice_response";
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String SESSION_ID_KEY = "session_id";

    private AudioRecord audioRecord;
    private boolean recording = false;
    private int bufferSize;
    private ByteArrayOutputStream audioData;
    private MediaPlayer mediaPlayer;
    private Toast currentToast;

    private ImageButton btnSttStart;
    private Button btnVoicePlay;
    private Button btnSettings;
    private Button btnEmo;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private Handler handler;
    private Runnable checkStatusRunnable;
    private static final int CHECK_INTERVAL = 5000; // 5 seconds
    private String sessionId;
    final int PERMISSION = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voicechatbot);

        // Request permissions
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSION);
        }

        // Initialize session ID
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        sessionId = sharedPreferences.getString("session_id", null);

        // Initialize UI elements
        btnSttStart = findViewById(R.id.btn_sttStart);
        btnVoicePlay = findViewById(R.id.btn_voiceplay);
        btnSettings = findViewById(R.id.btn_settings);
        btnEmo = findViewById(R.id.btn_emo);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter();
        chatRecyclerView.setAdapter(chatAdapter);
        
        // Button click listeners
        btnSttStart.setOnClickListener(v -> {
            if (!recording) {
                startRecord();
                showToast("녹음을 시작하겠습니다.");
            } else {
                stopRecord();
            }
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent1 = new Intent(VoiceChatBotActivity.this, SettingsActivity.class);
            startActivity(intent1);
        });

        btnEmo.setOnClickListener(v -> {
            Intent intent12 = new Intent(VoiceChatBotActivity.this, AnalysisActivity.class);
            startActivity(intent12);
        });

        btnVoicePlay.setOnClickListener(v -> {
            String sessionId = sharedPreferences.getString("session_id", null);
            if (sessionId != null) {
                playVoiceResponse(sessionId);
            }
        });

        // Initialize AudioRecord
        bufferSize = AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        handler = new Handler();
    }

    private void startRecord() {
        recording = true;
        audioData = new ByteArrayOutputStream();
        audioRecord.startRecording();

        new Thread(() -> {
            byte[] buffer = new byte[bufferSize];
            while (recording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    audioData.write(buffer, 0, read);
                }
            }
            audioRecord.stop();
            saveAudioToFile();
        }).start();
    }

    private void stopRecord() {
        recording = false;
        showToast("음성 기록을 중지합니다.");
        uploadVoiceFile();
    }

    private void saveAudioToFile() {
        try {
            File tempAudioFile = File.createTempFile("temp_audio", ".pcm", getCacheDir());
            try (FileOutputStream fos = new FileOutputStream(tempAudioFile)) {
                audioData.writeTo(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showToast("오디오 파일 저장 중 오류가 발생했습니다.");
        }
    }

    private void uploadVoiceFile() {
        new UploadFileTask().execute();
    }

    private class UploadFileTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                File tempAudioFile = File.createTempFile("temp_audio", ".pcm", getCacheDir());
                try (FileOutputStream fos = new FileOutputStream(tempAudioFile)) {
                    audioData.writeTo(fos);
                }

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
                String sessionId = sharedPreferences.getString("session_id", null);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", tempAudioFile.getName(),
                                RequestBody.create(MediaType.parse("audio/pcm"), tempAudioFile))
                        .addFormDataPart("session_id", sessionId)
                        .addFormDataPart("file_name", "temp_audio") // 파일 이름 추가
                        .build();

                Request request = new Request.Builder()
                        .url(UPLOAD_URL)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return new JSONObject(response.body().string());
                } else {
                    return null;
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "File upload failed", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                try {
                    boolean success = result.getBoolean("success");
                    if (success) {
                        String transcribedText = result.getString("transcribed_text");
                        String responseText = result.getString("response_text");

                        addChatMessage(transcribedText, true); // 사용자의 메시지 추가
                        addChatMessage(responseText, false); // 챗봇의 응답 추가

                        showToast("파일 업로드 및 응답 성공");
                    } else {
                        showToast("파일 업로드 실패: " + result.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToast("응답 처리 중 오류가 발생했습니다. 다시 시도해 주세요.");
                }
            } else {
                showToast("파일 업로드 실패");
            }
        }
    }

    private void playVoiceResponse(String sessionId) {
        String url = "http://lovelace0124.asuscomm.com:5003/play_answer?session_id=" + sessionId;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                showToast("응답 파일을 가져오는 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    showToast("응답 파일을 가져오는 데 실패했습니다. 다시 시도해 주세요.");
                    return;
                }

                File outputFile = new File(getCacheDir(), "response.wav");
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(response.body().bytes());
                }

                runOnUiThread(() -> playAudio(outputFile));
            }
        });
    }

    private void playAudio(File audioFile) {
        Log.d("VoiceChatBotActivity", "playAudio called with file: " + audioFile.getAbsolutePath());
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            showToast("오디오 재생 중 오류가 발생했습니다. 다시 시도해 주세요.");
        }

        mediaPlayer.setOnCompletionListener(mp -> {
            mediaPlayer.release();
            mediaPlayer = null;
        });
    }

    private void addChatMessage(String message, boolean isUser) {
        runOnUiThread(() -> {
            chatAdapter.addMessage(new ChatMessage(message, isUser));
            chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
        });
    }

    private void showToast(final String message) {
        runOnUiThread(() -> {
            if (currentToast != null) {
                currentToast.cancel();
            }
            currentToast = Toast.makeText(VoiceChatBotActivity.this, message, Toast.LENGTH_SHORT);
            currentToast.show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용됨
            } else {
                // 권한이 거부됨
                showToast("권한이 거부되었습니다.");
            }
        }
    }
}
