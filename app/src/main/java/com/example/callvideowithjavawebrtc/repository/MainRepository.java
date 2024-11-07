package com.example.callvideowithjavawebrtc.repository;

import com.example.callvideowithjavawebrtc.remote.FirebaseClient;
import com.example.callvideowithjavawebrtc.utils.DataModel;
import com.example.callvideowithjavawebrtc.utils.DataModelType;
import com.example.callvideowithjavawebrtc.utils.ErrorCallBack;
import com.example.callvideowithjavawebrtc.utils.NewEventCallBack;
import com.example.callvideowithjavawebrtc.utils.SuccessCallBack;

public class MainRepository {
    private FirebaseClient firebaseClient;
    private static MainRepository instance;
    private String currentUsername;
    private MainRepository() {
        firebaseClient = new FirebaseClient();
    }
    private void updateCurrentUsername(String username) {
        this.currentUsername = username;
    }
    public static MainRepository getInstance() {
        if (instance == null) {
            instance = new MainRepository();
        }
        return instance;
    }
    public void login(String username, SuccessCallBack callBack) {
        updateCurrentUsername(username);
        firebaseClient.login(username, ()->{callBack.onSuccess();});
    }
    public void sendCallRequest(String target, ErrorCallBack callBack){
        firebaseClient.sendMessageToOtherUser(
                new DataModel(target, currentUsername, null, DataModelType.StartCall),
                callBack
        );
    }
    public void subscribeForLatestEvent(NewEventCallBack callBack) {
        firebaseClient.observeIncomingLatestEvent(model->{
            switch (model.getType()){
                case Offer:
                    break;
                case Answer:
                    break;
                case IceCandidate:
                    break;
                case StartCall:
                    callBack.onNewEventReceived(model);
                    break;
            }
        });

    }}
