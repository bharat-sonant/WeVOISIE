package com.wevois.application.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wevois.application.ComplaintActivity;
import com.wevois.application.Interface.SwipeListenerInterface;
import com.wevois.application.Model.LandingListModel;
import com.wevois.application.R;
import com.wevois.application.Utilities.OnSwipeTouchListener;

import java.util.ArrayList;

public class ParentLVAdapter extends BaseAdapter {
    ArrayList<LandingListModel> dataAL;
    Context context;
    boolean isPass;

    public ParentLVAdapter(Context context, ArrayList<LandingListModel> dataAL) {
        this.dataAL = dataAL;
        this.context = context;
        this.isPass = true;
    }

    @Override
    public int getCount() {
        return dataAL.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.parent_lv_layout, null, true);

        LandingListModel model = dataAL.get(i);
        TextView entry = view.findViewById(R.id.entry_tv);
        TextView statusIcon = view.findViewById(R.id.status_icon);
        View topBorderView = view.findViewById(R.id.top_border);
        LinearLayout clickableLayout = view.findViewById(R.id.clickable);

        if (!model.getActionTime().trim().equals("null")) {
            statusIcon.setBackgroundResource(R.drawable.complete_icon);
        } else {
            statusIcon.setBackgroundResource(R.drawable.pending_icon);
        }

        clickableLayout.setOnClickListener(view1 -> {
            if (isPass){
                isPass = false;
                Intent intent = new Intent(context, ComplaintActivity.class);
                intent.putExtra("LandingListModel", model);
                context.startActivity(intent);
            }
        });

        if (i == 0) {
            topBorderView.setVisibility(View.VISIBLE);
        } else {
            topBorderView.setVisibility(View.GONE);
        }
        entry.setText(model.getAdd());
        return view;
    }
}
