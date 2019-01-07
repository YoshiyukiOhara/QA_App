package jp.techacademy.yoshiyuki.oohara.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private int mGenre = 0;
    private FirebaseUser user = null;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    //private ArrayList<Question> mFavoriteArrayList;
    private QuestionsListAdapter mAdapter;

    private DatabaseReference mUesrRef;
    private DatabaseReference mContentRef;

    private ArrayList<String> listA;
    private ArrayList<String> listB;
    private ArrayList<String> listC;

    FloatingActionButton fab;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
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

            //Log.d("DEBUG_TEST", String.valueOf(mGenre));
            //Log.d("DEBUG_TEST_k", String.valueOf(dataSnapshot.getKey()));
            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
            //Log.d("DEBUG_TEST", String.valueOf(dataSnapshot.getKey()));
            if (mGenre == 5) {
                for (String a : listA) {
                    Log.d("DEBUG_TEST_a", String.valueOf(a));
                    Log.d("DEBUG_TEST_k", String.valueOf(dataSnapshot.getKey()));

                    if (dataSnapshot.getKey().equals(a)) {

                        mQuestionArrayList.add(question);
                        Log.d("DEBUG_TEST", "mQuestionArrayListに追加成功");
                    } else {
                        Log.d("DEBUG_TEST", "mQuestionArrayListに追加失敗");
                    }
                }
            } else {
                Log.d("DEBUG_TEST", "5以外");
                mQuestionArrayList.add(question);
            }
            //Log.d("DEBUG_TEST_QList", String.valueOf(mQuestionArrayList));


            /*
            mQuestionArrayList.add(question);
            //Log.d("DEBUG_TEST", "mQuestionArrayList:" + String.valueOf(mQuestionArrayList));
            for (int i = 0 ; i < mQuestionArrayList.size() ; i++){
                Question country = mQuestionArrayList.get(i);
                Log.d("DEBUG_TEST", "mQuestionArrayList:" + String.valueOf(country));
            }

            Log.d("DEBUG_TEST", String.valueOf(dataSnapshot.getKey()));
            if (mGenre == 5) {
                for (String a: listA) {
                    if (!(dataSnapshot.getKey().equals(a))) {

                        int index = mQuestionArrayList.indexOf(a);
                        Log.d("DEBUG_TEST", String.valueOf(index));
                        //mQuestionArrayList.remove(index);
                    }
                }
            }*/

            //Log.d("DEBUG_TEST_A_V", String.valueOf(dataSnapshot.getValue()));
            //Log.d("DEBUG_TEST_A_K", String.valueOf(dataSnapshot.getKey()));
            //Log.d("DEBUG_TEST_A_QList", String.valueOf(mQuestionArrayList));
            mAdapter.notifyDataSetChanged();
            Log.d("DEBUG_TEST", "onChildAdded");
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
                    //Log.d("DEBUG_TEST_C", String.valueOf(dataSnapshot.getValue()));
                    //Log.d("DEBUG_TEST_C_QList", String.valueOf(mQuestionArrayList));

                    mAdapter.notifyDataSetChanged();
                    Log.d("DEBUG_TEST", "onChildChanged");
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
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
                if (mGenre == 0) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // ジャンルを渡して質問作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    startActivity(intent);
                }
            }
        });

        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
                //if (mGenre != 5) {
                    Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                    intent.putExtra("question", mQuestionArrayList.get(position));
                    startActivity(intent);
                //}
            }
        });

        listA = new ArrayList<String>();
        listB = new ArrayList<String>();
        listC = new ArrayList<String>();
        //mFavoriteArrayList = new ArrayList<Question>();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 1:趣味を既定の選択とする
        if(mGenre == 0) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            onNavigationItemSelected(navigationView.getMenu().getItem(0));
        }

        // ログインしていなければナビゲーションアイテム「お気に入り」を非表示
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        MenuItem navFavorite = menu.findItem(R.id.nav_favorite);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            navFavorite.setVisible(false);
        } else {
            navFavorite.setVisible(true);
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        user = FirebaseAuth.getInstance().getCurrentUser();

        int id = item.getItemId();

        fab.show();

        if (id == R.id.nav_hobby) {
            mToolbar.setTitle("趣味");
            mGenre = 1;
        } else if (id == R.id.nav_life) {
            mToolbar.setTitle("生活");
            mGenre = 2;
        } else if (id == R.id.nav_health) {
            mToolbar.setTitle("健康");
            mGenre = 3;
        } else if (id == R.id.nav_compter) {
            mToolbar.setTitle("コンピューター");
            mGenre = 4;
        } else if (id == R.id.nav_favorite) {
            mToolbar.setTitle("お気に入り");
            mGenre = 5;
            fab.hide();
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        //Log.d("DEBUG_TEST", String.valueOf(mQuestionArrayList));

        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear();
        mAdapter.setQuestionArrayList(mQuestionArrayList);

        //mFavoriteArrayList.clear();
        //mAdapter.setQuestionArrayList(mFavoriteArrayList);

        mListView.setAdapter(mAdapter);


        // 選択したジャンルにリスナーを登録する
        if (mGenreRef != null) {
            mGenreRef.removeEventListener(mEventListener);
        }
        mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));





        Log.d("DEBUG_TEST_QList", String.valueOf(mQuestionArrayList));

        if (mGenre == 5) {
            listA.clear();

            mUesrRef = mDatabaseReference.child(Const.FavoritesPATH).child(user.getUid());
            //Log.d("DEBUG_TEST_A", String.valueOf(mUesrRef));

            mUesrRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        listA.add(postSnapshot.getKey());
                        Log.d("DEBUG_TEST_listA", String.valueOf(listA));
                    }

                    for (int i = 1; i < 5; i++) {
                        //mGenre = i;
                        mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(i));
                        Log.d("DEBUG_TEST", String.valueOf(mGenreRef));
                        mGenreRef.addChildEventListener(mEventListener);
                    }
                }
                @Override
                public void onCancelled(DatabaseError firebaseError) {
                }
            });


        } else {
            mGenreRef.addChildEventListener(mEventListener);
        }
        /*
        for (int i = 1; i < 5; i++) {
            mGenre = i;
            DatabaseReference mContentRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
            Log.d("DEBUG_TEST", String.valueOf(mUesrRef));
            mContentRef.addChildEventListener(mEventListener);
        }
        //DatabaseReference mContentRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
        //mUesrRef = mDatabaseReference.child(Const.FavoritesPATH).child(user.getUid());
        */
        //Log.d("DEBUG_TEST", String.valueOf(mGenreRef));

        Log.d("DEBUG_TEST", String.valueOf(mEventListener));
        //mGenreRef.addChildEventListener(mEventListener);

        return true;
    }
}
