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
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.none.tom.exiferaser.R;
import com.none.tom.exiferaser.activity.MainActivity;
import com.none.tom.exiferaser.task.ExifStripTask;
import com.none.tom.exiferaser.task.JpegSaveTask;
import com.none.tom.exiferaser.util.Utils;

import static com.none.tom.exiferaser.util.Constants.MIME_TYPE_JPEG;
import static com.none.tom.exiferaser.util.Constants.QUALITY_DEFAULT;
import static com.none.tom.exiferaser.util.Constants.REQUEST_CODE_CREATE_DOCUMENT;
import static com.none.tom.exiferaser.util.Constants.REQUEST_CODE_OPEN_DOCUMENT;

public class MainFragment extends Fragment implements ExifStripTask.Callback,
                                                      JpegSaveTask.Callback,
                                                      QualityFragment.Callback {
    private AsyncTask<Void, Void, ?> mAsyncTask;
    private Bitmap mBitmap;

    private CoordinatorLayout mLayout;
    private TextView mDescription;
    private ProgressBar mSpinner;

    private int mQuality;
    private Uri mUri;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        mQuality = QUALITY_DEFAULT;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        mLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_main, container, false);
        mDescription = mLayout.findViewById(R.id.Description);
        mSpinner = mLayout.findViewById(R.id.Spinner);

        mLayout.findViewById(R.id.InsertPictureFab).setOnClickListener(view -> {
            if (!isTaskRunning()) {
                startActivityForResult(Intent.ACTION_OPEN_DOCUMENT, null);
            }
        });
        return mLayout;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && isTaskRunning()) {
            setTaskLoading(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu,
                                    @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (isTaskRunning()) {
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId()) {
            case R.id.action_quality:
                if (getFragmentManager() != null) {
                    QualityFragment
                            .getInstance(mQuality)
                            .show(getFragmentManager(), QualityFragment.TAG);
                }
                return true;
            case R.id.action_invert:
                Utils.invertTheme(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 @NonNull final Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (resultCode == Activity.RESULT_OK) {
            mUri = resultData.getData();

            if (mUri != null) {
                if (requestCode == REQUEST_CODE_OPEN_DOCUMENT) {
                    mAsyncTask = new ExifStripTask(this, mUri);
                    mAsyncTask.execute();

                    setTaskLoading(true);
                    setIntentHandled(true);
                } else if (requestCode == REQUEST_CODE_CREATE_DOCUMENT) {
                    mSpinner.setVisibility(View.VISIBLE);
                    mAsyncTask = new JpegSaveTask(this, mBitmap, mUri, mQuality);
                    mAsyncTask.execute();
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            setIntentHandled(Utils.isIntentSupported(getActivity()));
            setTaskLoading(false);
        }
    }

    @Override
    public void onQualityChanged(final int newQuality) {
        mQuality = newQuality;
    }

    @Override
    public void onExifStripResult(@NonNull final ExifStripTask.Result result) {
        if (result.getBitmap() != null) {
            mBitmap = result.getBitmap();
            mSpinner.setVisibility(View.GONE);

            startActivityForResult(Intent.ACTION_CREATE_DOCUMENT, result.getDisplayName());
        } else {
            setIntentHandled(false);
            setTaskLoading(false);

            Snackbar.make(mLayout, result.containsExif() ?
                    R.string.exif_strip_failed :
                    R.string.exif_already_stripped, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onJpegSaveResult(final boolean saved) {
        setIntentHandled(false);
        setTaskLoading(false);

        Snackbar.make(mLayout, saved ?
                R.string.picture_saved :
                R.string.picture_save_failed, Snackbar.LENGTH_LONG)
                .show();
    }

    @SuppressWarnings("unchecked")
    public void handleSupportedIntent(@NonNull final Intent intent) {
        final ClipData clipData = intent.getClipData();

        if (clipData != null && clipData.getItemCount() > 0) {
            mUri = clipData.getItemAt(0).getUri();

            mAsyncTask = new ExifStripTask(this, mUri);
            mAsyncTask.execute();

            setTaskLoading(true);
            setIntentHandled(true);
        }
    }

    private void setIntentHandled(final boolean handled) {
        final MainActivity activity = (MainActivity) getActivity();

        if (activity != null) {
            activity.setIntentHandled(handled);
        }
    }

    private boolean isTaskRunning() {
        return mAsyncTask != null && mAsyncTask.getStatus() == AsyncTask.Status.RUNNING;
    }

    private void setTaskLoading(final boolean loading) {
        if (loading) {
            mDescription.setVisibility(View.GONE);
            mSpinner.setVisibility(View.VISIBLE);
        } else {
            mSpinner.setVisibility(View.GONE);
            mDescription.setVisibility(View.VISIBLE);
        }
    }

    private void startActivityForResult(@NonNull final String action,
                                        @Nullable final String displayName) {
        Utils.startActivityForResult(this, mLayout, new Intent()
                .setType(MIME_TYPE_JPEG)
                .setAction(action)
                .putExtra(Intent.EXTRA_TITLE, (!TextUtils.isEmpty(displayName) ?
                        displayName.substring(0, displayName.length() - 4) : "") +
                        '_' +
                        getString(R.string.stripped)));
    }
}
