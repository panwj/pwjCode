package com.file.share;

import androidx.annotation.NonNull;

public class Endpoint {
    @NonNull
    private String id;
    @NonNull
    private String name;
    private ConnectionState state = ConnectionState.UNKNOWN;

    public Endpoint(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setState(ConnectionState state) {
        this.state = state;
    }

    public ConnectionState getState() {
        return state;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Endpoint) {
            Endpoint other = (Endpoint) obj;
            return id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Endpoint{id=%s, name=%s}", id, name);
    }
}
