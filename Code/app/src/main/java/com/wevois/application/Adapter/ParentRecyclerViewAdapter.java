package com.wevois.application.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wevois.application.Interface.TypeLvInterface;
import com.wevois.application.model.LandingListModel;
import com.wevois.application.databinding.ParentLvLayoutBinding;

import java.util.ArrayList;

public class ParentRecyclerViewAdapter extends RecyclerView.Adapter<ParentRecyclerViewAdapter.ParentViewHolder> {

    ArrayList<LandingListModel> models;
    Context context;
    TypeLvInterface typeLvInterface;

    public ParentRecyclerViewAdapter(ArrayList<LandingListModel> models, Context context, TypeLvInterface typeLvInterface){
        this.models = models;
        this.context = context;
        this.typeLvInterface = typeLvInterface;
    }

    @NonNull
    @Override
    public ParentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ParentLvLayoutBinding parentLvLayoutBinding = ParentLvLayoutBinding.inflate(layoutInflater,parent,false);

        return new ParentViewHolder(parentLvLayoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ParentViewHolder holder, int position) {
        LandingListModel model = models.get(position);
        holder.parentLvLayoutBinding.setParent(model);
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    class ParentViewHolder extends RecyclerView.ViewHolder{
        ParentLvLayoutBinding parentLvLayoutBinding;

        public ParentViewHolder(ParentLvLayoutBinding itemVeiw){
            super(itemVeiw.getRoot());
            this.parentLvLayoutBinding = itemVeiw;
            itemVeiw.clickable.setOnClickListener(v -> {
                typeLvInterface.onItemClick(getAdapterPosition(), models.get(getAdapterPosition()));
            });
        }
    }
}
