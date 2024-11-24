package com.intelligence.componentlib.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.DialogFragment;

import com.intelligence.browser.R;

public abstract class BaseDialogFragment extends DialogFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }

    protected abstract @LayoutRes
    int getLayoutId();

    protected abstract @StyleRes
    int getWindowAnimation();

    protected abstract void onItemClick(int viewId);

    protected abstract void initView(View view);

    protected int getLayoutHeight(){
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }

    protected int getLayoutWidth(){
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setLayout(getLayoutHeight(), getLayoutWidth());
            window.setWindowAnimations(R.style.animate_dialog);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setCancelable(true);
        }
    }

    @Override
    public void onClick(View v) {
        onItemClick(v.getId());
    }
}