package com.ex.simi.superclass;

/**
 * Created by smy on 20-11-11.
 *
 * 为了兼容BRVH 2.X版本
 */

import java.io.Serializable;

public abstract class OldSectionEntity<T> implements Serializable {
        public boolean isHeader;
        public T t;
        public String header;

        public OldSectionEntity(boolean isHeader, String header) {
            this.isHeader = isHeader;
            this.header = header;
            this.t = null;
        }

        public OldSectionEntity(T t) {
            this.isHeader = false;
            this.header = null;
            this.t = t;
        }
    }