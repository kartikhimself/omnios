package com.quewelcy.omnios;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.util.Comparator;

public class Configures {

    public static final long DELAY_MILLIS = 400L;
    public static final String DOT_MP3 = ".mp3";

    private static final String DOT = ".";
    private static final String DOT_MP4 = ".mp4";
    private static final String DOT_AVI = ".avi";
    private static final String DOT_FLV = ".flv";
    private static final String DOT_WMV = ".wmv";
    private static final String DOT_M4V = ".m4v";
    private static final String DOT_3GP = ".3gp";
    private static final String DOT_MOV = ".mov";
    private static final String DOT_MKV = ".mkv";
    private static final String DOT_MPG = ".mpg";
    private static final String SEMICOLON = ":";

    public static boolean isMusic(String path) {
        return path != null && path.endsWith(DOT_MP3);
    }

    public static boolean isVideo(String path) {
        return path != null &&
                (path.endsWith(DOT_MP4) ||
                        path.endsWith(DOT_AVI) ||
                        path.endsWith(DOT_FLV) ||
                        path.endsWith(DOT_WMV) ||
                        path.endsWith(DOT_M4V) ||
                        path.endsWith(DOT_3GP) ||
                        path.endsWith(DOT_MOV) ||
                        path.endsWith(DOT_MKV) ||
                        path.endsWith(DOT_MPG));
    }

    public static String dropExtension(String title) {
        if (isMusic(title) || isVideo(title)) {
            return title.substring(0, title.lastIndexOf(DOT));
        }
        return title;
    }

    public static String getTitle(String path) {
        return path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf(DOT));
    }

    static String getRealPathFromURI(Context c, Uri contentUri) {
        if (contentUri == null) {
            return null;
        }
        String scheme = contentUri.getScheme();
        if ("file".equals(scheme)) {
            return contentUri.getPath();
        } else if ("content".equals(scheme)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return getRealPathFromContentKitkat(c, contentUri);
            } else {
                return getDataColumn(c, contentUri, null, null);
            }
        }
        return null;
    }

    @TargetApi(19)
    private static String getRealPathFromContentKitkat(Context c, Uri uri) {
        if (DocumentsContract.isDocumentUri(c, uri)) {
            String documentId = DocumentsContract.getDocumentId(uri);

            // External Storage
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                String[] split = documentId.split(":");
                if ("primary".equalsIgnoreCase(split[0])) {
                    return Environment.getExternalStorageDirectory() + File.separator + split[1];
                }
            }

            // Downloads
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                return getDataColumn(c,
                        ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                                Long.valueOf(documentId)), null, null);
            }

            // MediaProvider
            else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String[] split = documentId.split(":");
                Uri contentUri = null;
                switch (split[0]) {
                    case "image":
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "video":
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "audio":
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        break;
                }
                return getDataColumn(c, contentUri, "_id=?", new String[]{split[1]});
            }
        }
        return null;
    }

    private static String getDataColumn(Context c, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Video.Media.DATA;
        String[] projection = {column};
        try {
            cursor = c.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(column));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    public static String millisToTimeString(long millis) {
        long seconds = millis / 1000;
        StringBuilder builder = new StringBuilder();
        long h = seconds / 3600;
        if (h > 0) {
            builder.append(h).append(Configures.SEMICOLON);
            seconds %= 3600;
        }
        long m = seconds / 60;
        if (m < 10 && builder.length() > 0) {
            builder.append(0);
        }
        builder.append(m).append(Configures.SEMICOLON);
        seconds %= 60;
        if (seconds < 10) {
            builder.append(0);
        }
        builder.append(seconds);
        return builder.toString();
    }

    public enum DirFileComparator implements Comparator<File> {
        DIR_FILE_SORT {
            public int compare(File left, File right) {
                if (left.isDirectory() && right.isFile()) {
                    return -1;
                } else if (left.isFile() && right.isDirectory()) {
                    return 1;
                }
                return 0;
            }
        },
        NAME_SORT {
            public int compare(File left, File right) {
                return left.getName().compareTo(right.getName());
            }
        };

        public static Comparator<File> ascending(final Comparator<File> other) {
            return new Comparator<File>() {
                public int compare(File left, File right) {
                    return other.compare(left, right);
                }
            };
        }

        public static Comparator<File> getComparator(final DirFileComparator... comparators) {
            return new Comparator<File>() {
                public int compare(File left, File right) {
                    for (DirFileComparator comparator : comparators) {
                        int result = comparator.compare(left, right);
                        if (result != 0) {
                            return result;
                        }
                    }
                    return 0;
                }
            };
        }
    }

    public static class Actions {
        public static final String CATEGORY_BROADCAST = "com.quewelcy.omnios.CATEGORY_BROADCAST";

        public static final String INVALIDATE = "com.quewelcy.omnios.INVALIDATE";
        public static final String ERROR_PLAYING = "com.quewelcy.omnios.ERROR_PLAYING";
        public static final String PREV = "com.quewelcy.omnios.PREV";
        public static final String PLAY_STATE = "com.quewelcy.omnios.PLAY_STATE";
        public static final String NEXT = "com.quewelcy.omnios.NEXT";
    }

    public static class Extras {
        public static final String TIME_CUR = "time_cur";
        public static final String TIME_END = "time_end";
        public static final String PROGRESS = "progress";
        static final String LOCK_ON_START = "lock_on_start";
    }

    static class PermissionRequestCode {
        static final int REQ_CODE = 1;
    }
}
