package com.example.callvideowithjavawebrtc.repository;

import android.content.Context;

import com.example.callvideowithjavawebrtc.remote.FirebaseClient;
import com.example.callvideowithjavawebrtc.utils.DataModel;
import com.example.callvideowithjavawebrtc.utils.DataModelType;
import com.example.callvideowithjavawebrtc.utils.ErrorCallBack;
import com.example.callvideowithjavawebrtc.utils.NewEventCallBack;
import com.example.callvideowithjavawebrtc.utils.SuccessCallBack;
import com.example.callvideowithjavawebrtc.webrtc.MyPeerConnectionObserver;
import com.example.callvideowithjavawebrtc.webrtc.WebRTCClient;
import com.google.gson.Gson;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

public class MainRepository implements WebRTCClient.Listener{
    private final Gson gson = new Gson();

    public Listener listener;
    private final FirebaseClient firebaseClient;
    private static MainRepository instance;
    private String currentUsername;
    private WebRTCClient webRTCClient;
    private SurfaceViewRenderer remoteView;
    private String target;
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
    public void login(String username, Context context, SuccessCallBack callBack) {
        updateCurrentUsername(username);
        this.webRTCClient = new WebRTCClient(context, new MyPeerConnectionObserver(){
            @Override
            public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                super.onConnectionChange(newState);
                if(newState == PeerConnection.PeerConnectionState.CONNECTED
                    && listener!=null){
                    listener.webrtcConnected();
                }
                if(newState == PeerConnection.PeerConnectionState.CLOSED ||
                        newState == PeerConnection.PeerConnectionState.DISCONNECTED){
                    if(listener!=null){
                        listener.webrtcClosed();
                    }
                }
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                try {
                    mediaStream.videoTracks.get(0).addSink(remoteView);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                webRTCClient.sendIceCandidate(iceCandidate, target);
            }
        }, username);
        webRTCClient.listener = this;
        firebaseClient.login(username, ()->{callBack.onSuccess();});
    }
    public void startCall(String target){
        webRTCClient.call(target);
    }
    public void switchCamera(){
        webRTCClient.switchCamera();
    }
    public void toggleAudio(Boolean shouldBeMuted){
        webRTCClient.toggleAudio(shouldBeMuted);
    }
    public void toggleVideo(Boolean shouldBeMuted){
        webRTCClient.toggleVideo(shouldBeMuted);
    }
    public void initLocalView(SurfaceViewRenderer view){
        webRTCClient.initLocalSurfaceView(view);
    }
    public void initRemoteView(SurfaceViewRenderer view){
        webRTCClient.initRemoteSurfaceView(view);
        this.remoteView = view;
    }
    public void endCall(){
        webRTCClient.closeConnection();
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
                    this.target = model.getSender();
                    webRTCClient.onRemoteSessionReceived(new SessionDescription(
                            SessionDescription.Type.OFFER, model.getData()
                    ));
                    webRTCClient.answer(model.getSender());
                    break;
                case Answer:
                    this.target = model.getSender();
                    webRTCClient.onRemoteSessionReceived(new SessionDescription(
                            SessionDescription.Type.ANSWER, model.getData()
                    ));
                    break;
                case IceCandidate:
                    try {
                        IceCandidate candidate = gson.fromJson(model.getData(), IceCandidate.class);
                        webRTCClient.addIceCandidate(candidate);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case StartCall:
                    this.target = model.getSender();
                    callBack.onNewEventReceived(model);
                    break;
            }
        });

    }

    @Override
    public void onTransferDataToOtherPeer(DataModel model) {
        firebaseClient.sendMessageToOtherUser(model, ()->{});
    }
    public interface Listener{
        void webrtcConnected();
        void webrtcClosed();
    }
}
