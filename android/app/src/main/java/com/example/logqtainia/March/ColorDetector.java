package com.example.logqtainia.March;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.FaceDetector;
import android.util.Log;

import java.util.Comparator;

import cs.min2phase.Search;

import static java.util.Arrays.sort;

/**
 * Created by tarjan on 17-1-30.
 */

public class ColorDetector {
    public static float[] averageColor(Bitmap bm) {
        int w = bm.getWidth();
        int h = bm.getHeight();
        int s = w * h;
        double r = 0;
        double g = 0;
        double b = 0;
        float[] hsv = new float[3];
        int[] pixels = new int[s];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int c = pixels[i * w + j];
                int pr = Color.red(c);
                int pg = Color.green(c);
                int pb = Color.blue(c);
                r += pr * pr;
                g += pg * pg;
                b += pb * pb;
            }
        }
        r = Math.sqrt(r / (double) s);
        g = Math.sqrt(g / (double) s);
        b = Math.sqrt(b / (double) s);
        Color.RGBToHSV((int) r, (int) g, (int) b, hsv);
//        hsv[0] /= 2;  //提高精度
//        hsv[1] *= 255;
//        hsv[2] *= 255;
        return hsv;
    }

    public static String getColorName(float[] hsv) {
//        float[] hsv = new float[3];
//        Color.RGBToHSV(color[0], color[1], color[2], hsv);
        //convert value range, different from JAVA to python
//        double h = hsv[0];
//        double s = hsv[1];
//        double v = hsv[2];
        int h = (int) hsv[0];
        int s = (int) hsv[1];
        int v = (int) hsv[2];

        if ((h < 15 || h > 150) && s > 40)
            return "red";
        else if (h <= 10 && v > 100)
            return "orange";
        else if (h <= 30 && s <= 100)
            return "white";
        else if (h <= 40)
            return "yellow";
        else if (h <= 85)
            return "green";
        else if (h <= 130)
            return "blue";

        return "white";
    }

    public static StringBuilder[] getColorName2(FaceColor[][] capturedFaces) {
        FaceColor[] faceColor = new FaceColor[63];
        StringBuilder[][] cubeState = new StringBuilder[6][9];
        //前六个元素表示对应面的颜色，最后一个元素表示魔方的状态，如FFFFFFFFFRRRRRRBBBBBBLLLLLLUUUUUUDDDDDD
        StringBuilder[] result = new StringBuilder[7];

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                faceColor[i * 9 + j] = capturedFaces[i][j];
            }
        }

        float minDis = 960728;
        for (int i = 0; i < 45; i++) {
            if (i % 9 == 0) {
                continue;
            }
            minDis = 960728;
            for (int j = i; j < 54; j++) {
                float tmpDis = FaceColor.distanceOf(faceColor[i / 9 * 9], faceColor[j]);
                if (tmpDis < minDis) {
                    minDis = tmpDis;
                    FaceColor tmp = faceColor[i];
                    faceColor[i] = faceColor[j];
                    faceColor[j] = tmp;
                }
            }
        }
//        result[0] = new StringBuilder(faceColor[0].getColor() + "");
//        result[1] = new StringBuilder(faceColor[9].getColor() + "");
//        result[2] = new StringBuilder(faceColor[18].getColor() + "");
//        result[3] = new StringBuilder(faceColor[27].getColor() + "");
//        result[4] = new StringBuilder(faceColor[36].getColor() + "");
//        result[5] = new StringBuilder(faceColor[45].getColor() + "");
        result[6] = new StringBuilder();
        for (int i = 0; i < 54; i++) result[6].append(' ');

        for (int i = 0; i < 6; i++) {
            char notation = ' ';
            for (int j = 0; j < 9; j++) {
                if (faceColor[i*9 + j].getId() % 10 == 4) {
                    int face = faceColor[i*9 + j].getId() / 10;
                    notation = LoadCubeFragment.FACES_ORDER.charAt(face);
                    result[MainActivity.FACES_ORDER.indexOf(notation)] =
                            new StringBuilder(faceColor[i*9 + j].getColor() + "");
                }
            }
            for (int j = 0; j < 9; j++) {
                int id = faceColor[i*9 + j].getId();
                Log.i("getColorName2", id + " ");
                int changedFacePos = Search.FACES_ORDER.indexOf(
                        LoadCubeFragment.FACES_ORDER.charAt(id/10));
                result[6].setCharAt(changedFacePos * 9 + (id%10), notation);
            }
            Log.i("getColorName2", " ");
        }

        Log.i("ColorDetector", result[6].toString());

        return result;
    }

    public static StringBuilder[] getColorName(FaceColor[][] capturedFaces) {
        FaceColor[] faceColor = new FaceColor[63];
        StringBuilder[][] cubeState = new StringBuilder[6][9];
        String notation = "";
        //前六个元素表示对应面的颜色，最后一个元素表示魔方的状态，如FFFFFFFFFRRRRRRBBBBBBLLLLLLUUUUUUDDDDDD
        StringBuilder[] result = new StringBuilder[7];

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                faceColor[i * 9 + j] = capturedFaces[i][j];
            }
        }

        //分离白色，ｓ值最小的六个
        sort(faceColor, 0, 54, new Comparator<FaceColor>() {
            @Override
            public int compare(FaceColor o1, FaceColor o2) {
                return (int)o1.getS() - (int)o2.getS();
            }
        });
        //先找到颜色对应的方向符号
        setColorNotation(0, "white", faceColor, cubeState, result);

        //根据h值分离剩下的颜色
        sort(faceColor, 9, 54, new Comparator<FaceColor>() {
            @Override
            public int compare(FaceColor o1, FaceColor o2) {
                return (int)o1.getH() - (int)o2.getH();
            }
        });
        //将h值最小的9个复制一遍到最后，解决红色h值范围问题
        for (int i = 0; i < 9; i++) faceColor[54 + i] = faceColor[9 + i];
        //h最大的六个为红色，红色和蓝色h值差100以上，如果红色不够6个，从最小的补齐
        int j = 54 - 1;
        while ((360 - faceColor[j].getH() < 50) && (54 - j <= 9)) {
            j--;
        }
        j++;
//        Log.i("color", j + "");
        setColorNotation(j, "red", faceColor, cubeState, result);
        j -= 9;
//        Log.i("color", j + "");
        setColorNotation(j, "blue", faceColor, cubeState, result);
        j -= 9;
//        Log.i("color", j + "");
        setColorNotation(j, "green", faceColor, cubeState, result);
        j -= 9;
//        Log.i("color", j + "");
        setColorNotation(j, "yellow", faceColor, cubeState, result);
        j -= 9;
//        Log.i("color", j + "");
        setColorNotation(j, "orange", faceColor, cubeState, result);

        //调用解魔方程序中魔方面顺序为URFDLB
        result[6] = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            for (int k = 0; k < 9; k++) {
                result[6].append(cubeState[MainActivity.FACES_ORDER.indexOf("URFDLB".charAt(i))][k]);
            }
//            result[6].append("\n");
        }
//        Log.i("test", result[6].toString());
        return result;
    }

    private static void setColorNotation(int begin, String colorName, FaceColor[] faceColor,
                                         StringBuilder[][] cubeState, StringBuilder[] result) {
        String notation = "";
        //先找到颜色对应的方向符号
        for (int i = 0; i < 9; i++) {
            if (faceColor[i + begin].getId() % 10 == 4) {
                int face = faceColor[i + begin].getId() / 10;
                result[face] = new StringBuilder(colorName);
                notation = MainActivity.FACES_ORDER.charAt(face) + "";
            }
        }

        for (int i = 0; i < 9; i++) {
            int pos = faceColor[i + begin].getId();
            cubeState[pos / 10][pos % 10] = new StringBuilder(notation);
        }
    }

    public static int nameToRGB(String colorName) {
        int color;
        switch (colorName) {
            case "white":
                color = 0xFFFFFF;
                break;
            case "yellow":
                color = 0xFFFF00;
                break;
            case "red":
                color = 0xFF0000;
                break;
            case "orange":
                color = 0xFFA500;
                break;
            case "blue":
                color = 0x0000FF;
                break;
            case "green":
                color = 0x00FF00;
                break;
            default:
                color = 0;
                break;
        }
        //don't forget ALPHA
        return color | (0xFF << 24);
    }
}
