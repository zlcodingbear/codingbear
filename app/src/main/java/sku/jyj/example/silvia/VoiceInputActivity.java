package sku.jyj.example.silvia;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

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

public class VoiceInputActivity extends AppCompatActivity {

    private static final String UPLOAD_URL = "http://lovelace0124.asuscomm.com:5003/upload";

    private AudioRecord audioRecord;
    private int bufferSize;
    private ByteArrayOutputStream audioData;
    private boolean recording = false;

    private MediaPlayer mediaPlayer = null;
    private boolean playing = false;

    final int PERMISSION = 1;
    private Button btn_repeat, btn_check;
    private ImageButton btn_record;
    private TextView textView34, textView3;

    private String memberNo;
    private File tempAudioFile;
    private String fileName;  // 파일 이름을 저장할 변수

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_input);

        // 사용자 ID 받기
        Intent intent = getIntent();
        memberNo = intent.getStringExtra("member_no");

        btn_record = findViewById(R.id.btn_record);
        btn_repeat = findViewById(R.id.btn_repeat);
        btn_check = findViewById(R.id.btn_check);

        textView34 = findViewById(R.id.textView34);
        textView3 = findViewById(R.id.textView3);

        setSpannableText(textView34, "원하는 목소리를", "#FF8C00");
        setSpannableText(textView3, "문장 읽어보기", "#FF8C00");

        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION);
        }

        Toolbar voice_toolbar1 = findViewById(R.id.voice_toolbar1);
        setSupportActionBar(voice_toolbar1);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bufferSize = AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        btn_record.setOnClickListener(v -> {
            if (!recording) {
                StartRecord();
                Toast.makeText(getApplicationContext(), "녹음을 시작하겠습니다.", Toast.LENGTH_SHORT).show();
            } else {
                StopRecord();
            }
        });

        btn_repeat.setOnClickListener(v -> {
            if (!playing) {
                PlayRecord();
                Toast.makeText(getApplicationContext(), "녹음을 재생하겠습니다.", Toast.LENGTH_SHORT).show();
            } else {
                StopAudio();
            }
        });

        btn_check.setOnClickListener(v -> {
            if (tempAudioFile != null && tempAudioFile.exists()) {
                uploadVoiceFile(tempAudioFile);
            } else {
                Toast.makeText(getApplicationContext(), "녹음된 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSpannableText(TextView textView, String targetText, String color) {
        if (textView == null || textView.getText() == null) {
            return;
        }
        String content = textView.getText().toString();
        SpannableString spannableString = new SpannableString(content);

        int start = content.indexOf(targetText);
        int end = start + targetText.length();

        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(color)), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
    }

    private void StartRecord() {
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

    private void StopRecord() {
        recording = false;
        Toast.makeText(getApplicationContext(), "음성 기록을 중지합니다.", Toast.LENGTH_SHORT).show();
        showFileNameDialog();
    }

    private void saveAudioToFile() {
        try {
            tempAudioFile = File.createTempFile("temp_audio", ".pcm", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempAudioFile);
            audioData.writeTo(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void PlayRecord() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempAudioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            playing = true;

            mediaPlayer.setOnCompletionListener(mp -> StopAudio());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void StopAudio() {
        playing = false;
        mediaPlayer.stop();
    }

    private void uploadVoiceFile(File file) {
        new UploadFileTask().execute(file);
    }

    private class UploadFileTask extends AsyncTask<File, Void, Boolean> {
        @Override
        protected Boolean doInBackground(File... params) {
            File file = params[0];
            Log.d("UploadFileTask", "Uploading file: " + file.getAbsolutePath());

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
            String sessionId = sharedPreferences.getString("session_id", null);

            if (sessionId == null) {
                return false;
            }

            MediaType MEDIA_TYPE_PCM = MediaType.parse("audio/pcm");

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName + ".pcm",  // 파일 이름을 포함하여 전송
                            RequestBody.create(MEDIA_TYPE_PCM, file))
                    .addFormDataPart("session_id", sessionId)
                    .addFormDataPart("member_no", memberNo != null ? memberNo : "")
                    .addFormDataPart("file_name", fileName != null ? fileName : "audio")  // 파일 이름 추가
                    .build();

            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                Log.d("UploadFileTask", "Response: " + response);
                return response.isSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(VoiceInputActivity.this, "파일 업로드 성공", Toast.LENGTH_SHORT).show();
                // 파일 업로드가 성공하면 이전 페이지로 이동
                finish();
            } else {
                Toast.makeText(VoiceInputActivity.this, "파일 업로드 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showFileNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_voice_input, null);
        builder.setView(dialogView);

        final EditText editTextFileName = dialogView.findViewById(R.id.editTextText2);
        Button btnCheck = dialogView.findViewById(R.id.btn_check);
        Button btnNo = dialogView.findViewById(R.id.btn_no);

        AlertDialog dialog = builder.create();

        btnCheck.setOnClickListener(v -> {
            Log.d("showFileNameDialog", "작성 완료 버튼 클릭됨");
            fileName = editTextFileName.getText().toString();  // 입력받은 파일 이름을 저장
            if (!fileName.isEmpty()) {
                uploadVoiceFile(tempAudioFile);
                Toast.makeText(getApplicationContext(), "파일 이름 저장 완료", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getApplicationContext(), "파일 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        btnNo.setOnClickListener(v -> {
            Log.d("showFileNameDialog", "이전으로 버튼 클릭됨");
            dialog.dismiss();
        });

        dialog.show();
    }
}
