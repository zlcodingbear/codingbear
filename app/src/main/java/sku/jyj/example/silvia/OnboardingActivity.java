package sku.jyj.example.silvia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OnboardingActivity extends AppCompatActivity {
    private LinearLayout SlidingPage;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        ViewPager2Adapter viewPager2Adapter
                = new ViewPager2Adapter(getSupportFragmentManager(), getLifecycle());
        ViewPager2 viewPager2 = findViewById(R.id.pager);
        viewPager2.setAdapter(viewPager2Adapter);

        SlidingPage = findViewById(R.id.SlidingPage);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                setCurrentIndicator(position);
            }
        });

        setupIndicators(3);

        Button btn_tosplash = findViewById(R.id.btn_gosplash);
        btn_tosplash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OnboardingActivity.this, SplashActivity.class);
                startActivity(intent);
            }
        });



        //=== TabLayout기능 추가 부분 ============================================
        /*TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText("Tab " + (position + 1));
            }
        }).attach();*/

    }

    private void setupIndicators(int count) {
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.bg_indicator_inactive));
            indicators[i].setLayoutParams(params);
            SlidingPage.addView(indicators[i]);
        }
        setCurrentIndicator(0);
    }

    private void setCurrentIndicator(int position) {
        int childCount = SlidingPage.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) SlidingPage.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.bg_indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.bg_indicator_inactive)); // 이 줄이 추가되어야 합니다.
            }
        }
    }
}

