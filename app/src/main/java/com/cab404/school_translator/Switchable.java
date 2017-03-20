package com.cab404.school_translator;

/**
 * Created at 22:37 on 20/03/17
 *
 * @author cab404
 */
public interface Switchable {
    void hide(Class replacer);
    void show();
    void hideImmediate();
}
