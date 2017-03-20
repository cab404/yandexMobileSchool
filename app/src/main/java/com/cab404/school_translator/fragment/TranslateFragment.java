package com.cab404.school_translator.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
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
import com.cab404.school_translator.item.LangItem;
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
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        ButterKnife.bind(this, rootView);

        // Mapping languages into list of pairs, so it will be easier to get language by index
        for (Map.Entry<String, String> entry : App.get(getContext()).getTranslations().langs.entrySet()) {
            languages.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        Collections.sort(languages, (a, b) -> a.second.compareTo(b.second));


        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_label);
        adapter.addAll(App.get(getContext()).getTranslations().langs.values());

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
            final int index = getLanguageIndex(App.get(getContext()).getUILanguage());
            if (index != -1)
                mToLang.setSelection(index);
        }

        {
            // Language autodetection
            RxTextView.textChanges(mText)
                    .doOnNext((text) -> moveTranslationUp())
                    .debounce(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                    .map(CharSequence::toString)
                    .subscribe(
                            (text) -> {
                                if (getFromLang() == null)
                                    App.get(getContext())
                                            .getClient()
                                            .getApi()
                                            .detect(text)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe((result -> mFromLang
                                                    .setSelection(getLanguageIndex(result.lang) + 1)));
                            });

        }

        {
            // Translation
            mTranslate.setOnClickListener((view) -> {
                        App.get(getContext())
                                .getClient()
                                .getApi()
                                .translate(mText.getText().toString(), getLang())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        translation -> {
                                            if (!translation.text.isEmpty())
                                                mTranslation.setText(translation.text.get(0));
                                            dropTranslationDown();
                                            bringTextBack();
                                        }
                                );
                        dropTextDown();
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

        {
            // Entrance animation
        }

    }

    private void dropTextDown() {
        ViewCompat.animate(mText).cancel();
        ViewCompat.animate(mProgress).alpha(1).start();
        ViewCompat.animate(mText).translationY(mText.getHeight()).alpha(0).start();

    }

    private void bringTextBack() {
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
