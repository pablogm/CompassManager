package com.compassmanager.pablogm.compassmanager.Interfaces;

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

import android.hardware.Sensor;

public interface CompassManagerCallback
{
    /**
     * Called on each sensor update
     * @param fromAzimuth:      Old azimuth value
     * @param toAzimuth:        New azimuth value
     *                          The azimuth is the angle formed between a
     *                          reference direction (North) and a line from
     *                          the observer to a point of interest projected
     *                          on the same plane as the reference direction.
     *                          More info: http://en.wikipedia.org/wiki/Azimuth
     */
    void onSensorChanged(float fromAzimuth, float toAzimuth);

    /**
     * Called when the accuracy changes
     * @param sensor    Current sensor
     * @param accuracy  New accuracy
     */
    void onAccuracyChanged(Sensor sensor, int accuracy);
}
