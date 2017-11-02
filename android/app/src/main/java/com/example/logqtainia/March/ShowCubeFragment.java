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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import cs.min2phase.Search;

/**
 * Created by tarjan on 17-3-11.
 */

public class ShowCubeFragment extends Fragment implements View.OnClickListener {
//    public static final int SIZE = 3;
//    public static final String FACES_ORDER = "UDFBLR";

    private String cubeString;
    private String result;
    private String betterSolution;
    private String[] colorName;
    private Search search = new Search();
    private int maxDepth = 21;
    private int mask = 0;

    TextView tv;
    TextView[] cubePieceTextView = new TextView[54];
//    Button btnLoadCube;
//    Button btnOpenBT;
//    Button btnSolveCube;
//    Button btnManCtrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_cube, container, false);

        tv = (TextView) view.findViewById(R.id.textView2);

        Button btnLoadCube = (Button) view.findViewById(R.id.btn_load_cube);
        btnLoadCube.setOnClickListener(this);

        Button btnOpenBT = (Button) view.findViewById(R.id.btn_open_blue_tooth);
        if (((MainActivity) getActivity()).getBTHelper() != null)
            btnOpenBT.setEnabled(
                    !(((MainActivity) getActivity()).getBTHelper().getConnected()));
        btnOpenBT.setOnClickListener(this);

        Button btnManCtrl = (Button) view.findViewById(R.id.btn_manual_control);
        btnManCtrl.setEnabled(false);
        if (((MainActivity) getActivity()).getBTHelper() != null)
            btnManCtrl.setEnabled(
                    ((MainActivity) getActivity()).getBTHelper().getConnected());
        btnManCtrl.setOnClickListener(this);

        CheckBox chkAutoMode = (CheckBox) view.findViewById(R.id.chk_auto_mode);
        chkAutoMode.setEnabled(false);
        chkAutoMode.setChecked(((MainActivity) getActivity()).getAutoMode());
        if (((MainActivity) getActivity()).getBTHelper() != null)
            chkAutoMode.setEnabled(
                    ((MainActivity) getActivity()).getBTHelper().getConnected());
        chkAutoMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                ((MainActivity) getActivity()).setAutoMode(isChecked);
//                    Log.i("chkBtn", isChecked + "");
            }
        });


        Button btnSolveCube = (Button) view.findViewById(R.id.btn_solve_cube);
        btnSolveCube.setEnabled(false);
        if (((MainActivity) getActivity()).getBTHelper() != null)
            btnSolveCube.setEnabled(
                    ((MainActivity) getActivity()).getBTHelper().getConnected());
        btnSolveCube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToArduino(betterSolution);
            }
        });

        initDrawCube(view);
        drawCube();

        new Thread() {
            @Override
            public void run() {
                //Error 8 probeMax 100 -> 1000
                result = search.solution(cubeString, maxDepth, 1000, 0, mask);
                betterSolution = optimize(result);
                if (((MainActivity) getActivity()).getAutoMode()
                        && ((MainActivity) getActivity()).getBTHelper() != null) {
                    sendToArduino(betterSolution);
                }
                tv.post(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText("Solution:\n" + result);
                        tv.append("\n--------------\nOptimized:\n");
                        tv.append(betterSolution);
                    }
                });
            }
        }.start();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_load_cube:
                if (((MainActivity) getActivity()).getBTHelper() != null)
                    ((MainActivity) getActivity()).getBTHelper().send(":Init".getBytes());
                getFragmentManager()
                        .beginTransaction()
                        .hide(this)
                        .add(R.id.container, new LoadCubeFragment())
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.btn_open_blue_tooth:
                ((MainActivity) getActivity()).connectToBT();
                break;
            case R.id.btn_manual_control:
                if (((MainActivity) getActivity()).getBTHelper() != null)
                    ((MainActivity) getActivity()).getBTHelper().send(":Manual".getBytes());
                getFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .hide(this)
                        .add(R.id.container, new ManualControlFragment())
                        .commit();
                break;
            default:
                break;
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
////        Log.i("onResume_autoMode", ((MainActivity) getActivity()).getAutoMode() + "");
////        Log.i("onResume_BT", (((MainActivity) getActivity()).getBTHelper() != null) + "");
////        if (((MainActivity) getActivity()).getAutoMode()
////                && ((MainActivity) getActivity()).getBTHelper() != null) {
////            sendToArduino();
////            Log.i("onResume", "here u r");
////        }
//    }

    private void sendToArduino(String solution) {
//        result = tv.getText().toString().replace(" ", "");
        ((MainActivity) getActivity()).getBTHelper().send(solution.replace(" ", "").getBytes());
        Log.i("Solve Result", solution);
    }

    private String optimize(String str) {
        StringBuffer origin = new StringBuffer(str.replace(" ", ""));
//        System.out.println(origin);
//        System.out.println(origin.length());
        HashMap<Character, Integer> char2int = new HashMap<Character, Integer>();
        char2int.put('F', 0);
        char2int.put('B', 0);
        char2int.put('U', 1);
        char2int.put('D', 1);
        char2int.put('R', 2);
        char2int.put('L', 2);
        String oriFace = "UDFBRL";
        String xRotate = "BFUDRL";    //x整体旋转后映射关系
        String zRotate = "RLFBDU";
        String result = "";
        int[] count = new int[origin.length() + 1];
        int[] nxtPos = new int[origin.length()];
        int[] lastPos = new int[3];
        int from, to;
        char oper;
        for (int i = 0; i < lastPos.length; i++)
            lastPos[i] = origin.length();
        int startPos = 0;
        while (startPos < origin.length() && origin.charAt(startPos) != 'U'
                && origin.charAt(startPos) != 'D') {
            startPos++;
        }

        if (startPos >= origin.length()) {
            return origin.toString();
        }

        for (int i = origin.length() - 1; i >= startPos; i--) {
            if (origin.charAt(i) == '2' || origin.charAt(i) == '\'')
                continue;
            int currentFace = char2int.get(origin.charAt(i));
            int pos1 = lastPos[(currentFace + 1) % 3];
            int pos2 = lastPos[(currentFace + 2) % 3];
            // count[i] = Math.min(cnt1, cnt2) + 1;
            if (count[pos1] < count[pos2]) {
                count[i] = count[pos1] + 1;
                nxtPos[i] = pos1;
            } else {
                count[i] = count[pos2] + 1;
                nxtPos[i] = pos2;
            }
            lastPos[char2int.get(origin.charAt(i))] = i;
//            //nxtPos[i] = 66666;
        }
        result += origin.substring(0, startPos);
        from = startPos;
        to = nxtPos[from];
        while (from < origin.length()) {
            to = nxtPos[from];
            if (to == origin.length() || char2int.get(origin.charAt(to)) == 2) {
                oper = 'z';
            } else {
                oper = 'x';
            }
            for (int i = from; i < origin.length(); i++) {
                char ch;
                int id = oriFace.indexOf(origin.charAt(i));
                if (id == -1) continue;
                if (oper == 'z') ch = zRotate.charAt(id);
                else ch = xRotate.charAt(id);
                origin.setCharAt(i, ch);
            }
            result += oper + origin.substring(from, to);
            from = to;
        }
//        for (int i = 0; i < origin.length(); i++)
//            System.out.print(count[i] + " ");
//        // System.out.print(" ");
//        System.out.println();
//        for (int i = 0; i < origin.length(); i++)
//            System.out.print(nxtPos[i] + " ");
        return result;
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

            for (int j = 0; j < MainActivity.SIZE; j++) {
                LinearLayout l = new LinearLayout(view.getContext());
                l.setOrientation(LinearLayout.HORIZONTAL);
                for (int k = 0; k < MainActivity.SIZE; k++) {
                    TextView tv = new TextView(view.getContext());
                    tv.setLayoutParams(lp);
//                    tv.setBackgroundColor(ColorDetector.nameToRGB(colorName[i]));
//                    tv.setBackgroundResource(R.drawable.border);
                    l.addView(tv);
                    cubePieceTextView[i * 9 + j * 3 + k] = tv;
//                    cubePieceTextView[i * 9 + j * 3 + k].setText(i + "");
                }
                cf.addView(l);
            }
        }
    }

    private void drawCube() {
        colorName = ((MainActivity) getActivity()).getColorName();
        cubeString = ((MainActivity) getActivity()).getCubeString();

        for (int i = 0; i < 54; i++) {
//            cubePieceTextView[i].setBackgroundColor(
//                    ColorDetector.nameToRGB(
//                            colorName[MainActivity.FACES_ORDER.indexOf(cubeString.charAt(i))]));
            String color = colorName[MainActivity.FACES_ORDER.indexOf(cubeString.charAt(i))];
            cubePieceTextView[i].setBackgroundColor(Integer.valueOf(color));
        }
    }
}
