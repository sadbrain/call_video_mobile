package com.example.callvideowithjavawebrtc.webrtc;

import org.webrtc.CandidatePairChangeEvent;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;

public class MyPeerConnectionObserver implements PeerConnection.Observer {
    @Override
    public void onStandardizedIceConnectionChange(PeerConnection.IceConnectionState newState) {
        PeerConnection.Observer.super.onStandardizedIceConnectionChange(newState);
    }

    @Override
    public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
        PeerConnection.Observer.super.onConnectionChange(newState);
    }

    @Override
    public void onSelectedCandidatePairChanged(CandidatePairChangeEvent event) {
        PeerConnection.Observer.super.onSelectedCandidatePairChanged(event);
    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {
        PeerConnection.Observer.super.onTrack(transceiver);
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {

    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    @Override
    public void onAddStream(MediaStream mediaStream) {

    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {

    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {

    }

    @Override
    public void onRenegotiationNeeded() {

    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

    }
}
