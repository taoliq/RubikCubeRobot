package com.example.logqtainia.March;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

/**
 * Created by tarjan on 17-9-17.
 */

public class ManualControlFragment extends Fragment {
    private Button btnPos, btnNeg;
    private Button btnCatch, btnRelease;

    private int singleStepperId = 0;
    private int multiStepperId = 4;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manual_control, container, false);

        ButtonListener b = new ButtonListener();
        btnPos = (Button) view.findViewById(R.id.btn_pos);
        btnPos.setOnTouchListener(b);
        btnNeg = (Button) view.findViewById(R.id.btn_neg);
        btnNeg.setOnTouchListener(b);
        btnCatch = (Button) view.findViewById(R.id.btn_catch);
        btnCatch.setOnTouchListener(b);
        btnRelease = (Button) view.findViewById(R.id.btn_release);
        btnRelease.setOnTouchListener(b);

        RadioGroup rgroupSingle = (RadioGroup) view.findViewById(R.id.rgroup_single);
        rgroupSingle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbtn_F:
                        singleStepperId = 0;
                        break;
                    case R.id.rbtn_R:
                        singleStepperId = 1;
                        break;
                    case R.id.rbtn_B:
                        singleStepperId = 2;
                        break;
                    case R.id.rbtn_L:
                        singleStepperId = 3;
                        break;
                    default:
                        break;
                }
            }
        });

        RadioGroup rgroupMulti = (RadioGroup) view.findViewById(R.id.rgroup_multi);
        rgroupMulti.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbtn_FB_distance:
                        multiStepperId = 4;
                        break;
                    case R.id.rbtn_RL_distance:
                        multiStepperId = 5;
                        break;
                    default:
                        break;
                }
            }
        });

        return view;
    }

    class ButtonListener implements View.OnClickListener, View.OnTouchListener {

        public void onClick(View v) {
            if (v.getId() == R.id.btn_pos) {
                Log.d("test", "cansal button ---> click");
            }
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                switch (v.getId()) {
                    case R.id.btn_pos:
                        ((MainActivity) getActivity()).getBTHelper().send((singleStepperId + "2").getBytes());
                        break;
                    case R.id.btn_neg:
                        ((MainActivity) getActivity()).getBTHelper().send((singleStepperId + "0").getBytes());
                        break;
                    case R.id.btn_catch:
                        ((MainActivity) getActivity()).getBTHelper().send((multiStepperId + "0").getBytes());
                        break;
                    case R.id.btn_release:
                        ((MainActivity) getActivity()).getBTHelper().send((multiStepperId + "2").getBytes());
                        break;
                    default:
                        break;
                }
//                    Log.d("test", "cansal button ---> down");
//                    mButton.setBackgroundResource(R.drawable.yellow);
//                    ((MainActivity) getActivity()).getBTHelper().send("10".getBytes());
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.d("test", "cansal button ---> cancel");
//                    mButton.setBackgroundResource(R.drawable.green);
//                ((MainActivity) getActivity()).getBTHelper().send("01".getBytes());
                switch (v.getId()) {
                    case R.id.btn_pos:
                    case R.id.btn_neg:
                        ((MainActivity) getActivity()).getBTHelper().send((singleStepperId + "1").getBytes());
                        break;
                    case R.id.btn_catch:
                    case R.id.btn_release:
                        ((MainActivity) getActivity()).getBTHelper().send((multiStepperId + "1").getBytes());
                        break;
                    default:
                        break;
                }
            }
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //得到Fragment的根布局并使该布局可以获得焦点
        getView().setFocusableInTouchMode(true);
        //得到Fragment的根布局并且使其获得焦点
        getView().requestFocus();
        //对该根布局View注册KeyListener的监听
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button
                    Log.i("get back", "success");
                    ((MainActivity) getActivity()).getBTHelper().send(":Solve".getBytes());
                    return false;
                }
                return false;
            }
        });
    }
}
