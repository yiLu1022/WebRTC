package com.netatmo.mbidon.pocwebrtc.helper;

import com.google.gson.JsonObject;
import com.netatmo.logger.Logger;
import com.netatmo.mbidon.pocwebrtc.data.Candidate;
import com.netatmo.mbidon.pocwebrtc.data.RtcMessage;
import com.netatmo.mbidon.pocwebrtc.data.Sdp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class providing tools to handle JSON data for WebRTC
 */
public final class RtcDataHelper {

    private RtcDataHelper() { }

    /**
     *
     *  {
            "type": "offer | answer | candidate",
            "candidate": {
                 "sdp_m_line_index": ... (Integer),
                 "candidate": ... (String)
             },
             "sdp": {
                 "type": "vod | live | call",
                 "video_id": ... (UUID),
                 "sdp": ... (SDP String)
            },
        }
     */
    public static RtcMessage parseRtcMessage(String jsonString) {

        Logger.i("ParsingSDP : " + jsonString);

        try {
            JSONObject rtcJson = new JSONObject(jsonString);

            RtcMessage.Type type = parseType(rtcJson.getString("type"));

            Candidate candidate = null;
            if (rtcJson.has("candidate")) {
                candidate = parseCandidate(rtcJson.getJSONObject("candidate"));
            }
            Sdp sdp = null;
            if (rtcJson.has("sdp")) {
                sdp = parseSdp(rtcJson.getJSONObject("sdp"));
            }

            return new RtcMessage(
                    type,
                    candidate,
                    sdp
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Sdp parseSdp(JSONObject body) {
        return new Sdp(
                parseJsonStringOrNull(body,"type"),
                parseJsonStringOrNull(body,"video_id"),
                parseJsonStringOrNull(body,"body")
        );
    }

    private static Candidate parseCandidate(JSONObject body) {
        return new Candidate(
                parseJsonIntegerOrNull(body,"sdp_m_line_index"),
                parseJsonStringOrNull(body,"candidate")
        );
    }

    private static RtcMessage.Type parseType(String type) {
        switch (type) {
            case "Answer":
                return RtcMessage.Type.ANSWER;
            case "Candidate":
                return RtcMessage.Type.CANDIDATE;
            case "Offer":
            default:
                return RtcMessage.Type.OFFER;
        }
    }

    private static String parseJsonStringOrNull(JSONObject obj, String key) {
        try {
            if (obj.has(key)) {
                return obj.getString(key);
            } else {
                return null;
            }
        } catch (Exception e) {
            Logger.e(e);
        }
        return null;
    }

    private static Integer parseJsonIntegerOrNull(JSONObject obj, String key) {
        try {
            if (obj.has(key)) {
                return obj.getInt(key);
            } else {
                return null;
            }
        } catch (Exception e) {
            Logger.e(e);
        }
        return null;
    }
}
