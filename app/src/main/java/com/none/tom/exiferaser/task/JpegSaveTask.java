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

package com.none.tom.exiferaser.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.io.FileOutputStream;
import java.io.IOException;

@SuppressLint("StaticFieldLeak")
public class JpegSaveTask extends AsyncTask<Void, Void, Boolean> {
    private final int mQuality;

    private final Bitmap mBitmap;
    private final Callback mCallback;
    private Context mContext;
    private final Uri mUri;

    public JpegSaveTask(@NonNull final Fragment fragment, @NonNull final Bitmap bitmap,
                        @NonNull final Uri uri, final int quality) {
        mCallback = (Callback) fragment;
        mBitmap = bitmap;
        mUri = uri;
        mQuality = quality;
        if (fragment.getContext() != null) {
            mContext = fragment.getContext().getApplicationContext();
        }
    }

    @NonNull
    @Override
    protected Boolean doInBackground(@NonNull final Void... unused) {
        ParcelFileDescriptor pfd = null;
        FileOutputStream fos = null;

        if (mContext != null) {
            try {
                pfd = mContext.getContentResolver().openFileDescriptor(mUri, "w");
                if (pfd != null) {
                    fos = new FileOutputStream(pfd.getFileDescriptor());
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, mQuality, fos);

                    return true;
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                if (pfd != null && fos != null) {
                    try {
                        fos.close();
                        pfd.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(@NonNull final Boolean saved) {
        mCallback.onJpegSaveResult(saved);
    }

    public interface Callback {
        void onJpegSaveResult(final boolean saved);
    }
}
