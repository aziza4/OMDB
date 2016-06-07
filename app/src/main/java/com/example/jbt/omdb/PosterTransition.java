package com.example.jbt.omdb;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.util.AttributeSet;



@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class PosterTransition extends TransitionSet {

    public PosterTransition() {
        init();
    }


    @SuppressWarnings("unused")  // This constructor allows us to use this transition in XML
    public PosterTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        setOrdering(ORDERING_TOGETHER);
        setDuration(300);

        addTransition(new ChangeBounds()).
                addTransition(new ChangeTransform()).
                addTransition(new ChangeImageTransform());
    }
}
