package com.mvcdemo.common.firebase.push;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by panwenjuan on 16-11-17.
 */
public class PushData implements Parcelable {

    public static String PUSH_DATA = "push_data";
    public static final String CUR_APP_PACKAGE_NAME = "screen.recorder";

    public static String DATA_IMAGE_LINK = "data_image_link";
    public static String DATA_TITLE = "data_title";
    public static String DATA_CONTENTS = "data_contents";
    public static String DATA_THEME_NAME = "data_theme_name";
    public static String DATA_PACKAGE_NAME = "data_package_name";
    public static String DATA_APP_VERSION_CODE = "data_version_code";
    public static String DATA_APP_VERSION_NAME = "data_version_name";
    public static String DATA_UPDATE_LINK = "data_update_link";

    public String mImageLink;
    public String mTitle;
    public String mContents;
    public String mThemeName;
    public String mPackageName;
    public String mVersionCode;
    public String mVersionName;
    public String mUpdateLink;

    public PushData() {

    }

    public static final Creator<PushData> CREATOR = new Creator<PushData>() {

        public PushData createFromParcel(Parcel in) {
            return new PushData(in);
        }

        public PushData[] newArray(int size) {
            return new PushData[size];
        }
    };

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mImageLink);
        out.writeString(mTitle);
        out.writeString(mContents);
        out.writeString(mThemeName);
        out.writeString(mPackageName);
        out.writeString(mUpdateLink);
        out.writeString(mVersionCode);
        out.writeString(mVersionName);
    }

    private PushData(Parcel in) {
        mImageLink = in.readString();
        mTitle = in.readString();
        mContents = in.readString();
        mThemeName = in.readString();
        mPackageName = in.readString();
        mUpdateLink = in.readString();
        mVersionCode = in.readString();
        mVersionName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "PushData{" +
                "mImageLink='" + mImageLink + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mContents='" + mContents + '\'' +
                ", mThemeName='" + mThemeName + '\'' +
                ", mPackageName='" + mPackageName + '\'' +
                ", mVersionCode='" + mVersionCode + '\'' +
                ", mVersionName='" + mVersionName + '\'' +
                ", mUpdateLink='" + mUpdateLink + '\'' +
                '}';
    }
}
