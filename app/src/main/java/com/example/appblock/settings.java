package com.example.appblock;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.franmontiel.attributionpresenter.AttributionPresenter;
import com.franmontiel.attributionpresenter.entities.Attribution;
import com.franmontiel.attributionpresenter.entities.License;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Fragment fragment = new SettingsHolder();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            transaction.add(R.id.settings_holder, fragment, "settings_screen");
        }

        transaction.commit();

    }


    // below inner class is a fragment, which must be called in the main activity
    public static class SettingsHolder extends PreferenceFragment {

        //Buttons
        Preference help;
        Preference recent_changes;
        Preference libraries;
        CheckBoxPreference DND;
        NotificationManager mNotificationManager;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            // Settings UI
            addPreferencesFromResource(R.xml.settings_ui);

            init();

        }

        public void init() {
            //Preference Buttons
            help = findPreference("help");
            recent_changes = findPreference("recent_changes");
            libraries = findPreference("libraries");
            DND = (CheckBoxPreference) findPreference("cb2");
            if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                DND.setEnabled(false);
            }

            //N Manager
            mNotificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);

            //DND CheckBox Listener
            DND.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Check if the notification policy access has been granted for the app.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (DND.isChecked()) {
                            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                                new LovelyStandardDialog(getActivity(), LovelyStandardDialog.ButtonLayout.VERTICAL)
                                        .setTopColorRes(R.color.red)
                                        .setIcon(R.drawable.ic_baseline_perm_device_information_24)
                                        .setTitle(R.string.Settings_dialog3_T)
                                        .setMessage(R.string.Settings_dialog3_D)
                                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                new LovelyStandardDialog(getActivity())
                                                        .setTopColorRes(R.color.blue)
                                                        .setIcon(R.drawable.ic_baseline_perm_device_information_24)
                                                        .setTitle(getString(R.string.Settings_dialog2_T))
                                                        .setMessage(getString(R.string.Settings_dialog2_D))
                                                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                                                startActivity(intent);
                                                                Toast.makeText(getActivity(), getString(R.string.Toast_1), Toast.LENGTH_LONG).show();
                                                            }
                                                        })
                                                        .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                DND.setChecked(false);
                                                            }
                                                        })
                                                        .setCancelable(false)
                                                        .show();
                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            } else {
                                new LovelyStandardDialog(getActivity())
                                        .setTopColorRes(R.color.red)
                                        .setIcon(R.drawable.ic_baseline_perm_device_information_24)
                                        .setTitle(R.string.Settings_dialog3_T)
                                        .setMessage(R.string.Settings_dialog3_D)
                                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Toast.makeText(getActivity(), getString(R.string.Toast_1), Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                DND.setChecked(false);
                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Notifications Enabled", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
            });


            help.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //Show Help
                    new LovelyCustomDialog(getActivity())
                            .setTopColorRes(R.color.blue)
                            .setTitle(getString(R.string.drawer_item2_dialog_title))
                            .setMessage(getString(R.string.drawer_item2_dialog_message))
                            .setIcon(R.drawable.ic_baseline_help_outline_24)
                            .show();
                    return true;
                }
            });
            libraries.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                   // Show Libraries
                    libraries_used();
                    return true;
                }
            });
        }

        public void libraries_used() {
            AttributionPresenter attributionPresenter = new AttributionPresenter.Builder(this.getActivity())
                    .addAttributions(
                            new Attribution.Builder("Android-gif-drawable")
                                    .addCopyrightNotice("Copyright (c) 2013 - present Karol Wrótniak, Droids on Roids")
                                    .addLicense(License.MIT)
                                    .setWebsite("https://github.com/koral--/android-gif-drawable")
                                    .build()
                    )
                    .addAttributions(
                            new Attribution.Builder("Root Beer")
                                    .addCopyrightNotice("Copyright (c) 2015, Scott Alexander-Bown, Mat Rollings")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/scottyab/rootbeer")
                                    .build()
                    )
                    .addAttributions(
                            new Attribution.Builder("Lovely Dialog")
                                    .addCopyrightNotice("Copyright (c) 2016 Yaroslav Shevchuk")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/yarolegovich/LovelyDialog")
                                    .build()
                    )
                    .addAttributions(
                            new Attribution.Builder("Material Drawer")
                                    .addCopyrightNotice("Copyright (c) 2018 Mike Penz")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/mikepenz/MaterialDrawer")
                                    .build()
                    )
                    .addAttributions(
                            new Attribution.Builder("AttributionPresenter")
                                    .addCopyrightNotice("Copyright (c) 2017 Francisco José Montiel Navarro")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/franmontiel/AttributionPresenter")
                                    .build()
                    )
                    .addAttributions(
                            new Attribution.Builder("SwipeSelector")
                                    .addCopyrightNotice("Copyright (c) 2016 Iiro Krankka")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/roughike/SwipeSelector")
                                    .build()
                    )
                    .addAttributions(
                            new Attribution.Builder("CafeBar")
                                    .addCopyrightNotice("Copyright (c) 2017 Dani Mahardhika")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/danimahardhika/cafebar")
                                    .build()
                    )
                    .addAttributions(
                            new Attribution.Builder("Android-About-Page")
                                    .addCopyrightNotice("Copyright (c) 2016 Mehdi Sakout")
                                    .addLicense(License.MIT)
                                    .setWebsite("https://github.com/medyo/android-about-page")
                                    .build()
                    )
                    .addAttributions(
                            new Attribution.Builder("Stetho")
                                    .addCopyrightNotice("Copyright (c) 2015, Facebook, Inc.")
                                    .addLicense(License.BSD_3)
                                    .setWebsite("https://github.com/facebook/stetho")
                                    .build()
                    )
                    .addAttributions(
                            new Attribution.Builder("Android-Multi-Select-Dialog")
                                    .addCopyrightNotice("Copyright (c) 2017 Abubakker Moallim")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/abumoallim/Android-Multi-Select-Dialog")
                                    .build()
                    )
                    .build();
            attributionPresenter.showDialog("Libraries Used");
        }

    }
}