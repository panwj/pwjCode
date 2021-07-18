package com.mvcdemo.common.google.billing;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by panwenjuan on 17-8-1.
 */
public class SubItem implements Parcelable {

    public int drawableId;
    public int iconId;
    public int strId;

    public SubItem() {
    }

    ;

    public SubItem(Parcel in) {
        drawableId = in.readInt();
        iconId = in.readInt();
        strId = in.readInt();
    }

    public static final Creator<SubItem> CREATOR = new Creator<SubItem>() {

        public SubItem createFromParcel(Parcel in) {
            return new SubItem(in);
        }

        public SubItem[] newArray(int size) {
            return new SubItem[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(drawableId);
        parcel.writeInt(iconId);
        parcel.writeInt(strId);
    }

    @Override
    public String toString() {
        return "Video{" +
                "drawableId = " + drawableId + "\'" +
                "iconId = " + iconId + "\'" +
                "strId = " + strId + "\'" +
                "}";
    }
}
