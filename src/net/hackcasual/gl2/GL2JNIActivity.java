/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.hackcasual.gl2;

import net.hackcasual.BounceClock;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

public class GL2JNIActivity extends Activity implements SensorEventListener {

    GL2JNIView mView;

    boolean hasAccel = false;
    
    public static BounceClock backgroundRenderer;
    
    @Override protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mView = new GL2JNIView(getApplication());
        setContentView(mView);
    }

    @Override protected void onPause() {
        super.onPause();
        mView.onPause();
        backgroundRenderer.shutdown();
        backgroundRenderer = null;
        SensorManager mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensorManager.unregisterListener(this);
    }

    @Override protected void onResume() {
        super.onResume();
        mView.onResume();
        
        SensorManager mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		GL2JNILib.setGravity(Math.min(event.values[0] / 10.0f, event.values[1] / 10.0f), Math.max(event.values[0] / 10.0f, event.values[1] / 10.0f));
	}
}
