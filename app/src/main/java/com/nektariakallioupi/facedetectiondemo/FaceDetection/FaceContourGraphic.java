package com.nektariakallioupi.facedetectiondemo.FaceDetection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;

import java.util.List;

/**
 * Graphic instance for rendering face contours graphic overlay view.
 */
public class FaceContourGraphic extends GraphicOverlay.Graphic {

    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private static final float ID_TEXT_SIZE = 70.0f;
    private static final float ID_Y_OFFSET = 80.0f;
    private static final float ID_X_OFFSET = -70.0f;

    private static final int[] COLOR_CHOICES = {
            Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.WHITE, Color.YELLOW
    };

    private final Paint facePositionPaint;
    private final Paint boxPaint;
    private final Paint idPaint;
    private final Paint facingYPaint;
    private final Paint facingXPaint;


    private static int currentColorIndex = 0;
    private volatile Face face;

    public FaceContourGraphic(GraphicOverlay overlay) {
        super(overlay);

        currentColorIndex = (currentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[currentColorIndex];

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        boxPaint = new Paint();
        boxPaint.setColor(COLOR_CHOICES[5]);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        idPaint = new Paint();
        idPaint.setColor(COLOR_CHOICES[5]);
        idPaint.setTextSize(ID_TEXT_SIZE);

        facingYPaint = new Paint();
        facingYPaint.setColor(COLOR_CHOICES[6]);
        facingYPaint.setTextSize(ID_TEXT_SIZE);

        facingXPaint = new Paint();
        facingXPaint.setColor(COLOR_CHOICES[2]);
        facingXPaint.setTextSize(ID_TEXT_SIZE);

    }

    /**
     * Updates the face instance from the detection of the most recent frame. Invalidates the relevant
     * portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face) {
        this.face = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {

        Face face = this.face;
        if (face == null) {
            return;
        }
        FaceUtils faceUtils = new FaceUtils(face);

        //  Draws a circle at the position of the detected face,with the face's track id below.
        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint);
        canvas.drawText("id: " + face.getTrackingId(), x + ID_X_OFFSET, y + ID_Y_OFFSET, idPaint);

        //  Draws a bounding box around the face.
        float left = translateX((float) face.getBoundingBox().left);
        float top = translateY((float) face.getBoundingBox().top);
        float right = translateX((float) face.getBoundingBox().right);
        float bottom = translateY((float) face.getBoundingBox().bottom);
        canvas.drawRect(left, top, right, bottom, boxPaint);

        // Draws a dotted contour on the most prominent detected face.
        List<FaceContour> contour = face.getAllContours();
        for (FaceContour faceContour : contour) {
            for (PointF point : faceContour.getPoints()) {
                float px = translateX(point.x);
                float py = translateY(point.y);
                canvas.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint);
            }
        }

        canvas.drawText("facingX: " + faceUtils.checkAxeXFacing(),right, bottom+ 80f, facingXPaint);
        canvas.drawText("facingY: " + faceUtils.checkAxeYFacing(), right, bottom+ 140f, facingYPaint);

    }
}