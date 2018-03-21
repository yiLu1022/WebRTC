package com.netatmo.mbidon.pocwebrtc.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mbidon on 21/03/18.
 */
public class Sdp implements Parcelable {

    public String type;
    public String videoId;
    public String body;

    public Sdp(Parcel in) {
        String[] data = new String[3];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.type = data[0];
        this.videoId = data[1];
        this.body = data[2];
    }

    public static final Creator<Sdp> CREATOR = new Creator<Sdp>() {
        @Override
        public Sdp createFromParcel(Parcel in) {
            return new Sdp(in);
        }

        @Override
        public Sdp[] newArray(int size) {
            return new Sdp[size];
        }
    };

    public Sdp(String type, String videoId, String body) {
        this.type = type;
        this.videoId = videoId;
        this.body = body;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeStringArray(new String[] {
                this.type,
                this.videoId,
                this.body
        });
    }

    @Override
    public String toString() {
        return String.format(
                "Sdp[type=%s, videoId=%s, body=%s]",
                type,
                videoId,
                body
        );
    }
}
