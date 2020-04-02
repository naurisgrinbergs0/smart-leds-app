package com.example.bt;

import android.animation.ObjectAnimator;
import android.view.View;

public abstract class Animator {

    public static int DURATION_SHORT = 1000;
    public static int DURATION_NORMAL = 2000;
    public static int DURATION_LONG = 3000;

    public static void fadeOut(View v, int duration){
        if(v == null)
            return;
        ObjectAnimator.ofFloat(v, "alpha", 0)
                .setDuration(duration)
                .start();
    }

    public static void fadeIn(View v, int duration){
        if(v == null)
            return;
        v.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(v, "alpha", 1)
                .setDuration(duration)
                .start();
    }

    public static void vanish(View v){
        if(v == null)
            return;
        v.setAlpha(0);
        v.setVisibility(View.INVISIBLE);
    }

    public static void appear(View v){
        if(v == null)
            return;
        v.setVisibility(View.VISIBLE);
        v.setAlpha(1);
    }


    public static void move(View v, int duration, int deltaXFromInitial, int deltaYFromInitial){
        if(v == null)
            return;
        if(v.getTranslationX() != deltaXFromInitial)
            ObjectAnimator.ofFloat(v, "translationX", deltaXFromInitial)
                    .setDuration(duration)
                    .start();
        if(v.getTranslationY() != deltaYFromInitial)
            ObjectAnimator.ofFloat(v, "translationY", deltaYFromInitial)
                    .setDuration(duration)
                    .start();
    }

    public static void teleport(View v, int duration, int deltaXFromInitial, int deltaYFromInitial){
        if(v == null)
            return;
        v.setTranslationX(deltaXFromInitial);
        v.setTranslationY(deltaYFromInitial);
    }
}
