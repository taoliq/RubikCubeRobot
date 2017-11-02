package com.example.logqtainia.March;

import android.graphics.Color;

/**
 * Created by tarjan on 17-1-31.
 */

public class FaceColor {
    //H: 0..360, S: 0..255, V: 0..255
    private float H, S, V;
    //mark the position
    private int id;
    //RGB color
    private int color;

    public FaceColor(float[] hsv, int id) {
        this.H = hsv[0];
        this.S = hsv[1];
        this.V = hsv[2];
        this.id = id;
        this.color = Color.HSVToColor(hsv) | (0xFF << 24);
    }

    public float getH() {
        return H;
    }

    public float getS() {
        return S;
    }

    public float getV() {
        return V;
    }

    public int getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    //self-defined
    private static final double R = 255;
    private static final double angle = 30;
    private static final double h = 255;
    private static final double r = R * Math.sin(angle / 180 * Math.PI);

    public static float distanceOf(FaceColor face1, FaceColor face2) {
        double x1 = R * face1.getV() * face1.getS() * Math.cos(face1.getH() / 180 * Math.PI);
        double y1 = R * face1.getV() * face1.getS() * Math.sin(face1.getH() / 180 * Math.PI);
        double z1 = h * (1 - face1.getV());
        double x2 = R * face2.getV() * face2.getS() * Math.cos(face2.getH() / 180 * Math.PI);
        double y2 = R * face2.getV() * face2.getS() * Math.sin(face2.getH() / 180 * Math.PI);
        double z2 = h * (1 - face2.getV());
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
