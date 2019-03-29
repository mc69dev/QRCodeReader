package mchou.com.pedago;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;

public class Util {

    public static void show(Context context, Class<?> activity){
        Intent intent= new Intent(context, activity);
        context.startActivity(intent);
    }

    public static void hideSystemUI(Window window) {
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY    //SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
    public static void showSystemUI(Window window) {
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static CharSequence spanIconText(Drawable r, String title) {
        r.setBounds(0, 0, r.getIntrinsicWidth()/2, r.getIntrinsicHeight()/2);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BASELINE);
        SpannableStringBuilder sb = new SpannableStringBuilder("    " + title);
        RelativeSizeSpan sizedText = new RelativeSizeSpan(1.2f);
        sb.setSpan(imageSpan, // Span to add
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE // Do not extend the span when text add later
        );
        sb.setSpan(sizedText, 0, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(Color.rgb(0,42,77)), 0, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }


    public static void customToolbar(ActionBar actionbar, int logo, int background_color) {
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setIcon(logo);
        actionbar.setBackgroundDrawable(new ColorDrawable(background_color));

        //actionbar.setDisplayShowTitleEnabled(false);
    }
}
