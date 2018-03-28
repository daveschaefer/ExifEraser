// Copyright (c) 2018, Tom Geiselmann
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software
// and associated documentation files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge, publish, distribute,
// sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or
// substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
// INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
// IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY,WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.none.tom.exiferaser.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import com.none.tom.exiferaser.R;

import static com.none.tom.exiferaser.util.Constants.KEY_INVERT;
import static com.none.tom.exiferaser.util.Constants.REQUEST_CODE_CREATE_DOCUMENT;
import static com.none.tom.exiferaser.util.Constants.REQUEST_CODE_OPEN_DOCUMENT;

public class Utils {
    public static boolean containsExif(final byte[] jpeg) {
        for (int i = 0; i < jpeg.length; i++) {
            // Check for presence of the Exif header, typically after SOI
            if (jpeg[i] == 0x45 && jpeg[i + 1] == 0x78 && jpeg[i + 2] == 0x69 &&
                    jpeg[i + 3] == 0x66 && jpeg[i + 4] == 0x00 && jpeg[i + 5] == 0x00) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBuildVerMinO() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean isDarkThemeSelected(@NonNull final Context context) {
        return !context.getSharedPreferences(KEY_INVERT, Context.MODE_PRIVATE)
                .getBoolean(KEY_INVERT, false);
    }

    public static boolean isIntentSupported(@Nullable final Activity activity) {
        if (activity != null) {
            final Intent intent = activity.getIntent();

            return !TextUtils.isEmpty(intent.getAction()) &&
                    intent.getAction().equals(Intent.ACTION_SEND);
        }
        return false;
    }

    public static void invertTheme(@Nullable final Activity activity) {
        if (activity != null) {
            final SharedPreferences prefs =
                    activity.getSharedPreferences(KEY_INVERT, Context.MODE_PRIVATE);

            prefs.edit()
                    .putBoolean(KEY_INVERT, !prefs.getBoolean(KEY_INVERT, false))
                    .apply();
            activity.recreate();
        }
    }

    public static void startActivityForResult(@NonNull final Fragment fragment,
                                              @NonNull final View layout,
                                              @NonNull final Intent intent) {
        final Activity activity = fragment.getActivity();

        if (activity != null) {
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                if (!TextUtils.isEmpty(intent.getAction()) &&
                        intent.getAction().equals(Intent.ACTION_OPEN_DOCUMENT)) {
                    fragment.startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT);
                } else {
                    fragment.startActivityForResult(intent, REQUEST_CODE_CREATE_DOCUMENT);
                }
            } else {
                Snackbar.make(layout, R.string.no_suitable_activity, Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
