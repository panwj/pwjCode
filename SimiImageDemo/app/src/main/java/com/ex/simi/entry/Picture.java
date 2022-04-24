package com.ex.simi.entry;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.opencv.core.Mat;

@Entity(tableName = "picture_table")
public class Picture implements Parcelable {

    @PrimaryKey
    public long id;
    @ColumnInfo
    public String path;
    @ColumnInfo
    public String name;
    @ColumnInfo
    public String mimetype;
    @ColumnInfo
    public long size;
    @ColumnInfo
    public long takeDate;
    @ColumnInfo
    public long a_finger;
    @ColumnInfo
    public long p_finger;
    @ColumnInfo
    public long d_finger;
    @Ignore
    public Mat mat;
    @Ignore
    public int type = -1;
    @Ignore
    public boolean isUse;
    @Ignore
    public Mat[] mats;

    public Picture() {

    }

    public static final Creator<Picture> CREATOR = new Creator<Picture>() {
        @Override
        public Picture createFromParcel(Parcel source) {
            return new Picture(source);
        }

        @Override
        public Picture[] newArray(int size) {
            return new Picture[size];
        }
    };

    protected Picture(Parcel in) {
        this.id = in.readLong();
        this.path = in.readString();
        this.name = in.readString();
        this.mimetype = in.readString();
        this.size = in.readLong();
        this.takeDate = in.readLong();
        this.a_finger = in.readLong();
        this.p_finger = in.readLong();
        this.d_finger = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.path);
        dest.writeString(this.name);
        dest.writeString(this.mimetype);
        dest.writeLong(this.size);
        dest.writeLong(this.takeDate);
        dest.writeLong(this.a_finger);
        dest.writeLong(this.p_finger);
        dest.writeLong(this.d_finger);
    }
}
