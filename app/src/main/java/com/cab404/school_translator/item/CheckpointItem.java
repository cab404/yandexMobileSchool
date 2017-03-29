package com.cab404.school_translator.item;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.ViewConverter;

/**
 * Made to detect reaching certain place in text.
 *
 * @author cab404
 */
public class CheckpointItem implements ViewConverter<Runnable> {
    @Override
    public void convert(View view, Runnable data, int index, ViewGroup parent, ChumrollAdapter adapter) {
        data.run();
        view.post(() -> {
            adapter.remove(index);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup parent, ChumrollAdapter adapter) {
        return new View(inflater.getContext());
    }

    @Override
    public boolean enabled(Runnable data, int index, ChumrollAdapter adapter) {
        return false;
    }
}
