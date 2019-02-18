package io.naturali.dhldemo;

import android.Manifest;
import android.annotation.SuppressLint;
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
import io.naturali.common.data.bean.AttributeRequest;
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
    private String mTag = MainActivity.class.getSimpleName();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            requestPermissions(PERMISSIONS, 101);
        }

        // 初始化 SDK
        MessageManager.init(this);

        // 初始化用户信息
        MessageManager.initUserInfo(new UserInfo(mUserId, "bbb"));

        // 初始化语音识别模块
        SpeechRecognizerWrapper.getInstance().init(this.getApplicationContext());

        // 初始化 UI
        initView();

        // 创建一个MessageManager对象并设置监听
        final MessageManager manager = new MessageManager(mAgentId);
        manager.setListener(new MessageListener() {
            @Override
            public void onReceive(MessageResult messageResult) {
                Log.e(mTag, "onReceive message result : " + new Gson().toJson(messageResult));
            }

            @Override
            public void onReceive(List<MessageResult> list) {
                Log.e(mTag, "onReceive message result list : " + new Gson().toJson(list));
                mMessageAdapter.replace(list);
            }

            @Override
            public void onError(QueryError error) {

            }
        });

        mButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {

                        // 添加语音识别时的动态实体，用于增强语音识别准确度
                        final EntityRequest entityRequest = new EntityRequest();
                        entityRequest.setTypeName("NaturaliTest.动态实体测试");
                        Map<String, List<String>> entityValues = new HashMap<>();
                        entityValues.put("李明", new ArrayList<String>());
                        entityRequest.setValues(entityValues);

                        // 构建语音识别参数
                        Bundle bundle = new SpeechExtras.Builder()
                            .agentId(mAgentId)
                            .userId(mUserId)
                            .forceHandleManually(false)
                            .oneShot(false)
                            .addEntityRequest(entityRequest)
                            .build();

                        // 开始录音并进行识别
                        SpeechRecognizerWrapper.getInstance().start(new SpeechListener() {
                            @Override
                            public void onReadyForSpeech(Bundle bundle) {
                                Log.e(mTag, "onReadyForSpeech : ");

                            }

                            @Override
                            public void onRmsChanged(float v) {
                                Log.e(mTag, "onRmsChanged : " + v);

                            }

                            @Override
                            public void onBeginningOfSpeech() {
                                Log.e(mTag, "onBeginningOfSpeech : ");

                            }

                            @Override
                            public void onBufferReceived(byte[] bytes) {

                            }

                            @Override
                            public void onEndOfSpeech() {
                                Log.e(mTag, "onEndOfSpeech : ");

                            }

                            @Override
                            public void onResults(Bundle bundle) {
                                String text = bundle
                                    .getString(SpeechRecognizer.RESULTS_RECOGNITION);
                                mTextView.setText(text);

                                sendMessage(text, manager, entityRequest);
                            }

                            @Override
                            public void onPartialResults(Bundle bundle) {
                                String text = bundle
                                    .getString(SpeechRecognizer.RESULTS_RECOGNITION);
                                Log.e(mTag, "onPartialResults : " + text);
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

    private void initView() {
        mTextView = findViewById(R.id.input_text);
        mButton = findViewById(R.id.input_button);
        mRecyclerView = findViewById(R.id.recyclerView);
        mMessageAdapter = new MessageAdapter(new ArrayList<MessageResult>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mMessageAdapter);
    }

    private void sendMessage(String message, MessageManager manager, EntityRequest entityRequest) {

        // 创建全局属性
        AttributeRequest globalAttribute = new AttributeRequest();
        globalAttribute.setName("name");
        globalAttribute.setValue("value");

        // 创建本地属性
        AttributeRequest localAttribute = new AttributeRequest();
        localAttribute.setName("name");
        localAttribute.setValue("value");

        // 将语音识别结果构造成一个请求对象
        MessageRequest request = new MessageRequest.Builder()
            .agentId(mAgentId) // 当前服务的agent id
            .query(message) // 发送的文字信息
            .userId(mUserId) // 设置 userid
            .addEntityRequest(entityRequest) // 添加动态实体，多次调用可添加多个
            .addGlobalAttribute(globalAttribute) // 添加全局属性，可多次调用添加多个
            .addLocalAttribute(localAttribute) // 添加本地属性，可多次调用添加多个
            .intentName("intent name") // 上述 attribute 对应的 intent 名称
            .build();

        // 发送 message 请求
        manager.sendMessage(request);
    }
}
