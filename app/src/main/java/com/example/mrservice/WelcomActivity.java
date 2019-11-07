package com.example.mrservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.mrservice.adapter.MpagerAdapter;

import java.util.Timer;

public class WelcomActivity extends AppCompatActivity implements View.OnClickListener {
    private ViewPager mpager;
    private int[] layouts = {R.layout.intro_slide_forth, R.layout.intro_slide_first, R.layout.intro_slide_third
            , R.layout.intro_slide_second};
    private MpagerAdapter mpagerAdapter;

    private LinearLayout Dots_Layout;
    private ImageView[] dots;
    private Button BnNext, BnSkip;
    private Timer timer;
    private int current_position = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (new PreferenceManager(this).checkPreference()) {
            loadHome();
        }
        if (Build.VERSION.SDK_INT > 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_welcom);


        mpager = (ViewPager) findViewById(R.id.viewpager);
        mpagerAdapter = new MpagerAdapter(layouts, this);
        mpager.setAdapter(mpagerAdapter);
//        createSlideshow();

        Dots_Layout = (LinearLayout) findViewById(R.id.dotsLayout);
        BnNext = (Button) findViewById(R.id.btn_next);
        BnSkip = (Button) findViewById(R.id.btn_skip);
        BnNext.setOnClickListener(this);
        BnSkip.setOnClickListener(this);
        createDots(0);

        // hide the action bar


        mpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                createDots(position);
                if (position == layouts.length - 1) {
                    BnNext.setText("Finish");
                    BnSkip.setVisibility(View.INVISIBLE);
                } else {
                    BnNext.setText("Next");
                    BnSkip.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    private void createDots(int current_position) {
        if (Dots_Layout != null)
            Dots_Layout.removeAllViews();
        dots = new ImageView[layouts.length];

        for (int i = 0; i < layouts.length; i++) {
            dots[i] = new ImageView(this);
            if (i == current_position) {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_dots));
            } else {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.default_dots));

            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(4, 0, 4, 0);
            Dots_Layout.addView(dots[i], params);
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_next:
                loadNextSlide();
                break;
            case R.id.btn_skip:
                loadHome();
                new PreferenceManager(this).writeprefernce();
                break;
        }

    }
    private void loadHome() {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        finish();
    }


    private void loadNextSlide() {
        int next_slide = mpager.getCurrentItem() + 1;

        if (next_slide < layouts.length) {
            mpager.setCurrentItem(next_slide);

        } else {
            loadHome();
            new PreferenceManager(this).writeprefernce();
        }
    }

//    private void createSlideshow() {
//        timer = new Timer();
//        final Handler handler = new Handler();
//
//        final Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//
//
//                mpager.setCurrentItem(current_position++, true);
//                if (current_position == layouts.length) {
//                    timer.purge();
//                    timer.cancel();
//                    current_position = 0;
//
//                }
//
//
//            }
//        };
//
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                handler.post(runnable);
//
//            }
//        }, 250, 1500);
//    }

}
