package com.ex.simi.entry;

import org.opencv.core.Mat;

public class Picture {

    private long id;

    private String path;

    private String name;

    private String mimetype;

    private long size;

    private long a_finger;

    private long p_finger;

    private long d_finger;

    private Mat mat;

    private int type = -1;

    private boolean isUse;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getAFinger() {
        return a_finger;
    }

    public void setAFinger(long a_finger) {
        this.a_finger = a_finger;
    }

    public long getPFinger() {
        return p_finger;
    }

    public void setPFinger(long p_finger) {
        this.p_finger = p_finger;
    }

    public long getDFinger() {
        return d_finger;
    }

    public void setDFinger(long d_finger) {
        this.d_finger = d_finger;
    }

    public Mat getMat() {
        return mat;
    }

    public void setMat(Mat mat) {
        this.mat = mat;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isUse() {
        return isUse;
    }

    public void setUse(boolean use) {
        isUse = use;
    }
}
