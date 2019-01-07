package jp.techacademy.yoshiyuki.oohara.qa_app;

import java.io.Serializable;

public class Favorite implements Serializable {
    private String mUid;
    private String mQuestionUid;

    public Favorite(String uid, String questionUid) {
        mUid = uid;
        mQuestionUid = questionUid;
    }

    public String getUid() {
        return mUid;
    }

    public String getQuestionUid() {
        return mQuestionUid;
    }
}
