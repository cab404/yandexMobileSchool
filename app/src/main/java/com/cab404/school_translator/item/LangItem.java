package com.cab404.school_translator.item;

import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.ViewConverter;
import com.cab404.school_translator.R;


/**
 * Simple item for languages in spinners.
 * <p>
 * Created at 17:44 on 20/03/17
 *
 * @author cab404
 */
public class LangItem implements ViewConverter<Pair<String, String>> {

    @Override
    public void convert(View view, Pair<String, String> data, int index, ViewGroup parent, ChumrollAdapter adapter) {
        ((TextView) view).setText(data.second);
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup parent, ChumrollAdapter adapter) {
        return inflater.inflate(R.layout.item_label, parent, false);
    }

    @Override
    public boolean enabled(Pair<String, String> data, int index, ChumrollAdapter adapter) {
        return true;
    }

}
