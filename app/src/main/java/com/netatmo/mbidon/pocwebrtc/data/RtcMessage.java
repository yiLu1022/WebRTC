package com.netatmo.mbidon.pocwebrtc.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mbidon on 21/03/18.
 */
public class RtcMessage {

    public enum Type {
        OFFER,
        ANSWER,
        CANDIDATE,
    }

    public Type type;
    public Candidate candidate;
    public Sdp sdp;

    public RtcMessage(Type type, Candidate candidate, Sdp sdp) {
        this.type = type;
        this.candidate = candidate;
        this.sdp = sdp;
    }

    @Override
    public String toString() {
        return String.format(
                "RtcMessage[\ntype=%s,\ncandidate=%s,\nsdp=%s]",
                type,
                candidate,
                sdp
                );
    }
}
