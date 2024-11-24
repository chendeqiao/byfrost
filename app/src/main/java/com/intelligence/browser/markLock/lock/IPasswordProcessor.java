package com.intelligence.browser.markLock.lock;

import androidx.annotation.NonNull;

public interface IPasswordProcessor {

    void handlePassword(@NonNull String pwd, int pwdLength, OnPswHandledListener listener);
}
