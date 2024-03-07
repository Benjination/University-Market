package com.example.universitymarket.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.*;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.NetListener;
import com.example.universitymarket.utilities.Network;

import java.util.HashMap;

public class TestFragment extends Fragment implements View.OnClickListener {

    private View root;
    private LayoutInflater inflater;
    private ViewGroup container;
    private static Test test;

    public TestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        root = inflater.inflate(R.layout.fragment_test, container, false);
        configureButtons(root);
        return root;
    }

    private void configureButtons(View v) {
        Button uploadButton = v.findViewById(R.id.test_upload_button);
        Button clearButton = v.findViewById(R.id.test_clear_button);
        Button downloadButton = v.findViewById(R.id.test_download_button);
        uploadButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);
    }

    private void displayPopup(View v) {
        View popupView = inflater.inflate(R.layout.test_popup, container, false);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

        TextView c1 = popupView.findViewById(R.id.test_coll_lvl1_txt);
        TextView f1 = popupView.findViewById(R.id.test_field_lvl1_txt);
        TextView f2 = popupView.findViewById(R.id.test_field_lvl2_txt);
        TextView f3 = popupView.findViewById(R.id.test_field_lvl3_txt);
        TextView l1 = popupView.findViewById(R.id.test_list_lvl1_txt);
        TextView l2 = popupView.findViewById(R.id.test_list_lvl2_txt);
        c1.setText(test.getCollLvl1().toString());
        f1.setText(test.getFieldLvl1());
        f2.setText(test.getFieldLvl2());
        l1.setText(test.getListLvl1().toString());
        l2.setText(test.getListLvl2().toString());

        popupView.setOnTouchListener((view, event) -> {
                    v.performClick();
                    popupWindow.dismiss();
                    return true;
                }
        );
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();
        if(ID == R.id.test_upload_button) {
            ActiveUser.id = "test";
            //Network.setTestFromCache(requireActivity(),"test2", false,null);
        }
        if(ID == R.id.test_clear_button) {
            Toast.makeText(
                    getContext(),
                    ActiveUser.id,
                    Toast.LENGTH_SHORT
            ).show();
            //Network.setTestFromCache(requireActivity(),"test2", false,null);
        }
        if(ID == R.id.test_download_button) {
            Network.getTest(requireActivity(),"test", new NetListener<Test>() {
                @Override
                public void onSuccess(Test result) {
                    Data.setCache(requireActivity(), result, false);
                    HashMap<String, Object> data = Data.getCachedToPOJO(requireActivity(), result);
                    if(data != null) {
                        test = new Test(data);
                        displayPopup(v);
                    }
                }

                @Override
                public void onFailure(Exception error) {
                    Log.e("getTest", error.getMessage());
                    Toast.makeText(
                            getContext(),
                            error.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }
}