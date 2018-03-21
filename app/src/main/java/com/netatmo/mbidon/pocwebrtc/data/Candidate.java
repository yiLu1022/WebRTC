package com.netatmo.mbidon.pocwebrtc.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mbidon on 21/03/18.
 */
public class Candidate implements Parcelable {

    public Integer index;
    public String candidate;

    public Candidate(Parcel in) {
        String[] data = new String[2];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.index = Integer.valueOf(data[0]);
        this.candidate = data[1];
    }

    public static final Creator<Candidate> CREATOR = new Creator<Candidate>() {
        @Override
        public Candidate createFromParcel(Parcel in) {
            return new Candidate(in);
        }

        @Override
        public Candidate[] newArray(int size) {
            return new Candidate[size];
        }
    };

    public Candidate(Integer index, String candidate) {
        this.index = index;
        this.candidate = candidate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeStringArray(new String[] {
                String.valueOf(this.index),
                this.candidate
        });
    }

    @Override
    public String toString() {
        return String.format(
                "Candidate[index=%s, candidate=%s]",
                index,
                candidate
        );
    }
}
