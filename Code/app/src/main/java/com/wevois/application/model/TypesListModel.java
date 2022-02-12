package com.wevois.application.model;

public class TypesListModel {
    int id;
    String typeNm;
    boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Integer getId() {
        return id;
    }

    public String getTypeNm() {
        return typeNm;
    }

    public TypesListModel(Integer id, String typeNm,boolean isSelected) {
        this.id = id;
        this.typeNm = typeNm;
        this.isSelected = isSelected;
    }
}
