package com.example.remaindergame;

// מחלקת CardState מייצגת את המצב של קלף במשחק
public class CardState {
    private int imageId; // מזהה התמונה של הקלף (Resource ID)
    private boolean flipped; // האם הקלף במצב הפוך (true = גלוי, false = מוסתר)

    // בנאי ברירת מחדל (נדרש על ידי Firebase)
    public CardState() {
        // בנאי ריק לצורך תאימות לספריות כמו Firebase
    }

    // בנאי עם פרמטרים לאתחול המצב ההתחלתי של הקלף
    public CardState(int imageId, boolean flipped) {
        this.imageId = imageId; // מזהה התמונה של הקלף
        this.flipped = flipped; // המצב ההתחלתי של הקלף (גלוי או מוסתר)
    }

    // פונקציה המחזירה את מזהה התמונה של הקלף
    public int getImageId() {
        return imageId;
    }

    // פונקציה המגדירה את מזהה התמונה של הקלף
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    // פונקציה הבודקת האם הקלף במצב הפוך (גלוי)
    public boolean isFlipped() {
        return flipped;
    }

    // פונקציה המגדירה את המצב של הקלף (גלוי או מוסתר)
    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }
}
