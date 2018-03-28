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

package com.none.tom.exiferaser.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.none.tom.exiferaser.fragment.MainFragment;
import com.none.tom.exiferaser.R;
import com.none.tom.exiferaser.util.Utils;

public class MainActivity extends AppCompatActivity {
    private boolean mHandled;

    private MainFragment mFragment;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.isDarkThemeSelected(this)) {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isIntentSupported()) {
            getMainFragment().handleSupportedIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
    }

    @NonNull
    public MainFragment getMainFragment() {
        if (mFragment == null) {
            mFragment = (MainFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.FragmentMain);
        }
        return mFragment;
    }

    public void setIntentHandled(final boolean handled) {
        mHandled = handled;
    }

    private boolean isIntentSupported() {
        return mHandled ? mHandled = false : Utils.isIntentSupported(this);
    }
}
