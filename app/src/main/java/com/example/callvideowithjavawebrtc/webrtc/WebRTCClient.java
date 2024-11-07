package com.example.callvideowithjavawebrtc.webrtc;

import android.content.Context;

import com.example.callvideowithjavawebrtc.utils.DataModel;
import com.example.callvideowithjavawebrtc.utils.DataModelType;
import com.google.gson.Gson;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class WebRTCClient {
    private final Context context;
    private final String username;
    private EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();

    //initializing peer connection section
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private List<PeerConnection.IceServer> iceServer = new ArrayList<>();

    //initializing ui like surface view renderers
    private CameraVideoCapturer videoCapturer;
    private VideoSource localVideoSource;
    private AudioSource localAudioSource;
    private String localTrackId = "local_track";
    private String localStreamId = "local_stream";
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private MediaStream localStream;

    //negotiation section like call and answer
    private MediaConstraints mediaConstraints = new MediaConstraints();
    private  Listener listener;
    private final Gson gson = new Gson();

    public WebRTCClient(Context context, PeerConnection.Observer observer, String username) {
        this.context = context;
        this.username = username;
        //initializing peer connection section
        initPeerConnectionFactory();
        peerConnectionFactory = createPeerConnectionFactory();
        iceServer.add(PeerConnection.IceServer.builder("turn:a.relay.metered.ca:443?transport=tcp")
                .setUsername("83eebabf8b4cce9d5dbcb649")
                .setPassword("2D7JvfkOQtBdYW3R").createIceServer());
        peerConnection = createPeerConnection(observer);
        //initializing ui like surface view renderers
        localAudioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localVideoSource = peerConnectionFactory.createVideoSource(false);
        //negotiation section like call and answer
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
    }

    //initializing peer connection section
    private void initPeerConnectionFactory() {
        PeerConnectionFactory.InitializationOptions options =
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .setFieldTrials("CallVideoWithJavaWebRtc-H264HighProfile/Enabled")
                        .setEnableInternalTracer(true).createInitializationOptions();
        PeerConnectionFactory.initialize(options);
    }

    private PeerConnectionFactory createPeerConnectionFactory() {
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.disableEncryption = false;
        options.disableNetworkMonitor = false;
        return PeerConnectionFactory.builder()
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBaseContext, true, true))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBaseContext))
                .setOptions(options).createPeerConnectionFactory();
    }

    private PeerConnection createPeerConnection(PeerConnection.Observer observer) {
        return peerConnectionFactory.createPeerConnection(iceServer, observer);
    }

    //initializing ui like surface view renderers
    public void initSurfaceViewRendered(SurfaceViewRenderer viewRenderer) {
        viewRenderer.setEnableHardwareScaler(true);
        viewRenderer.setMirror(true);
        viewRenderer.init(eglBaseContext, null);
    }

    public void initLocalSurfaceView(SurfaceViewRenderer view) {
        initSurfaceViewRendered(view);
        startLocalVideoStreaming(view);
    }

    private void startLocalVideoStreaming(SurfaceViewRenderer view) {
        SurfaceTextureHelper helper = SurfaceTextureHelper.create(
                Thread.currentThread().getName(), eglBaseContext);
        videoCapturer = getVideoCapturer();
        videoCapturer.initialize(helper, context, localVideoSource.getCapturerObserver());
        videoCapturer.startCapture(480, 360, 15);
        localVideoTrack = peerConnectionFactory.createVideoTrack(
                localTrackId + "_video", localVideoSource
        );
        localVideoTrack.addSink(view);
        localAudioTrack = peerConnectionFactory.createAudioTrack(
                localTrackId + "_audio", localAudioSource
        );
        localStream = peerConnectionFactory.createLocalMediaStream(localStreamId);
        localStream.addTrack(localVideoTrack);
        localStream.addTrack(localAudioTrack);
        peerConnection.addStream(localStream);
    }

    public CameraVideoCapturer getVideoCapturer() {
        Camera2Enumerator enumerator = new Camera2Enumerator(context);
        String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null);
            }
            throw new IllegalStateException("front facing camera not found");

        }
        return null;
    }
    public void initRemoteSurfaceView(SurfaceViewRenderer view){
        initSurfaceViewRendered(view);
    }

    //negotiation section like call and answer
    public void call(String target){
        try{
            peerConnection.createOffer(new MySdpObserver(){
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    super.onCreateSuccess(sessionDescription);
                    peerConnection.setLocalDescription(new MySdpObserver(){
                        @Override
                        public void onSetSuccess() {
                            super.onSetSuccess();
                            //its time to transfer this sdp to the other peer
                            if(listener != null){
                                listener.onTransferDataToOtherPeer(new DataModel(
                                        target,
                                        username,
                                        sessionDescription.description,
                                        DataModelType.Offer
                                ));
                            }
                        }
                    }, sessionDescription);
                }
            }, mediaConstraints);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void answer(String target){
        try{
            peerConnection.createOffer(new MySdpObserver(){
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    super.onCreateSuccess(sessionDescription);
                    peerConnection.setLocalDescription(new MySdpObserver(){
                        @Override
                        public void onSetSuccess() {
                            super.onSetSuccess();
                            //its time to transfer this sdp to the other peer
                            if(listener != null){
                                listener.onTransferDataToOtherPeer(new DataModel(
                                        target,
                                        username,
                                        sessionDescription.description,
                                        DataModelType.Answer
                                ));
                            }
                        }
                    }, sessionDescription);
                }
            }, mediaConstraints);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onRemoteSessionReceived(SessionDescription sessionDescription){
        peerConnection.setRemoteDescription(new MySdpObserver(), sessionDescription);
    }
    public void addIceCandidate(IceCandidate iceCandidate){
        peerConnection.addIceCandidate(iceCandidate);
    }
    public void sendIceCandidate(IceCandidate iceCandidate, String target){
        addIceCandidate(iceCandidate);
        if(listener!=null){
            listener.onTransferDataToOtherPeer(new DataModel(
                    target,
                    username,
                    gson.toJson(iceCandidate),
                    DataModelType.IceCandidate
            ));
        }
    }
    public void switchCamera() {
        videoCapturer.switchCamera(null);
    }

    public void toggleVideo(Boolean shouldBeMuted){
        localVideoTrack.setEnabled(shouldBeMuted);
    }

    public void toggleAudio(Boolean shouldBeMuted){
        localAudioTrack.setEnabled(shouldBeMuted);
    }

    public void closeConnection(){
        try{

            localVideoTrack.dispose();
            videoCapturer.stopCapture();
            videoCapturer.dispose();
            peerConnection.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public interface Listener{
        void onTransferDataToOtherPeer(DataModel model);
    }
}