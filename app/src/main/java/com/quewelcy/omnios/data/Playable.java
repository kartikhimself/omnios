package com.quewelcy.omnios.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Playable implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Playable createFromParcel(Parcel in) {
            return new Playable(in);
        }

        public Playable[] newArray(int size) {
            return new Playable[size];
        }
    };
    private String title;
    private String path;
    private long position;

    public Playable() {
        title = "";
        path = "";
        position = 0;
    }

    public Playable(String title, String path, long position) {
        this.title = title;
        this.path = path;
        this.position = position;
    }

    private Playable(Parcel in) {
        title = in.readString();
        path = in.readString();
        position = in.readInt();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public boolean isDefault() {
        return path.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return "Playable [" + title + ", " + path + ", " + position + "]";
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Playable && ((Playable) o).path.equals(path);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(path);
        dest.writeLong(position);
    }
}