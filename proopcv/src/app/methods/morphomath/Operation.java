package app.methods.morphomath;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by C_Luc on 18/06/2017.
 */
public class Operation {


    public static void dilation(Mat src, Mat dst, Mat kernel) {
        Imgproc.dilate(src, dst, kernel);
    }

    public static void erode(Mat src, Mat dst, int elementSize, int elementShape) {
        Mat element = null;
    }
}