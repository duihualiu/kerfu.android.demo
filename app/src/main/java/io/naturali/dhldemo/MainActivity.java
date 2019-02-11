package io.naturali.dhldemo;

import android.Manifest;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import io.naturali.common.data.bean.EntityRequest;
import io.naturali.common.data.bean.MessageRequest;
import io.naturali.common.data.bean.MessageResult;
import io.naturali.common.data.bean.QueryError;
import io.naturali.common.data.bean.UserInfo;
import io.naturali.common.sdk.MessageManager;
import io.naturali.common.sdk.callback.MessageListener;
import io.naturali.speech.SpeechListener;
import io.naturali.speech.SpeechRecognizerWrapper;
import io.naturali.speech.model.SpeechExtras;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private Button mButton;
    private RecyclerView mRecyclerView;
    private MessageAdapter mMessageAdapter;
    private String[] PERMISSIONS = new String[]{
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_CALENDAR};

    private String mUserId = "aaa";
    private String mAgentId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            requestPermissions(PERMISSIONS, 101);
        }
        MessageManager.init(this);
        MessageManager.initUserInfo(new UserInfo(mUserId, "bbb"));
        SpeechRecognizerWrapper.getInstance().init(this.getApplicationContext());
        mTextView = findViewById(R.id.input_text);
        mButton = findViewById(R.id.input_button);
        mRecyclerView = findViewById(R.id.recyclerView);
        mMessageAdapter = new MessageAdapter(new ArrayList<MessageResult>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mMessageAdapter);

        final MessageManager manager = new MessageManager(mAgentId);
        manager.setListener(new MessageListener() {
            @Override
            public void onReceive(MessageResult messageResult) {
                Log.e("PP", "onReceive 1 : " + new Gson().toJson(messageResult));
            }

            @Override
            public void onReceive(List<MessageResult> list) {
                Log.e("PP", "onReceive 2 : " + new Gson().toJson(list));
                mMessageAdapter.replace(list);
            }

            @Override
            public void onError(QueryError error) {

            }
        });

        mButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("CC", "onTouch : " + MotionEvent.actionToString(event.getAction()));

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {

                        final EntityRequest entityRequest = new EntityRequest();
                        entityRequest.setTypeName("NaturaliTest.动态实体测试");
                        Map<String, List<String>> entityValues = new HashMap<>();
                        entityValues.put("李明", new ArrayList<String>());
                        entityRequest.setValues(entityValues);

                        Bundle bundle = new SpeechExtras.Builder()
                            .agentId(mAgentId)
                            .userId(mUserId)
                            .forceHandleManually(false)
                            .oneShot(false)
                            .addEntityRequest(entityRequest)
                            .build();

                        SpeechRecognizerWrapper.getInstance().start(new SpeechListener() {
                            @Override
                            public void onReadyForSpeech(Bundle bundle) {
                                Log.e("CC", "onReadyForSpeech : ");

                            }

                            @Override
                            public void onRmsChanged(float v) {
                                Log.e("CC", "onRmsChanged : " + v);

                            }

                            @Override
                            public void onBeginningOfSpeech() {
                                Log.e("CC", "onBeginningOfSpeech : ");

                            }

                            @Override
                            public void onBufferReceived(byte[] bytes) {

                            }

                            @Override
                            public void onEndOfSpeech() {
                                Log.e("CC", "onEndOfSpeech : ");

                            }

                            @Override
                            public void onResults(Bundle bundle) {
                                String text = bundle
                                    .getString(SpeechRecognizer.RESULTS_RECOGNITION);
                                mTextView.setText(text);
                                MessageRequest request = new MessageRequest.Builder()
                                    .agentId(mAgentId)
                                    .query(text)
                                    .userId(mUserId)
                                    .addEntityRequest(entityRequest)
                                    .build();
                                manager.sendMessage(request);
                            }

                            @Override
                            public void onPartialResults(Bundle bundle) {
                                String text = bundle
                                    .getString(SpeechRecognizer.RESULTS_RECOGNITION);
                                Log.e("CC", "onPartialResults : " + text);
                                mTextView.setText(text);
                            }

                            @Override
                            public void onError(String s, int i) {

                            }

                            @Override
                            public void onEvent(int i, Bundle bundle) {

                            }
                        }, bundle);
                    }
                    break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        SpeechRecognizerWrapper.getInstance().stop();
                        break;
                }
                return false;
            }
        });
    }
}
