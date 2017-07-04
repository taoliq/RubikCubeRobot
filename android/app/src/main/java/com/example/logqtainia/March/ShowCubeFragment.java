package com.example.logqtainia.March;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import cs.min2phase.Search;

/**
 * Created by tarjan on 17-3-11.
 */

public class ShowCubeFragment extends Fragment {
    public static final int SIZE = 3;
    public static final String FACES_ORDER = "FRBLUD";

    private String cubeString = "UUUUUUUUU" + "RRRRRRRRR" + "FFFFFFFFF"
            + "DDDDDDDDD" + "LLLLLLLLL" + "BBBBBBBBB";
    private String result;
    private String[] colorName = new String[]{"yellow", "red", "blue", "white", "orange", "green"};
    private Search search = new Search();
    private int maxDepth = 21;
    private int mask = 0;

    TextView tv;
    TextView[] cubePieceTextView = new TextView[54];
    Button btnLoadCube;
    Button btnOpenBT;
    Button btnSolveCube;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_cube, container, false);

        tv = (TextView) view.findViewById(R.id.textView2);
        btnLoadCube = (Button) view.findViewById(R.id.btn_load_cube);
        btnLoadCube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.container, new LoadCubeFragment())
                        .commit();
            }
        });

        btnOpenBT = (Button) view.findViewById(R.id.btn_open_blue_tooth);
        if (((MainActivity) getActivity()).getBTHelper() != null)
            btnOpenBT.setEnabled(
                    !(((MainActivity) getActivity()).getBTHelper().getConnected()));
        btnOpenBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).connectToBT();
            }
        });

        btnSolveCube = (Button) view.findViewById(R.id.btn_solve_cube);
        btnSolveCube.setEnabled(false);
        if (((MainActivity) getActivity()).getBTHelper() != null)
            btnSolveCube.setEnabled(
                    ((MainActivity) getActivity()).getBTHelper().getConnected());
        btnSolveCube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String operation = tv.getText().toString();
                ((MainActivity) getActivity()).getBTHelper().send(operation.getBytes());
                Log.i("Solve Result", operation);
            }
        });

        initDrawCube(view);
        drawCube();

        new Thread() {
            @Override
            public void run() {
                //Error 8 probeMax 100 -> 1000
                result = search.solution(cubeString, maxDepth, 1000, 0, mask);
                tv.post(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(result);
                    }
                });
            }
        }.start();

        return view;
    }

    private void initDrawCube(View view) {
        int s = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                22, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(s, s);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                2, getResources().getDisplayMetrics());
        lp.setMargins(margin, margin, margin, margin);

        for (int i = 0; i < 6; i++) {
            Resources res = getResources();
            String idName = "cubeFace" + String.valueOf(i);
            int id = res.getIdentifier(idName, "id", getActivity().getPackageName());
            LinearLayout cf = (LinearLayout) view.findViewById(id);

            for (int j = 0; j < SIZE; j++) {
                LinearLayout l = new LinearLayout(view.getContext());
                l.setOrientation(LinearLayout.HORIZONTAL);
                for (int k = 0; k < SIZE; k++) {
                    TextView tv = new TextView(view.getContext());
                    tv.setLayoutParams(lp);
//                    tv.setBackgroundColor(ColorDetector.nameToRGB(colorName[i]));
//                    tv.setBackgroundResource(R.drawable.border);
                    l.addView(tv);
                    cubePieceTextView[i * 9 + j * 3 + k] = tv;
                }
                cf.addView(l);
            }
        }
    }

    private void drawCube() {
        colorName = ((MainActivity) getActivity()).getColorName();
        cubeString = ((MainActivity) getActivity()).getCubeString();

        for (int i = 0; i < 54; i++) {
//            Log.i("drawCube", cubeString.charAt(i) + "");
//            Log.i("drawCube", FACES_ORDER.indexOf(cubeString.charAt(i)) + "");
            cubePieceTextView[i].setBackgroundColor(
                    ColorDetector.nameToRGB(
                            colorName[FACES_ORDER.indexOf(cubeString.charAt(i))]));
        }
    }
}
