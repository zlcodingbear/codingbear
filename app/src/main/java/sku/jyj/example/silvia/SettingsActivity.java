package sku.jyj.example.silvia;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_preference, rootKey);

            // "회원정보 수정" Preference 찾기
            Preference userInfoPreference = findPreference("key_memberinfo");

            // "회원정보 수정" Preference에 대한 클릭 리스너 설정
            userInfoPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // 클릭 이벤트 처리 및 UserInfoChangeActivity 시작
                    Intent userInfoChangeIntent = new Intent(getActivity(), UserInfoActivity.class);
                    startActivity(userInfoChangeIntent);
                    return true;
                }
            });

            // "목소리 수정" Preference 찾기
            Preference voiceChangePreference = findPreference("key_tts_setting");

            // "목소리 수정" Preference에 대한 클릭 리스너 설정
            voiceChangePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    // 클릭 이벤트 처리 및 voiceChangePreference 시작
                    Intent voiceChangeIntent = new Intent(getActivity(), VoiceChoiceActivity.class);
                    startActivity(voiceChangeIntent);
                    return true;
                }
            });

            // "로그아웃" Preference 찾기
            Preference logoutPreference = findPreference("key_logout");

            // "로그아웃" Preference에 대한 클릭 리스너 설정
            /*logoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    // 클릭 이벤트 처리 및 LogoutActivity 시작
                    Intent logoutIntent = new Intent(getActivity(), LogoutActivity.class);
                    startActivity(logoutIntent);
                    return true;
                }
            });*/
        }
    }
}