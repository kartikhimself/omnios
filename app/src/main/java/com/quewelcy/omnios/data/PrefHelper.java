package com.quewelcy.omnios.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.google.gson.Gson;
import com.quewelcy.omnios.Configures;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.quewelcy.omnios.Configures.getTitle;

public class PrefHelper {

    private static final String PREFS_FILE_NAME = "omnios_prefs";
    private static final Gson GSON = new Gson();

    private static QPrefs qPrefs;

    private static void validatePrefs(Context c) {
        if (qPrefs == null) {
            qPrefs = readPrefs(c);
        }
    }

    private static void savePrefs(Context c) {
        if (qPrefs == null) {
            return;
        }
        String json = GSON.toJson(qPrefs, QPrefs.class);
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(PREFS_FILE_NAME, json);
        edit.apply();
    }

    private static QPrefs readPrefs(Context c) {
        QPrefs qp = null;
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        try {
            String string = sharedPreferences.getString(PREFS_FILE_NAME, null);
            qp = GSON.fromJson(string, QPrefs.class);
        } catch (Exception e) {
            //ignore
        }
        if (qp == null) {
            qp = new QPrefs();
        }
        return qp;
    }

    public static long getVideoPosition(Context c, String path) {
        validatePrefs(c);
        Playable videoPlayable = qPrefs.getVideoPosition();
        if (videoPlayable.getPath().equals(path)) {
            return videoPlayable.getPosition();
        }
        return 0L;
    }

    public static void setVideoPosition(Context c, String path, long position) {
        validatePrefs(c);
        qPrefs.setVideoPosition(new Playable(getTitle(path), path, position));
        savePrefs(c);
    }

    public static Playable getCurrentPlayable(Context c) {
        validatePrefs(c);
        if (qPrefs.getCurrentPlayable() == null) {
            File file = Environment.getExternalStorageDirectory();
            qPrefs.setCurrentPlayable(new Playable(file.getName(), file.getPath(), 0));
        }
        return qPrefs.getCurrentPlayable();
    }

    public static void setCurrentPlayable(Context c, Playable playable) {
        if (playable == null) {
            return;
        }
        qPrefs.setCurrentPlayable(playable);
        savePrefs(c);
    }

    public static void setAudioPosition(Context c, String path, int position) {
        validatePrefs(c);
        Playable playable = new Playable(getTitle(path), path, position);
        qPrefs.setAudioPosition(playable);
        checkAndUpdatePermanentPath(c, playable);
        savePrefs(c);
    }

    public static long getAudioPosition(Context c, String path) {
        validatePrefs(c);
        Playable audioPlayable = qPrefs.getAudioPosition();
        if (audioPlayable.getPath().equals(path)) {
            return audioPlayable.getPosition();
        }
        return findInPermanentPaths(c, path);
    }

    public static void checkAndContinueAudioPosition(Context c, Playable currentPlayable, Playable nextPlayable) {
        validatePrefs(c);
        Playable p = qPrefs.getPerm(currentPlayable.getPath());
        if (p == null) {
            return;
        }
        removeFromPerms(c, currentPlayable);
        addPathToPerms(c, nextPlayable);
    }

    private static void checkAndUpdatePermanentPath(Context c, Playable newPlayable) {
        Playable p = getPerm(c, newPlayable.getPath());
        if (p == null) {
            return;
        }
        addPathToPerms(c, newPlayable);
        savePrefs(c);
    }

    private static long findInPermanentPaths(Context c, String path) {
        Playable p = getPerm(c, path);
        if (p == null) {
            return 0;
        }
        return p.getPosition();
    }

    public static void toggleCurrentPathToPerms(Context c) {
        Playable p = getCurrentPlayable(c);
        if (getPerms(c).containsKey(p.getPath())) {
            removeFromPerms(c, p);
        } else {
            addPathToPerms(c, p);
        }
        savePrefs(c);
    }

    private static void addPathToPerms(Context c, Playable playable) {
        if (Configures.isMusic(playable.getPath()) && !playable.isDefault()) {
            getPerms(c).put(playable.getPath(), playable);
        }
    }

    public static void removeFromPerms(Context c, Playable playable) {
        validatePrefs(c);
        getPerms(c).remove(playable.getPath());
        savePrefs(c);
    }

    public static void clearPerms(Context c) {
        validatePrefs(c);
        qPrefs.setPerms(new HashMap<>());
        savePrefs(c);
    }

    public static Map<String, Playable> getPerms(Context c) {
        validatePrefs(c);
        return qPrefs.getPerms();
    }

    private static Playable getPerm(Context c, String path) {
        validatePrefs(c);
        return qPrefs.getPerm(path);
    }

    public static boolean isPersisted(Context c, String path) {
        return !(c == null || path == null) && getPerm(c, path) != null;
    }

    private static class QPrefs {

        private Map<String, Playable> perms = new HashMap<>();
        private Playable currentPlayable;
        private Playable audioPosition;
        private Playable videoPosition;

        Playable getPerm(String path) {
            return perms.get(path);
        }

        Map<String, Playable> getPerms() {
            return perms;
        }

        void setPerms(Map<String, Playable> perms) {
            this.perms = perms;
        }

        Playable getCurrentPlayable() {
            return currentPlayable;
        }

        void setCurrentPlayable(Playable currentPlayable) {
            this.currentPlayable = currentPlayable;
        }

        Playable getAudioPosition() {
            if (audioPosition == null) {
                audioPosition = new Playable();
            }
            return audioPosition;
        }

        void setAudioPosition(Playable audioPosition) {
            this.audioPosition = audioPosition;
        }

        Playable getVideoPosition() {
            if (videoPosition == null) {
                videoPosition = new Playable();
            }
            return videoPosition;
        }

        void setVideoPosition(Playable videoPosition) {
            this.videoPosition = videoPosition;
        }
    }
}