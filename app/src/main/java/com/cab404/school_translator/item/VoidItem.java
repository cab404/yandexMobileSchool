package com.cab404.school_translator.item;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.ViewConverter;

/**
 * Made with sole purpose of adding voids in lists, so scrollbars will be beautiful.
 * <p>
 * Created at 02:02 on 29/03/17
 *
 * @author cab404
 */
public class VoidItem implements ViewConverter {
    @Override
    public void convert(View view, Object data, int index, ViewGroup parent, ChumrollAdapter adapter) {
//        throw new UnsupportedOperationException("You fell into the void.");
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup parent, ChumrollAdapter adapter) {
        return new View(parent.getContext());
    }

    @Override
    public boolean enabled(Object data, int index, ChumrollAdapter adapter) {
        return false;
    }
}
