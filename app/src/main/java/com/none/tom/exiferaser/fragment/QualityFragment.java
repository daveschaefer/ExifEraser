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

package com.none.tom.exiferaser.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.none.tom.exiferaser.R;
import com.none.tom.exiferaser.activity.MainActivity;

import static com.none.tom.exiferaser.util.Constants.KEY_QUALITY;
import static com.none.tom.exiferaser.util.Constants.QUALITY_DEFAULT;

public class QualityFragment extends DialogFragment {
    static final String TAG = QualityFragment.class.getSimpleName();

    private static QualityFragment sQualityFragment;

    private Callback mCallback;
    private int mQuality;

    interface Callback {
        void onQualityChanged(final int newQuality);
    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);

        mCallback = ((MainActivity) context).getMainFragment();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mQuality = getArguments().getInt(KEY_QUALITY);
        } else {
            mQuality = QUALITY_DEFAULT;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final View view = View.inflate(activity, R.layout.fragment_quality, null);
        final SeekBar seekBar = view.findViewById(R.id.SeekBar);
        final TextView qualityView = view.findViewById(R.id.TextView);

        seekBar.setProgress(mQuality);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int value,
                                          final boolean b) {
                final String quality = String.valueOf(roundtoMultipleOf5(value));

                if (!qualityView.getText().equals(quality)) {
                    qualityView.setText(quality);
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {}
        });

        qualityView.setText(String.valueOf(mQuality));

        return new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) ->
                        mCallback.onQualityChanged(roundtoMultipleOf5(seekBar.getProgress())))
                .create();
    }

    @NonNull
    static QualityFragment getInstance(final int quality) {
        if (sQualityFragment == null) {
            sQualityFragment = new QualityFragment();
            final Bundle args = new Bundle();

            args.putInt(KEY_QUALITY, quality);
            sQualityFragment.setArguments(args);
        } else {
            final Bundle args = sQualityFragment.getArguments();
            if (args != null) {
                args.clear();
                args.putInt(KEY_QUALITY, quality);
            }
        }
        return sQualityFragment;
    }

    private int roundtoMultipleOf5(final float value) {
        return 5 * Math.round((value / 5f));
    }
}
