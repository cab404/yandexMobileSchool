package com.cab404.school_translator.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.school_translator.App;
import com.cab404.school_translator.R;
import com.cab404.school_translator.Switchable;
import com.cab404.school_translator.Utils;
import com.cab404.school_translator.data.LocalTranslation;
import com.cab404.school_translator.item.LangItem;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created at 21:55 on 19/03/17
 *
 * @author cab404
 */
public class TranslateFragment extends Fragment implements Switchable {

    private static final String TAG = "TranslateFragment";
    @BindView(R.id.lang_from)
    Spinner mFromLang;
    @BindView(R.id.lang_to)
    Spinner mToLang;
    @BindView(R.id.translate)
    View mTranslate;
    @BindView(R.id.text)
    EditText mText;
    @BindView(R.id.translation)
    TextView mTranslation;
    @BindView(R.id.swap_langs)
    View mSwap;
    @BindView(R.id.progress)
    ProgressBar mProgress;

    private List<Pair<String, String>> languages = new ArrayList<>();
    private BroadcastReceiver connectivityListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (getView() != null) {
                getView().post(() -> {
                    final NetworkInfo info = manager.getActiveNetworkInfo();
                    if (info == null || !info.isConnected()) {
                        mNetworkAlertSnackbar = Snackbar
                                .make(
                                        getView(),
                                        "Похоже, у нас проблемы с сетью :(",
                                        Snackbar.LENGTH_INDEFINITE
                                );
                        mNetworkAlertSnackbar.show();
                    } else {
                        if (mNetworkAlertSnackbar != null) {
                            mNetworkAlertSnackbar.dismiss();
                            mNetworkAlertSnackbar = null;
                        }
                    }
                });
            }
        }
    };
    private Snackbar mNetworkAlertSnackbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_translator, container, false);
    }

    /**
     * Returns null if autodetect is selected
     */
    @Nullable
    private String getFromLang() {
        if (mFromLang.getSelectedItemPosition() == 0) return null;
        return languages.get(mFromLang.getSelectedItemPosition() - 1).first;
    }

    private String getToLang() {
        return languages.get(mToLang.getSelectedItemPosition()).first;
    }

    private int getLanguageIndex(String code) {
        for (int i = 0; i < languages.size(); i++)
            if (languages.get(i).first.equals(code))
                return i;
        return -1;
    }

    private String getLang() {
        String from = getFromLang();
        String to = getToLang();
        return from == null ? to : (from + "-" + to);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
        getActivity().registerReceiver(
                connectivityListener,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
        getActivity().unregisterReceiver(
                connectivityListener
        );
    }


    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        ButterKnife.bind(this, rootView);

        // Mapping languages into list of pairs, so it will be easier to get language by index
        for (Map.Entry<String, String> entry : App.instance().getTranslations().langs.entrySet()) {
            languages.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        Collections.sort(languages, (a, b) -> a.second.compareTo(b.second));


        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_label);
        adapter.addAll(App.instance().getTranslations().langs.values());

        {
            // "From language" spinner
            final ChumrollAdapter fromAdapter = new ChumrollAdapter();
            fromAdapter.prepareFor(new LangItem());
            fromAdapter.add(LangItem.class, new Pair<>(null, getString(R.string.autodetect)));
            fromAdapter.addAll(LangItem.class, languages);
            mFromLang.setAdapter(fromAdapter);
        }

        {
            // "To language" spinner
            final ChumrollAdapter toAdapter = new ChumrollAdapter();
            toAdapter.prepareFor(new LangItem());
            toAdapter.addAll(LangItem.class, languages);
            mToLang.setAdapter(toAdapter);
            final int index = getLanguageIndex(Utils.getUILanguage(getContext()));
            if (index != -1)
                mToLang.setSelection(index);
        }

        {
            // Language autodetection
            RxTextView.textChanges(mText)
                    .doOnNext((text) -> moveTranslationUp())
                    .debounce(1, TimeUnit.SECONDS)
                    .filter((text) -> text.length() > 0)
                    .map(CharSequence::toString)
                    // Not using switchMap because it kills everything
                    // upwards on error, including text listener
                    .subscribe(
                            (query) -> App.instance()
                                    .getClient()
                                    .getApi()
                                    .detect(query)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            (result) -> mFromLang.setSelection(getLanguageIndex(result.lang) + 1),
                                            error -> mFromLang.setSelection(0)
                                    )
                    );

        }

        {
            // Translation
            RxView.clicks(mTranslate)
                    .filter(ignored -> mText.getText().length() > 0)
                    .doOnNext(ignored -> dropTextDown())
                    .switchMap(ignored ->
                            App.instance()
                                    .getClient()
                                    .getApi()
                                    .translate(mText.getText().toString(), getLang())
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            translation -> {
                                // Saving translation
                                App.instance().getDaoSession()
                                        .getLocalTranslationDao()
                                        .insert(
                                                new LocalTranslation(
                                                        mText.getText().toString(),
                                                        translation
                                                )
                                        );
                                if (!translation.text.isEmpty())
                                    mTranslation.setText(translation.text.get(0));
                                dropTranslationDown();
                                bringTextBack();
                            },
                            error -> {
                                errorTing();
                                if (getView() != null)
                                    Snackbar.make(
                                            getView(),
                                            R.string.network_problems,
                                            Snackbar.LENGTH_SHORT
                                    ).show();
                                moveTranslationUp();
                                bringTextBack();
                            }
                    );
        }

        {
            // Language swap animation
            mSwap.setOnClickListener(
                    view -> {
                        if (getFromLang() != null) {
                            mToLang.setClickable(false);
                            mFromLang.setClickable(false);
                            ViewCompat.animate(mToLang)
                                    .translationX(mFromLang.getX() - mToLang.getX())
                                    .setDuration(200)
                                    .start();
                            ViewCompat.animate(mFromLang)
                                    .translationX(mToLang.getX() - mFromLang.getX())
                                    .setDuration(200)
                                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(View view) {
                                            super.onAnimationEnd(view);
                                            int toIndex = mFromLang.getSelectedItemPosition() - 1;
                                            mFromLang.setSelection(mToLang.getSelectedItemPosition() + 1);
                                            mToLang.setSelection(toIndex);
                                            mToLang.setTranslationX(0);
                                            mFromLang.setTranslationX(0);
                                            mToLang.setClickable(true);
                                            mFromLang.setClickable(true);
                                        }
                                    })
                                    .start();
                        }
                    }
            );

        }

    }

    private void animateTing(float to) {
        ViewCompat.animate(mTranslate)
                .translationX(to)
                .setInterpolator(new BounceInterpolator())
                .setDuration(70)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        // after first animation we go to the opposite of first destination x
                        // (and assuming it was positive)
                        if (to > 0)
                            animateTing(-to);
                        // and after second animation we go back to zero
                        if (to < 0)
                            animateTing(0);
                    }
                })
                .start();
    }

    // Shakes translation button
    private void errorTing() {
        animateTing(getResources().getDisplayMetrics().density * 3);
    }

    private void dropTextDown() {
        mText.setEnabled(false);
        mTranslate.setEnabled(false);
        ViewCompat.animate(mText).cancel();
        ViewCompat.animate(mProgress).alpha(1).start();
        ViewCompat.animate(mText).translationY(mText.getHeight()).alpha(0).start();
    }

    private void bringTextBack() {
        mText.setEnabled(true);
        mTranslate.setEnabled(true);
        ViewCompat.animate(mText).cancel();
        ViewCompat.animate(mProgress).alpha(0).start();
        ViewCompat.animate(mText).translationY(0).alpha(1).start();
    }

    private void dropTranslationDown() {
        ViewCompat.animate(mTranslation).cancel();
        ViewCompat.animate(mTranslation)
                .translationY(0)
                .setDuration(200)
                .alpha(1)
                .setInterpolator(new BounceInterpolator())
                .start();
    }

    private void moveTranslationUp() {
        ViewCompat.animate(mTranslation).cancel();
        ViewCompat.animate(mTranslation)
                .translationY(-getResources().getDisplayMetrics().density * 30)
                .setDuration(200)
                .alpha(0)
                .start();
    }

    @Override
    public void hide(Class replacer) {

    }

    @Override
    public void show() {

    }

    @Override
    public void hideImmediate() {

    }
}
