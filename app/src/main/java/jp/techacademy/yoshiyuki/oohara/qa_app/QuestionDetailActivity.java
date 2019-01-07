package jp.techacademy.yoshiyuki.oohara.qa_app;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private Button mFavoriteButton;

    private DatabaseReference mAnswerRef;
    private DatabaseReference mDataBaseReference;
    private DatabaseReference mUserRef;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        mDataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = mDataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

        mFavoriteButton = (Button) findViewById(R.id.favoriteButton);
        // お気に入りボタンをタップした時の処理
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                mUserRef = mDataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());

                mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        if (snapshot.getValue() == null) {
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("genre", String.valueOf(mQuestion.getGenre()));
                            mUserRef.setValue(data);
                            mFavoriteButton.setBackgroundResource(R.drawable.btn_pressed);
                        } else {
                            mUserRef.removeValue();
                            mFavoriteButton.setBackgroundResource(R.drawable.btn);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFavoriteButton = (Button) findViewById(R.id.favoriteButton);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            mFavoriteButton.setVisibility(View.VISIBLE);

            mDataBaseReference = FirebaseDatabase.getInstance().getReference();
            mUserRef = mDataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());

            mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    // お気に入りボタンの状態変更
                    if (snapshot.getValue() == null) {
                        mFavoriteButton.setBackgroundResource(R.drawable.btn);
                        Log.d("DEBUG_TEST", "お気に入り登録されてない");
                    } else {
                        mFavoriteButton.setBackgroundResource(R.drawable.btn_pressed);
                        Log.d("DEBUG_TEST", "お気に入り登録されてる");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            // ログインしていなければお気に入りボタンを非表示
            mFavoriteButton.setVisibility(View.INVISIBLE);
        }
    }
}
