package com.wevois.application.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.wevois.application.Model.TypesListModel;
import com.wevois.application.R;
import com.wevois.application.Interface.TypeLvInterface;

import java.util.ArrayList;

public class TypesLVAdapter extends BaseAdapter {
    ArrayList<TypesListModel> dataAL;
    Context context;
    TypeLvInterface typeLvInterface;

    public TypesLVAdapter(Context context, ArrayList<TypesListModel> dataAL, TypeLvInterface typeLvInterface) {
        this.dataAL = dataAL;
        this.context = context;
        this.typeLvInterface = typeLvInterface;
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
        return i;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.types_lv_layout, null, true);
        TypesListModel model = dataAL.get(i);
        TextView nmTv = view.findViewById(R.id.type_name);
        RadioButton rBtn = view.findViewById(R.id.r_btn);
        LinearLayout pLayout = view.findViewById(R.id.p_layout);
        nmTv.setText(model.getTypeNm());

        rBtn.setChecked(model.isSelected());

        rBtn.setOnClickListener(view12 -> {
            for (TypesListModel typesListModel : dataAL) {
                typesListModel.setSelected(false);
            }
            model.setSelected(true);
            notifyDataSetChanged();
            typeLvInterface.onItemClick(i, model);
        });

        pLayout.setOnClickListener(view1 -> {
            for (TypesListModel typesListModel : dataAL) {
                typesListModel.setSelected(false);
            }
            model.setSelected(true);
            notifyDataSetChanged();
            typeLvInterface.onItemClick(i, model);
        });

        return view;
    }
}
