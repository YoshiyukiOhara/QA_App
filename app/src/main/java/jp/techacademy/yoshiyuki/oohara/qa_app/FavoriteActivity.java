package jp.techacademy.yoshiyuki.oohara.qa_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FavoriteActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFavoriteRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;

    private ChildEventListener mFavoriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d("DEBUG_TEST", String.valueOf(dataSnapshot));
            HashMap map = (HashMap) dataSnapshot.getValue();
            final String genre = (String) map.get("genre");

            // お気に入りしている質問情報から質問詳細を取得
            DatabaseReference ref = mDatabaseReference.child(Const.ContentsPATH).child(genre).child(dataSnapshot.getKey());
            Log.d("DEBUG_TEST", String.valueOf(ref));

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("DEBUG_TEST", String.valueOf(dataSnapshot));

                    HashMap map = (HashMap) dataSnapshot.getValue();
                    String title = (String) map.get("title");
                    String body = (String) map.get("body");
                    String name = (String) map.get("name");
                    String uid = (String) map.get("uid");
                    String imageString = (String) map.get("image");
                    byte[] bytes;
                    if (imageString != null) {
                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                    } else {
                        bytes = new byte[0];
                    }

                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            answerArrayList.add(answer);
                        }
                    }

                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), Integer.parseInt(genre), bytes, answerArrayList);
                    mQuestionArrayList.add(question);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question: mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
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
        setContentView(R.layout.activity_favorite);

        setTitle("お気に入り");

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        // 選択したジャンルにリスナーを登録する
        if (mFavoriteRef != null) {
            mFavoriteRef.removeEventListener(mFavoriteListener);
        }

        // ログイン済みのユーザーを取得する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // ログイン済みのユーザーのお気に入りしている質問情報を取得
        mFavoriteRef = mDatabaseReference.child(Const.FavoritesPATH).child(user.getUid());
        Log.d("DEBUG_TEST", String.valueOf(mFavoriteRef));
        mFavoriteRef.addChildEventListener(mFavoriteListener);
    }
}
