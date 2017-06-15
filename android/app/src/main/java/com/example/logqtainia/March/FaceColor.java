package com.example.logqtainia.March;

/**
 * Created by tarjan on 17-1-31.
 */

public class FaceColor {
    //h: 0..360, s: 0..255, v: 0..255
    private int h, s, v;
    //mark the position
    private int id;

    public FaceColor(float[] hsv, int id) {
        this.h = (int)hsv[0];
        this.s = (int)hsv[1];
        this.v = (int)hsv[2];
        this.id = id;
    }

    public int getH() {
        return h;
    }

    public int getS() {
        return s;
    }

    public int getV() {
        return v;
    }

    public int getId() {
        return id;
    }
}
