package com.cab404.school_translator.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.viewbinder.BindContext;
import com.cab404.chumroll.viewbinder.DataBindContext;
import com.cab404.chumroll.viewbinder.ViewBinder;
import com.cab404.chumroll.viewbinder.ViewBinderItem;
import com.cab404.school_translator.App;
import com.cab404.school_translator.R;
import com.cab404.school_translator.data.DaoSession;
import com.cab404.school_translator.data.LocalTranslation;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created at 01:39 on 28/03/17
 *
 * @author cab404
 */
public class TranslationItem extends ViewBinderItem<LocalTranslation> {

    class VB extends ViewBinder<LocalTranslation> {
        private final BindContext context;

        @BindView(R.id.root)
        LinearLayout mRoot;
        @BindView(R.id.from)
        TextView mFrom;
        @BindView(R.id.to)
        TextView mTo;
        @BindView(R.id.dir)
        TextView mDir;
        @BindView(R.id.favourites)
        ImageView mFavs;

        LocalTranslation mData;

        public VB(BindContext context) {
            this.context = context;
            ButterKnife.bind(this, context.getView());
        }

        @Override
        protected void reuse(DataBindContext<LocalTranslation> context) {
            mData = context.getData();
            mTo.setText(mData.translatedText);
            mFrom.setText(mData.sourceText);
            mDir.setText(mData.lang);
            mFavs.setImageResource(
                    mData.getInFavourites() ?
                            R.drawable.ic_favorite_black_24dp :
                            R.drawable.ic_favorite_border_black_24dp
            );
        }

        @OnClick(R.id.favourites)
        public void favs() {
            mData.inFavourites = !mData.inFavourites;
            final Context context = this.context.getView().getContext();
            App.instance().getDaoSession()
                    .getLocalTranslationDao()
                    .save(mData);
            Toast.makeText(context,
                    mData.inFavourites ?
                            R.string.added_to_favs
                            :
                            R.string.removed_from_favs,
                    Toast.LENGTH_SHORT).show();
            this.context.getAdapter().notifyDataSetChanged();
        }

    }

    @Override
    protected ViewBinder<LocalTranslation> getBinder(BindContext context) {
        return new VB(context);
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup parent, ChumrollAdapter adapter) {
        return inflater.inflate(R.layout.item_history_result, parent, false);
    }

    @Override
    public boolean enabled(LocalTranslation data, int index, ChumrollAdapter adapter) {
        return true;
    }
}
