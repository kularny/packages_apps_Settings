/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.android.settings.cyanogenmod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.settings.DisplaySettings;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.hardware.DisplayColor;
import com.android.settings.hardware.DisplayGamma;
import com.android.settings.hardware.VibratorIntensity;
import com.android.settings.location.LocationSettings;
import com.android.settings.probam.BatterySaverHelper;

import java.util.Arrays;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    private static final String KSM_SETTINGS_PROP = "sys.ksm.restored";
    private static final String CHARGE_SETTINGS_PROP = "sys.charge.restored";

    @Override
    public void onReceive(Context ctx, Intent intent) {

        if (Utils.fileExists(MemoryManagement.KSM_RUN_FILE)) {
            if (SystemProperties.getBoolean(KSM_SETTINGS_PROP, false) == false
                    && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                SystemProperties.set(KSM_SETTINGS_PROP, "true");
                configureKSM(ctx);
            } else {
                SystemProperties.set(KSM_SETTINGS_PROP, "false");
            }
        }

        if (PerformanceSettings.FAST_CHARGE_PATH != null) {
            if (SystemProperties.getBoolean(CHARGE_SETTINGS_PROP, false) == false
                    && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                SystemProperties.set(CHARGE_SETTINGS_PROP, "true");
                configureCharge(ctx);
            } else {
                SystemProperties.set(CHARGE_SETTINGS_PROP, "false");
            }
        if (BatterySaverHelper.deviceSupportsMobileData(ctx)) {
            BatterySaverHelper.scheduleService(ctx);
        }
        BatterySaverHelper.scheduleService(ctx);

        /* Restore the hardware tunable values */
        DisplayColor.restore(ctx);
        DisplayGamma.restore(ctx);
        VibratorIntensity.restore(ctx);
        DisplaySettings.restore(ctx);
        LocationSettings.restore(ctx);
    }

    private void configureKSM(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        boolean ksmDefault = (SystemProperties.get("ro.ksm.default", "0") != "0");
        boolean ksm = prefs.getBoolean(MemoryManagement.KSM_PREF, ksmDefault);

        Utils.fileWriteOneLine(MemoryManagement.KSM_RUN_FILE, ksm ? "1" : "0");
        Log.d(TAG, "KSM settings restored.");
    }

    private void configureCharge(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        boolean charge = prefs.getBoolean(PerformanceSettings.KEY_FORCE_FAST_CHARGE, false);

        Utils.fileWriteOneLine(PerformanceSettings.FAST_CHARGE_PATH, charge ? "1" : "0");
        Log.d(TAG, "Fast Charge settings restored.");
    }
}
