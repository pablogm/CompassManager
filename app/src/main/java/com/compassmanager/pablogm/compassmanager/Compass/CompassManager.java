package com.compassmanager.pablogm.compassmanager.Compass;

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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationEventListener;

import com.compassmanager.pablogm.compassmanager.Interfaces.CompassManagerCallback;


public class CompassManager implements SensorEventListener
{
    private SensorManager               mSensorManager;
    private Sensor                      mAccelerometer;
    private Sensor                      mMagnetometer;
    private Sensor                      mRotation;
    private Sensor                      mOrientationSensor;

    private float[]                     mLastAccelerometer               = new float[3];
    private float[]                     mLastMagnetometer                = new float[3];
    private float[]                     mR                               = new float[9];
    private float[]                     mOrientation                     = new float[3];
    private float[]                     rotMat                           = new float[9];

    private boolean                     mLastAccelerometerSet            = false;
    private boolean                     mLastMagnetometerSet             = false;

    private float                       mCurrentDegree                   = 0f;
    private int                         orientationValue;

    private CompassManagerCallback      callback;

    private OrientationEventListener    mOrientationEventListener;

    public static final float           TWENTY_FIVE_DEGREE_IN_RADIAN    = 0.436332313f;
    public static final float           ONE_FIFTY_FIVE_DEGREE_IN_RADIAN = 2.7052603f;
    public static final int             FORTY_FIVE_IN_DEGREES           = 45;
    public static final int             NINETY_IN_DEGREES               = 90;
    public static final int             ONE_THIRTY_FIVE_IN_DEGREES      = 135;
    public static final int             TWO_TWENTY_FIVE_IN_DEGREES      = 225;
    public static final int             THREE_FIFTEEN_IN_DEGREES        = 315;
    public static final float           THREE_SIXTY_IN_DEGREES          = 360.0f;

    public static final String          CompassManLog                   = "CompassManagerSample";


    // *********************************
    // *            API                *
    // *********************************

    /**
     * Default constructor
     * @param context Parent context
     * @param callback Compass manager callback
     */
    public CompassManager(Context context, CompassManagerCallback callback)
    {
        // Init callback
        this.callback = callback;

        // Init sensors
        mSensorManager      = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer      = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer       = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mOrientationSensor  = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mRotation           = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mOrientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL)
        {
            @Override
            public void onOrientationChanged(int orientation)
            {
                orientationValue = orientation;
            }
        };
    }

    /**
     * Register sensors
     */
    public void register()
    {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);

        if (mOrientationEventListener.canDetectOrientation())
        {
            mOrientationEventListener.enable();
        }
    }

    /**
     * Unregister sensors
     */
    public void unregister()
    {
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
        mSensorManager.unregisterListener(this, mRotation);
        mSensorManager.unregisterListener(this, mOrientationSensor);
        mOrientationEventListener.disable();
    }


    // *********************************
    // * SensorEventListener methods   *
    // *********************************

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if( mRotation == null )
        {
            if (event.sensor == mOrientationSensor)
            {
                onOrientationSensorUpdate(event);
            }
        }
        else
        {
            if ( event.sensor == mRotation )
            {
                onRotationVectorUpdate( event );
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        /* CALLBACK */
        this.callback.onAccuracyChanged(sensor, accuracy);
    }


    // *********************************
    // * SensorManager private methods *
    // *********************************

    /**
     * Update rotation on sensor update
     * @deprecated use CompassManager.onRotationVectorUpdate(SensorEvent event) or CompassManager.onAccelerometerMagnetometerUpdate(SensorEvent event) instead.
     * @param event Sensor event
     */
    private void onOrientationSensorUpdate(SensorEvent event)
    {
        /// Filtering
        float degree            = lowPassFilter(event.values[0], -mCurrentDegree);

        /* CALLBACK */
        this.callback.onSensorChanged(mCurrentDegree, degree);

        mCurrentDegree = -degree;
    }

    /**
     * Update rotation on sensor update
     * FIXME: Does not work when the device is flat
     * @param event Sensor event
     */
    private void onAccelerometerMagnetometerUpdate(SensorEvent event)
    {
        if (event.sensor == mAccelerometer)
        {
            mLastAccelerometer = event.values;
            mLastAccelerometerSet = true;
        }
        else if (event.sensor == mMagnetometer)
        {
            mLastMagnetometer = event.values;
            mLastMagnetometerSet = true;
        }
        else
        {
            mLastAccelerometerSet = mLastMagnetometerSet = false;

            Log.e(CompassManLog, "Couldn't set gravity nor geo magnetic values");
        }

        if (mLastAccelerometerSet && mLastMagnetometerSet)
        {
            boolean success = SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);

            if( success )
            {
                SensorManager.getOrientation(mR, mOrientation);

                float azimuthInRadians = mOrientation[0]; // orientation contains: azimut, pitch and roll

                float azimuthInDegress  = (float)(Math.toDegrees(azimuthInRadians) + THREE_SIXTY_IN_DEGREES) % THREE_SIXTY_IN_DEGREES;

                /// Filtering
                float degree            = lowPassFilter(azimuthInDegress, -mCurrentDegree);

                /* CALLBACK */
                this.callback.onSensorChanged(mCurrentDegree, degree);

                mCurrentDegree = -degree;
            }
            else
            {
                Log.e(CompassManLog, "Couldn't get rotation matrix");
            }
        }
    }

    /**
     * Update rotation on sensor update
     * @param event Sensor event
     */
    public void onRotationVectorUpdate(SensorEvent event)
    {
        SensorManager.getRotationMatrixFromVector(rotMat, event.values);

        // Get the inclination ( i.e. the degree of tilt by the device independent of orientation (portrait or landscape) ).
        // If less than 25 or more than 155 degrees the device is considered lying flat
        //
        float inclination = (float) Math.acos(rotMat[8]);

        if (inclination < TWENTY_FIVE_DEGREE_IN_RADIAN || inclination > ONE_FIFTY_FIVE_DEGREE_IN_RADIAN)
        {
            SensorManager.remapCoordinateSystem(rotMat, SensorManager.AXIS_X, SensorManager.AXIS_Y, rotMat);
        }
        else
        {
            SensorManager.remapCoordinateSystem(rotMat, SensorManager.AXIS_X, SensorManager.AXIS_Z, rotMat);
        }

        SensorManager.getOrientation(rotMat, mOrientation);

        float azimuthInRadians  = mOrientation[0]; // orientation contains: azimut, pitch and roll

        float azimuthInDegress  = (float)(Math.toDegrees(azimuthInRadians) + THREE_SIXTY_IN_DEGREES) % THREE_SIXTY_IN_DEGREES;

        azimuthInDegress        = applyOrientationCorrection( azimuthInDegress );

        /// Filtering
        float degree            = lowPassFilter(azimuthInDegress, -mCurrentDegree);

        /* CALLBACK */
        this.callback.onSensorChanged(mCurrentDegree, degree);

        mCurrentDegree = -degree;
    }

    /**
     * Correct azimuth according to orientation
     * @param val Azimuth value
     * @return Corrected azimuth value
     */
    private float applyOrientationCorrection( float val )
    {
        if ( orientationValue > THREE_FIFTEEN_IN_DEGREES && orientationValue < FORTY_FIVE_IN_DEGREES )
        {
            val += 0;
        }
        else if ( orientationValue > FORTY_FIVE_IN_DEGREES && orientationValue < ONE_THIRTY_FIVE_IN_DEGREES )
        {
            val += NINETY_IN_DEGREES;
        }
        else if( orientationValue > ONE_THIRTY_FIVE_IN_DEGREES && orientationValue < TWO_TWENTY_FIVE_IN_DEGREES )
        {
            val += 2 * NINETY_IN_DEGREES;
        }
        else if( orientationValue > TWO_TWENTY_FIVE_IN_DEGREES && orientationValue < THREE_FIFTEEN_IN_DEGREES)
        {
            val += 3 * NINETY_IN_DEGREES;
        }

        return val;
    }

    /**
     * Low pass filer and normalize values
     * @param newVal New value
     * @param oldVal Previous value
     * @return  Filtered and normalized value
     */
    public float lowPassFilter(float newVal, float oldVal)
    {
        float  smoothFactor     = 0.2f;
        float  soothThreshold   = 150.0f;

        if (Math.abs(newVal - oldVal) < THREE_SIXTY_IN_DEGREES / 2)
        {
            if (Math.abs(newVal - oldVal) > soothThreshold)
            {
                oldVal = newVal;
            }
            else
            {
                oldVal = oldVal + smoothFactor * (newVal - oldVal);
            }
        }
        else
        {
            if (THREE_SIXTY_IN_DEGREES - Math.abs(newVal - oldVal) > soothThreshold)
            {
                oldVal = newVal;
            }
            else
            {
                if (oldVal > newVal)
                {
                    oldVal = (oldVal + smoothFactor * ((THREE_SIXTY_IN_DEGREES + newVal - oldVal) % THREE_SIXTY_IN_DEGREES) + THREE_SIXTY_IN_DEGREES) % THREE_SIXTY_IN_DEGREES;
                }
                else
                {
                    oldVal = (oldVal - smoothFactor * ((THREE_SIXTY_IN_DEGREES - newVal + oldVal) % THREE_SIXTY_IN_DEGREES) + THREE_SIXTY_IN_DEGREES) % THREE_SIXTY_IN_DEGREES;
                }
            }
        }
        return oldVal;
    }
}
