package sku.jyj.example.silvia;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SecondFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView = view.findViewById(R.id.text02);
        String content1 = textView.getText().toString();
        SpannableString spannableString = new SpannableString(content1);

        // '실비아'라는 단어의 시작과 끝 인덱스를 찾습니다.
        String word1 = "원하는 목소리";
        int start = content1.indexOf(word1);
        int end = start + word1.length();

        // 해당 범위에 볼드 효과를 적용합니다.
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 텍스트뷰에 스타일이 적용된 텍스트를 설정합니다.
        textView.setText(spannableString);
    }
}