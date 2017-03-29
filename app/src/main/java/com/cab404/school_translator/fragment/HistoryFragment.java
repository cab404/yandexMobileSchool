package com.cab404.school_translator.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.school_translator.App;
import com.cab404.school_translator.R;
import com.cab404.school_translator.data.LocalTranslation;
import com.cab404.school_translator.data.LocalTranslationDao;
import com.cab404.school_translator.item.CheckpointItem;
import com.cab404.school_translator.item.TranslationItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created at 22:34 on 20/03/17
 *
 * @author cab404
 */
public class HistoryFragment extends Fragment {

    // Let's just limit max amount of loaded translations, to be safe
    public static final int LIMIT = 100;
    // Amount of items added to list in one operation
    public static final int ONEGO = 20;

    @BindView(R.id.list)
    ListView mList;

    @OnItemClick(R.id.list)
    public void onItemClicked(int pos) {
        if (!mAdapter.classOf(pos).equals(TranslationItem.class)) return;


        ClipboardManager manager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        final LocalTranslation item = (LocalTranslation) mAdapter.getItem(pos);
        manager.setPrimaryClip(ClipData.newPlainText(
                getString(R.string.app_name),
                item.translatedText
        ));

        Toast.makeText(getContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    private ChumrollAdapter mAdapter = new ChumrollAdapter();
    private Runnable mContinuator;
    private Disposable mListGovernor;

    {
        mAdapter.prepareFor(
                new TranslationItem(),
                new CheckpointItem()
        );
    }

    private final static String TAG = "HistoryFragment";

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mAdapter.clear();
        mList.setAdapter(mAdapter);
        mListGovernor = Observable
                .<Integer>create((emitter) -> {
                    // This will fire as soon as we'll get to last item
                    // in list, and will add next page
                    mContinuator = new Runnable() {
                        int page = 0;

                        @Override
                        public void run() {
                            Log.d(TAG, "Next page " + page);
                            emitter.onNext(page++);
                        }
                    };
                    mAdapter.add(CheckpointItem.class, mContinuator);
                    mAdapter.notifyDataSetChanged();
                })
                // Because we add continuator to the list in creation of observable,
                // and it's a main-thread-only task.
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.computation())
                .switchMap((i) ->
                        Observable.fromIterable(
                                App.instance().getDaoSession().getLocalTranslationDao()
                                        .queryBuilder()
                                        .orderDesc(
                                                LocalTranslationDao.Properties.InFavourites,
                                                LocalTranslationDao.Properties.Timestamp
                                        )
                                        .limit(LIMIT)
                                        .offset(LIMIT * i)
                                        .list()
                        ).buffer(ONEGO)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((translations) -> {
                    mAdapter.addAll(
                            TranslationItem.class,
                            translations
                    );
                    mAdapter.add(
                            CheckpointItem.class,
                            mContinuator
                    );
                    mAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListGovernor.dispose();
    }

}
