package com.wevois.application.Interface;

import com.wevois.application.model.LandingListModel;
import com.wevois.application.model.TypesListModel;

public interface TypeLvInterface {
    void onItemClick(int position, TypesListModel model);

    void onItemClick(int position, LandingListModel model);
}
