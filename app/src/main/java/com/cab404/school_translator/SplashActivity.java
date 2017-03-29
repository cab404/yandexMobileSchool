package com.cab404.school_translator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created at 02:07 on 20/03/17
 *
 * @author cab404
 */
public class SplashActivity extends AppCompatActivity {
    private final static String TAG = "SplashActivity";

    @BindView(R.id.current_status)
    TextView currentStatus;

    private Disposable titleChanger;
    private SpannableStringBuilder status = new SpannableStringBuilder();

    /**
     * Appends line to status text
     */
    @SuppressWarnings("SimplifyStreamApiCallChains")
    public void appendLoadingLine(String line) {
        line = line + "\n";
        final String s = Observable.fromArray(status.toString().split("\n"))
                .take(4)
                .reduce((a, b) -> a + "\n" + b)
                .blockingGet();
        status.clear();
        status.append(line).append(s);
        status.setSpan(
                new ForegroundColorSpan(0xffa3a3a3),
                line.length(),
                status.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
        currentStatus.setText(status);

        // Animating line moving downwards
        currentStatus.setTranslationY(-currentStatus.getHeight() / 4);
        ViewCompat.animate(currentStatus).translationY(0).setDuration(100).start();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        // Generates loading text
        final String[] titles = getResources().getStringArray(R.array.loading_quotes);
        titleChanger = Observable.interval(235, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .map((a) -> titles[(int) (Math.random() * titles.length)] + "…")
                .subscribe(this::appendLoadingLine);

        // Loading list of languages
        App.instance()
                .getClient()
                .getApi()
                .getLanguages(Utils.getUILanguage(this))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(this::finite)
                .subscribe(
                        (translations) -> App.instance()
                                .setTranslations(translations),
                        (e) -> {
                            Snackbar.make(
                                    findViewById(R.id.root),
                                    "Ошибка сети: " + e.getMessage(),
                                    Snackbar.LENGTH_INDEFINITE
                            ).show();
                        }
                );
    }

    private void finite() {
        // Just for looks, we will wait slightly longer on loading page
        App.handler.postDelayed(
                () -> {
                    appendLoadingLine(getString(R.string.waiting) + "…");
                    titleChanger.dispose();
                }, 1000
        );
        App.handler.postDelayed(
                () -> {
                    finish();
                    startActivity(new Intent(this, MainActivity.class));
                }, 1500
        );
    }

}
