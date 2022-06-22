package tw.edu.pu.csim.mis2b.wcchen.snake;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.protobuf.Any;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Context context = this;
    private Button b1;
    private EditText t1;
    private ListView LV;
    private FirebaseFirestore db;
    private String x_last = "0";
    private String x_select = "1";

    public static ImageView img_swipe;
    public static Dialog dialogScore;
    private GameView gv;
    public static TextView txt_score, txt_best_score, txt_dialog_score, txt_dialog_best_score;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Constants.SCREEN_WIDTH = dm.widthPixels;
        Constants.SCREEN_HEIGHT = dm.heightPixels;
        setContentView(R.layout.activity_main);
        img_swipe = findViewById(R.id.img_swipe);
        gv = findViewById(R.id.gv);
        txt_score = findViewById(R.id.txt_score);
        txt_best_score = findViewById(R.id.txt_best_score);
        dialogScore();
        data_select();
    }

    private void data_select(){
        final List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        db.collection("users").orderBy("Score", Query.Direction.DESCENDING).limit(8)
        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document:task.getResult()){
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("Name", document.get("Name"));
                    item.put("Score", document.get("Score"));
                    items.add(item);
                    x_last = document.getId();
                }
                SimpleAdapter SA = new SimpleAdapter(context, items, R.layout.list, new String[]{"Name", "Score"}, new int[]{R.id.text2, R.id.text3});
                LV.setAdapter(SA);
            }
        });
    }

    private void dialogScore() {
        int bestScore = 0;
        SharedPreferences sp = this.getSharedPreferences("gamesetting", Context.MODE_PRIVATE);
        if(sp!=null){
            bestScore = sp.getInt("bestscore",0);
        }
        MainActivity.txt_best_score.setText(bestScore+"");
        dialogScore = new Dialog(this);
        dialogScore.setContentView(R.layout.dialog_start);
        txt_dialog_score = dialogScore.findViewById(R.id.txt_dialog_score);
        txt_dialog_best_score = dialogScore.findViewById(R.id.txt_dialog_best_score);
        txt_dialog_best_score.setText(bestScore + "");
        dialogScore.setCanceledOnTouchOutside(false);
        RelativeLayout rl_start = dialogScore.findViewById(R.id.rl_start);

        b1 = (Button) dialogScore.findViewById(R.id.btn_upload);
        LV = (ListView) dialogScore.findViewById(R.id.LV);
        t1 = (EditText) dialogScore.findViewById(R.id.et1);
        db = FirebaseFirestore.getInstance();

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> text_data = new HashMap<String, Object>();
                text_data.put("Name", t1.getText().toString());
                text_data.put("Score", Integer.valueOf(txt_dialog_score.getText().toString()));

                Integer x_id = Integer.valueOf(x_last) + 1;

                db.collection("users").document(String.valueOf(x_id)).set(text_data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("結果：", "新增成功!!!! ");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("結果：", "新增失敗!!!! ");
                    }
                });
                data_select();
            }
        });

        LV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView t1_s = (TextView) view.findViewById(R.id.text2);
                x_select = t1_s.getText().toString();
                TextView t2_s = (TextView) view.findViewById(R.id.text3);
                t1.setText(t2_s.getText().toString());
            }
        });
        data_select();

        rl_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_swipe.setVisibility(View.VISIBLE);
                gv.reset();
                dialogScore.dismiss();
            }
        });
        dialogScore.show();
    }
}