package com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.Free_hand_cropper_updated;

/**
 * Created by seunk on 9/8/2018.
 */

public class CropModel {
    private float x;
    private float y;

    public CropModel(float y, float x) {
        this.y = y;
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }
}
