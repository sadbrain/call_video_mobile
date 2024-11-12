package com.example.callvideowithjavawebrtc.repository;

import static org.webrtc.ContextUtils.getApplicationContext;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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

public class MainRepository implements WebRTCClient.Listener {

    private static volatile MainRepository instance;

    private final FirebaseClient firebaseClient;
    private WebRTCClient webRTCClient;
    private String currentUsername;
    private SurfaceViewRenderer remoteView;
    private String target;
    private final Gson gson = new Gson();
    public Listener listener;

    // Private constructor for Singleton
    private MainRepository() {
        this.firebaseClient = new FirebaseClient();
    }

    public static synchronized MainRepository getInstance() {
        if (instance == null) {
            instance = new MainRepository();
        }
        return instance;
    }

    // Login and WebRTC initialization
    public void login(String username, Context context, SuccessCallBack callBack) {
        firebaseClient.login(username, () -> {
            updateCurrentUsername(username);
            initializeWebRTCClient(context, username);
            callBack.onSuccess();
        });
    }

    private void updateCurrentUsername(String username) {
        this.currentUsername = username;
    }

    private void initializeWebRTCClient(Context context, String username) {
        this.webRTCClient = new WebRTCClient(context, new MyPeerConnectionObserver() {
            @Override
            public void onAddStream(MediaStream mediaStream) {
                if (remoteView != null && !mediaStream.videoTracks.isEmpty()) {
                    mediaStream.videoTracks.get(0).addSink(remoteView);
                }
            }

            @Override
            public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    Log.d("WebRTC", "Connected");
                    if (listener != null) listener.webrtcConnected();
                } else if (newState == PeerConnection.PeerConnectionState.CLOSED ||
                        newState == PeerConnection.PeerConnectionState.DISCONNECTED) {
                    Log.d("WebRTC", "Connection closed or disconnected");
                    if (listener != null) listener.webrtcClosed();
                }
            }


            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                webRTCClient.sendIceCandidate(iceCandidate, target);
            }
        }, username);
        webRTCClient.listener = this;
    }

    public void initLocalView(SurfaceViewRenderer view) {
        webRTCClient.initLocalSurfaceView(view);
    }

    public void initRemoteView(SurfaceViewRenderer view) {
        webRTCClient.initRemoteSurfaceView(view);
        this.remoteView = view;
    }

    public void startCall(String target) {
        this.target = target;
        webRTCClient.call(target);
    }

    public void switchCamera() {
        webRTCClient.switchCamera();
    }

    public void toggleAudio(boolean shouldBeMuted) {
        webRTCClient.toggleAudio(shouldBeMuted);
    }

    public void toggleVideo(boolean shouldBeMuted) {
        webRTCClient.toggleVideo(shouldBeMuted);
    }

    public void sendCallRequest(String target, ErrorCallBack errorCallBack) {
        try {
            firebaseClient.sendMessageToOtherUser(
                    new DataModel(target, currentUsername, "Call request", DataModelType.StartCall),
                    errorCallBack
            );
        } catch (Exception e) {
            Log.e("MainRepository", "Error sending call request", e);
        }
    }


    public void endCall() {
        webRTCClient.closeConnection();
    }

    public void subscribeForLatestEvent(NewEventCallBack callBack) {
        firebaseClient.observeIncomingLatestEvent(model -> {
            switch (model.getType()) {
                case Offer:
                    handleOfferEvent(model);
                    break;
                case Answer:
                    handleAnswerEvent(model);
                    break;
                case IceCandidate:
                    handleIceCandidateEvent(model);
                    break;
                case StartCall:
                    this.target = model.getSender();
                    callBack.onNewEventReceived(model);
                    break;
            }
        });
    }

    private void handleOfferEvent(DataModel model) {
        this.target = model.getSender();
        webRTCClient.onRemoteSessionReceived(new SessionDescription(
                SessionDescription.Type.OFFER, model.getData()
        ));
        webRTCClient.answer(model.getSender());
    }

    private void handleAnswerEvent(DataModel model) {
        this.target = model.getSender();
        webRTCClient.onRemoteSessionReceived(new SessionDescription(
                SessionDescription.Type.ANSWER, model.getData()
        ));
    }

    private void handleIceCandidateEvent(DataModel model) {
        try {
            IceCandidate candidate = gson.fromJson(model.getData(), IceCandidate.class);
            webRTCClient.addIceCandidate(candidate);
        } catch (Exception e) {
            Log.e("MainRepository", "Error parsing IceCandidate", e);
        }
    }

    @Override
    public void onTransferDataToOtherPeer(DataModel model) {
        firebaseClient.sendMessageToOtherUser(model, () -> {});
    }

    public interface Listener {
        void webrtcConnected();
        void webrtcClosed();
    }
}
