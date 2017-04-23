package com.fc.materialbomb.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fc.materialbomb.MainActivity;
import com.fc.materialbomb.R;

/**
 * Created by Administrator on 2017/4/23 0023.
 */

public class LeadFragment extends Fragment{



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        int index = bundle.getInt("index");
        int layoutId = bundle.getInt("layoutId");

        View view = inflater.inflate(layoutId,null);
        view.setTag(index);

        if(R.layout.fragment_lead3 == layoutId){

            Button btn = (Button) view.findViewById(R.id.btn_login);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LeadFragment.this.getActivity(), MainActivity.class);
                    startActivity(intent);
                }
            });
        }
        return view;
    }
}
