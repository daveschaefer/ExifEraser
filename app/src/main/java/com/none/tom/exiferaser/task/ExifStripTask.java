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
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.none.tom.exiferaser.util.Utils;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@SuppressLint("StaticFieldLeak")
public class ExifStripTask extends AsyncTask<Void, Void, ExifStripTask.Result> {
    private static final String TAG = ExifStripTask.class.getSimpleName();

    private static final int BUF_SIZE = 16384;

    private final Callback mCallback;
    private Context mContext;
    private final Uri mUri;

    public ExifStripTask(@NonNull final Fragment fragment, @NonNull final Uri uri) {
        mCallback = (Callback) fragment;
        mUri = uri;
        if (fragment.getContext() != null) {
            mContext = fragment.getContext().getApplicationContext();
        }
    }

    @NonNull
    @Override
    protected Result doInBackground(@NonNull final Void... unused) {
        boolean containsExif = true;
        InputStream in = null;
        Bitmap bitmap = null;
        String displayName = null;

        if (mContext != null) {
            try {
                final ContentResolver resolver = mContext.getContentResolver();
                final String[] projection = { DocumentsContract.Document.COLUMN_DISPLAY_NAME };
                final Cursor cursor = resolver.query(mUri, projection, null, null, null);

                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        displayName = cursor.getString(cursor.getPosition());
                    }
                    cursor.close();
                }

                in = resolver.openInputStream(mUri);
                if (in != null) {
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    final byte[] buf = new byte[BUF_SIZE];

                    for (int read; (read = in.read(buf, 0, BUF_SIZE)) > 0; ) {
                        out.write(buf, 0, read);
                    }

                    byte[] jpeg = out.toByteArray();
                    out.reset();

                    if (Utils.containsExif(jpeg)) {
                        new ExifRewriter().removeExifMetadata(jpeg, out);

                        final byte[] jpegStripped = out.toByteArray();
                        final int strippedSize = out.size();

                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;

                        BitmapFactory.decodeByteArray(jpegStripped, 0, strippedSize, options);

                        long bitmapBytes = options.outWidth * options.outWidth;
                        boolean willFitInMem = false;

                        if (Utils.isBuildVerMinO()) {
                            final ActivityManager.MemoryInfo inf = new ActivityManager.MemoryInfo();
                            final ActivityManager manager = (ActivityManager) mContext
                                    .getSystemService(Context.ACTIVITY_SERVICE);

                            if (manager != null) {
                                manager.getMemoryInfo(inf);

                                bitmapBytes *= getBytesPerPixel(options.outConfig);
                                willFitInMem = inf.totalMem / 8 > bitmapBytes;
                            }
                        } else {
                            bitmapBytes *= getBytesPerPixel(Bitmap.Config.ARGB_8888);
                            willFitInMem = Runtime.getRuntime().maxMemory() / 2 > bitmapBytes;
                        }

                        if (willFitInMem) {
                            bitmap = BitmapFactory.decodeByteArray(jpegStripped, 0, strippedSize);
                        } else {
                            Log.w(TAG, "bitmap decode exceeds memory size limit");
                        }
                    } else {
                        containsExif = false;
                    }
                }
            } catch (final IOException | ImageReadException | ImageWriteException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return new Result(bitmap, displayName, containsExif);
    }

    @Override
    protected void onPostExecute(@NonNull final Result result) {
        mCallback.onExifStripResult(result);
    }

    private int getBytesPerPixel(@NonNull final Bitmap.Config config) {
        if (Utils.isBuildVerMinO() && config == Bitmap.Config.RGBA_F16) {
            return 8;
        } else if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565 || config == Bitmap.Config.ARGB_4444) {
            return 2;
        }
        return 1;
    }

    public static class Result {
        private final boolean mContainsExif;

        private final Bitmap mBitmap;
        private final String mDisplayName;

        Result(@Nullable final Bitmap bitmap, @Nullable final String displayName,
               final boolean containsExif) {
            mBitmap = bitmap;
            mDisplayName = displayName;
            mContainsExif = containsExif;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public String getDisplayName() {
            return mDisplayName;
        }

        public boolean containsExif() {
            return mContainsExif;
        }
    }

    public interface Callback {
        void onExifStripResult(@NonNull final Result result);
    }
}
