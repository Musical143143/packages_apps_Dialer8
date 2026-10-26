package com.android.incallui.ios27;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Ios27GestureController implements SensorEventListener {
    public interface GestureCallback {
        void onAirWaveAnswer();
        void onFlipToSilence();
    }

    private final SensorManager sensorManager;
    private final Sensor proximitySensor;
    private final Sensor accelerometer;
    private final GestureCallback callback;
    private long lastNearTimestamp = 0L;

    public Ios27GestureController(Context context, GestureCallback callback) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.proximitySensor = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        this.accelerometer = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.callback = callback;
    }

    public void startListening() {
        if (sensorManager == null) return;
        if (proximitySensor == null) {
            // no-op
        } else {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
        }
        if (accelerometer == null) {
            // no-op
        } else {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stopListening() {
        if (sensorManager == null) return;
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (callback == null || event == null) return;
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            float maxRange = event.sensor.getMaximumRange();
            long now = System.currentTimeMillis();
            if (distance < maxRange) {
                lastNearTimestamp = now;
            } else if (lastNearTimestamp > 0 && (now - lastNearTimestamp) < 650L) {
                lastNearTimestamp = 0L;
                callback.onAirWaveAnswer();
            }
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float z = event.values[2];
            if (z < -9.0f) {
                callback.onFlipToSilence();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
