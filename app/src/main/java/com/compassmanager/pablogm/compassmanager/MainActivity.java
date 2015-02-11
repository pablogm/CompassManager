package com.compassmanager.pablogm.compassmanager;

/**
 Copyright 2015 Pablo GM <invanzert@gmail.com>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.compassmanager.pablogm.compassmanager.Compass.CompassManager;
import com.compassmanager.pablogm.compassmanager.Interfaces.CompassManagerCallback;


public class MainActivity extends ActionBarActivity implements CompassManagerCallback
{
    private CompassManager          compassManager;
    private ImageView               mPointer;

    // *********************************
    // * Life Cycle                    *
    // *********************************

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Remove title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);

        /// Compass
        compassManager  = new CompassManager(this, this);
        mPointer        = (ImageView) findViewById(R.id.pointer);

        Log.d(CompassManager.CompassManLog, "*** onCreate() ***");
    }


    @Override
    public void onResume()
    {
        super.onResume();

        /// Compass
        compassManager.register();

        Log.d(CompassManager.CompassManLog, "*** onResume() ***");
    }

    @Override
    public void onStop()
    {
        super.onStop();

        Log.d(CompassManager.CompassManLog, "*** onStop() ***");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        /// Compass
        compassManager.unregister();

        System.gc();

        Log.d(CompassManager.CompassManLog, "*** onDestroy() ***");
    }

    @Override
    public void onPause()
    {
        super.onPause();

        /// Compass
        compassManager.unregister();

        Log.d(CompassManager.CompassManLog, "*** onPause() ***");
    }


    // *********************************
    // * Compass methods               *
    // *********************************

    @Override
    public void onSensorChanged(float fromAzimuth, float toAzimuth)
    {
        // Show current and old orientation in a text view
        TextView orientationText = (TextView) findViewById(R.id.orientationTextId);
        orientationText.setText("Old -> " + String.format("%.4f", -fromAzimuth) + " New -> " + String.format("%.4f", toAzimuth));

        // Point North
        RotateAnimation ra = new RotateAnimation(
                fromAzimuth,
                -toAzimuth,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        ra.setDuration(250);

        ra.setFillAfter(true);

        mPointer.startAnimation(ra);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}
