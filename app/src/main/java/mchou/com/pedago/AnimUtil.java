package mchou.com.pedago;

import android.animation.Animator;
import android.view.View;
import android.view.ViewAnimationUtils;

public class AnimUtil {

    public static void CircularReveal(View view) {
        long duration = view.getContext().getResources().getInteger(android.R.integer.config_longAnimTime);
        // Check if the runtime version is at least Lollipop
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        // get the center for the clipping circle
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;

        // get the final radius for the clipping circle
        float finalRadius = (float) Math.hypot(cx, cy);

        // create the animator for this view (the start radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0f, finalRadius);

        // make the view visible and start the animation
        view.setVisibility(View.VISIBLE);
        anim.setDuration(duration).start();
    }
}
