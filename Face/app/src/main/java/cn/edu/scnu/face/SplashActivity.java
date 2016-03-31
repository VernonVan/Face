package cn.edu.scnu.face;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import com.hanks.htextview.HTextView;
import com.hanks.htextview.HTextViewType;

/**
 * Created by Vernon on 2015/12/25.
 */
public class SplashActivity extends AppCompatActivity
{
    private HTextView hTextView;

    /**
     * Duration of wait
     **/
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.splash_screen);

        hTextView = (HTextView) findViewById(R.id.tagline_htext);
        hTextView.setAnimateType(HTextViewType.LINE);
        hTextView.animateText(getResources().getText(R.string.tagline));        // animate

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                /* Create an Intent that will start the Menu-Activity. */
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
