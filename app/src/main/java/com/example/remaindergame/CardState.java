package com.example.remaindergame;

public class CardState {
    private int imageId;
    private boolean flipped;

    public CardState() {
        // Default constructor for Firebase
    }

    public CardState(int imageId, boolean flipped) {
        this.imageId = imageId;
        this.flipped = flipped;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }
}

