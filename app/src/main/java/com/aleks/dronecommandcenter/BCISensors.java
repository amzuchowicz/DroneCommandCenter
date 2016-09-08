package com.aleks.dronecommandcenter;

import android.test.suitebuilder.annotation.Suppress;
import android.view.View;
import android.widget.ImageView;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEmoStateDLL;

/**
 * Created by Aleks on 3/08/2016.
 */
@SuppressWarnings("WrongConstant")
public final class BCISensors {
    private static final int CMS = 0; //stuck on 4?
    private static final int F3 = 3; //AF3
    private static final int F4 = 16; //AF4
    private static final int T7 = 7;
    private static final int T8 = 12;
    private static final int PZ = 9;

    private static final int RED = 0;
    private static final int YELLOW = 1;
    private static final int GREEN = 2;

    public BCISensors() {

    }

    public static void update(View v) {
        int[] contactQuality = IEmoStateDLL.IS_GetContactQualityFromAllChannels();
        //printOutput(contactQuality);
        int[][] visibility = new int[contactQuality.length][3];
        for(int i = 0; i < contactQuality.length; i++) {
            switch(contactQuality[i]) {
                case 0:
                    visibility[i][RED] = View.INVISIBLE;
                    visibility[i][YELLOW] = View.INVISIBLE;
                    visibility[i][GREEN] = View.INVISIBLE;
                    break;
                case 1:
                    visibility[i][RED] = View.VISIBLE;
                    visibility[i][YELLOW] = View.INVISIBLE;
                    visibility[i][GREEN] = View.INVISIBLE;
                    break;
                case 2:
                    visibility[i][RED] = View.INVISIBLE;
                    visibility[i][YELLOW] = View.VISIBLE;
                    visibility[i][GREEN] = View.INVISIBLE;
                    break;
                case 4:
                    visibility[i][RED] = View.INVISIBLE;
                    visibility[i][YELLOW] = View.INVISIBLE;
                    visibility[i][GREEN] = View.VISIBLE;
                    break;
                default:
                    visibility[i][RED] = View.INVISIBLE;
                    visibility[i][YELLOW] = View.INVISIBLE;
                    visibility[i][GREEN] = View.INVISIBLE;
            }
        }

        ImageView imgCFRred = (ImageView) v.findViewById(R.id.imgCFRred);
        imgCFRred.setVisibility(visibility[CMS][RED]);
        ImageView imgCFRyellow = (ImageView) v.findViewById(R.id.imgCFRyellow);
        imgCFRyellow.setVisibility(visibility[CMS][YELLOW]);
        ImageView imgCFRgreen = (ImageView) v.findViewById(R.id.imgCFRgreen);
        imgCFRgreen.setVisibility(visibility[CMS][GREEN]);

        ImageView imgF3red = (ImageView) v.findViewById(R.id.imgF3red);
        imgF3red.setVisibility(visibility[F3][RED]);
        ImageView imgF3yellow = (ImageView) v.findViewById(R.id.imgF3yellow);
        imgF3yellow.setVisibility(visibility[F3][YELLOW]);
        ImageView imgF3green = (ImageView) v.findViewById(R.id.imgF3green);
        imgF3green.setVisibility(visibility[F3][GREEN]);

        ImageView imgF4red = (ImageView) v.findViewById(R.id.imgF4red);
        imgF4red.setVisibility(visibility[F4][RED]);
        ImageView imgF4yellow = (ImageView) v.findViewById(R.id.imgF4yellow);
        imgF4yellow.setVisibility(visibility[F4][YELLOW]);
        ImageView imgF4green = (ImageView) v.findViewById(R.id.imgF4green);
        imgF4green.setVisibility(visibility[F4][GREEN]);

        ImageView imgT7red = (ImageView) v.findViewById(R.id.imgT7red);
        imgT7red.setVisibility(visibility[T7][RED]);
        ImageView imgT7yellow = (ImageView) v.findViewById(R.id.imgT7yellow);
        imgT7yellow.setVisibility(visibility[T7][YELLOW]);
        ImageView imgT7green = (ImageView) v.findViewById(R.id.imgT7green);
        imgT7green.setVisibility(visibility[T7][GREEN]);

        ImageView imgT8red = (ImageView) v.findViewById(R.id.imgT8red);
        imgT8red.setVisibility(visibility[T8][RED]);
        ImageView imgT8yellow = (ImageView) v.findViewById(R.id.imgT8yellow);
        imgT8yellow.setVisibility(visibility[T8][YELLOW]);
        ImageView imgT8green = (ImageView) v.findViewById(R.id.imgT8green);
        imgT8green.setVisibility(visibility[T8][GREEN]);

        ImageView imgPZred = (ImageView) v.findViewById(R.id.imgPZred);
        imgPZred.setVisibility(visibility[PZ][RED]);
        ImageView imgPZyellow = (ImageView) v.findViewById(R.id.imgPZyellow);
        imgPZyellow.setVisibility(visibility[PZ][YELLOW]);
        ImageView imgPZgreen = (ImageView) v.findViewById(R.id.imgPZgreen);
        imgPZgreen.setVisibility(visibility[PZ][GREEN]);
    }

    public static void printOutput(int[] contactQuality) {
        System.out.print("[");
        for (int i = 0; i < contactQuality.length; i++) {
            System.out.print(contactQuality[i] + " ");
        }
        System.out.println("]");
    }

}
