package com.ex.simi.entry;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity(tableName = "similar_photo_table")
public class PhotoEntity implements Comparable<PhotoEntity>, Parcelable {

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
    public long time;//秒
    @ColumnInfo
    public long takeDate;//毫秒
    @ColumnInfo
    public long a_finger;
    @ColumnInfo
    public long p_finger;
    @ColumnInfo
    public long d_finger;
    @ColumnInfo
    public int orientation;
    @Ignore
    public int groupId;
    @Ignore
    public boolean isUse;
    @Ignore
    public boolean isBestPhoto;
    @Ignore
    public boolean isChecked;


    public PhotoEntity() {}

    public static final Creator<PhotoEntity> CREATOR = new Creator<PhotoEntity>() {
        @Override
        public PhotoEntity createFromParcel(Parcel source) {
            return new PhotoEntity(source);
        }

        @Override
        public PhotoEntity[] newArray(int size) {
            return new PhotoEntity[size];
        }
    };

    protected PhotoEntity(Parcel in) {
        this.id = in.readLong();
        this.path = in.readString();
        this.name = in.readString();
        this.mimetype = in.readString();
        this.size = in.readLong();
        this.time = in.readLong();
        this.takeDate = in.readLong();
        this.a_finger = in.readLong();
        this.p_finger = in.readLong();
        this.d_finger = in.readLong();
        this.orientation = in.readInt();
        this.isChecked = in.readByte() != 0;
        this.isBestPhoto = in.readByte() != 0;
        this.groupId = in.readInt();
        this.isUse = in.readByte() != 0;
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
        dest.writeLong(this.time);
        dest.writeLong(this.takeDate);
        dest.writeLong(this.a_finger);
        dest.writeLong(this.p_finger);
        dest.writeLong(this.d_finger);
        dest.writeInt(this.orientation);
        dest.writeByte((byte) (isChecked ? 1 : 0));
        dest.writeByte((byte) (isBestPhoto ? 1 : 0));
        dest.writeInt(groupId);
        dest.writeByte((byte) (isUse ? 1 : 0));
    }

    @Override
    public int compareTo(PhotoEntity o) {
        if (this.time - o.time > 0) {
            return -1;
        } else if (this.time - o.time < 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj instanceof PhotoEntity) {
            PhotoEntity info = (PhotoEntity) obj;
            return id == info.id;
        }
        return false;
    }
}
