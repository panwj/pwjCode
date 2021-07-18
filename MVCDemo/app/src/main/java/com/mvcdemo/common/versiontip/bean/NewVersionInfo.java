package com.mvcdemo.common.versiontip.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by panwenjuan on 16-9-12.
 */
public class NewVersionInfo implements Parcelable {
    private int versionCode;
    private String versionName;
    private String whatsNew;
    private int compulsoryUpgrading;//0: false; 1: true

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getWhatsNew() {
        return whatsNew;
    }

    public void setWhatsNew(String whatsNew) {
        this.whatsNew = whatsNew;
    }

    public boolean isCompulsoryUpgrading() {
        return compulsoryUpgrading == 1;
    }

    public void setCompulsoryUpgrading(int compulsoryUpgrading) {
        this.compulsoryUpgrading = compulsoryUpgrading;
    }

    public static final Creator<NewVersionInfo> CREATOR = new Creator<NewVersionInfo>() {

        public NewVersionInfo createFromParcel(Parcel in) {
            return new NewVersionInfo(in);
        }

        public NewVersionInfo[] newArray(int size) {
            return new NewVersionInfo[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(versionCode);
        dest.writeString(versionName);
        dest.writeString(whatsNew);
        dest.writeInt(compulsoryUpgrading);
    }

    public NewVersionInfo() {};

    public NewVersionInfo(Parcel in) {
        versionCode = in.readInt();
        versionName = in.readString();
        whatsNew = in.readString();
        compulsoryUpgrading = in.readInt();
    }

    @Override
    public String toString() {
        return "NewVersionInfo{" +
                "versionCode=" + versionCode +
                ", versionName='" + versionName + '\'' +
                ", whatsNew='" + whatsNew + '\'' +
                ", compulsoryUpgrading='" + compulsoryUpgrading + '\'' +
                '}';
    }
}
