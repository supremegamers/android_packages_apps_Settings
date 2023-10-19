/*
 * Copyright (C) 2017-2021 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.libremobileos.statusbar;

import android.content.ContentResolver;
import android.os.Bundle;
import android.view.View;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.libremobileos.support.preferences.SecureSettingSwitchPreference;
import com.android.settings.libremobileos.utils.DeviceUtils;

public class NetworkTrafficSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener  {

    private static final String TAG = "NetworkTrafficSettings";
    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";

    private static final int POSITION_START = 0;
    private static final int POSITION_CENTER = 1;
    private static final int POSITION_END = 2;

    private DropDownPreference mNetTrafficMode;
    private DropDownPreference mNetTrafficPosition;
    private SecureSettingSwitchPreference mNetTrafficAutohide;
    private DropDownPreference mNetTrafficUnits;
    private SecureSettingSwitchPreference mNetTrafficShowUnits;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.network_traffic_settings);
        getActivity().setTitle(R.string.network_traffic_settings_title);

        final ContentResolver resolver = getActivity().getContentResolver();

        mNetTrafficMode = findPreference(Settings.Secure.NETWORK_TRAFFIC_MODE);
        mNetTrafficMode.setOnPreferenceChangeListener(this);
        int mode = Settings.Secure.getInt(resolver,
                Settings.Secure.NETWORK_TRAFFIC_MODE, 0);
        mNetTrafficMode.setValue(String.valueOf(mode));

        final boolean hasCenteredCutout = DeviceUtils.hasCenteredCutout(getActivity());
        final boolean disallowCenteredTraffic = hasCenteredCutout || getClockPosition() == 1;

        mNetTrafficPosition = findPreference(Settings.Secure.NETWORK_TRAFFIC_POSITION);
        mNetTrafficPosition.setOnPreferenceChangeListener(this);

        // Adjust network traffic preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (disallowCenteredTraffic) {
                mNetTrafficPosition.setEntries(R.array.network_traffic_position_entries_notch_rtl);
                mNetTrafficPosition.setEntryValues(R.array.network_traffic_position_values_notch);
            } else {
                mNetTrafficPosition.setEntries(R.array.network_traffic_position_entries_rtl);
                mNetTrafficPosition.setEntryValues(R.array.network_traffic_position_values);
            }
        } else {
            if (disallowCenteredTraffic) {
                mNetTrafficPosition.setEntries(R.array.network_traffic_position_entries_notch);
                mNetTrafficPosition.setEntryValues(R.array.network_traffic_position_values_notch);
            } else {
                mNetTrafficPosition.setEntries(R.array.network_traffic_position_entries);
                mNetTrafficPosition.setEntryValues(R.array.network_traffic_position_values);
            }
        }

        int position = Settings.Secure.getInt(resolver,
                Settings.Secure.NETWORK_TRAFFIC_POSITION, POSITION_CENTER);

        if (disallowCenteredTraffic && position == POSITION_CENTER) {
            position = POSITION_END;
            Settings.Secure.putInt(getActivity().getContentResolver(),
                Settings.Secure.NETWORK_TRAFFIC_POSITION, position);
        }
        mNetTrafficPosition.setValue(String.valueOf(position));

        mNetTrafficAutohide = findPreference(Settings.Secure.NETWORK_TRAFFIC_AUTOHIDE);
        mNetTrafficAutohide.setOnPreferenceChangeListener(this);

        mNetTrafficUnits = findPreference(Settings.Secure.NETWORK_TRAFFIC_UNITS);
        mNetTrafficUnits.setOnPreferenceChangeListener(this);
        int units = Settings.Secure.getInt(resolver,
                Settings.Secure.NETWORK_TRAFFIC_UNITS, /* Mbps */ 1);
        mNetTrafficUnits.setValue(String.valueOf(units));

        mNetTrafficShowUnits = findPreference(Settings.Secure.NETWORK_TRAFFIC_SHOW_UNITS);
        mNetTrafficShowUnits.setOnPreferenceChangeListener(this);

        updateEnabledStates(mode);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNetTrafficMode) {
            int mode = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.NETWORK_TRAFFIC_MODE, mode);
            updateEnabledStates(mode);
        } else if (preference == mNetTrafficPosition) {
            int position = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.NETWORK_TRAFFIC_POSITION, position);
        } else if (preference == mNetTrafficUnits) {
            int units = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.NETWORK_TRAFFIC_UNITS, units);
        }
        return true;
    }

    private void updateEnabledStates(int mode) {
        final boolean enabled = mode != 0;
        mNetTrafficPosition.setEnabled(enabled);
        mNetTrafficAutohide.setEnabled(enabled);
        mNetTrafficUnits.setEnabled(enabled);
        mNetTrafficShowUnits.setEnabled(enabled);
    }

    private int getClockPosition() {
        return Settings.System.getInt(getActivity().getContentResolver(),
                STATUS_BAR_CLOCK_STYLE, 2);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.LMODROID;
    }
}
