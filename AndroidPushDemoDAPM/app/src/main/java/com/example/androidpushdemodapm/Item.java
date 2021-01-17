package com.example.androidpushdemodapm;

public class Item {

    private String mTexto;
    private int mId;
    private boolean mCompleto;

    public Item() {
    }

    public Item(String texto, int id, boolean completo) {
        mTexto = texto;
        mId = id;
        mCompleto = completo;
    }

    public String getTexto() {
        return mTexto;
    }

    public void setTexto(String texto) {
        mTexto = texto;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public boolean isCompleto() {
        return mCompleto;
    }

    public void setCompleto(boolean completo) {
        mCompleto = completo;
    }

    public boolean igual(Object o){
        return o instanceof Item && ((Item)o).mId==mId;
    }


}
