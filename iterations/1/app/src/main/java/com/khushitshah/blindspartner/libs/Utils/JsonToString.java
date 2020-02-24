package com.khushitshah.blindspartner.libs.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonToString {

    public static String jsonToString(JSONArray array, float focal_length, int width, int height) {
        StringBuilder op = new StringBuilder();

        if (array.length() <= 0) {
            op.append("Can't Detect any object in front of you.");
            return op.toString();
        } else if (array.length() == 1) {
            try {
                JSONObject cur = array.getJSONObject(0);

                int distanceMeter = (int) GetDistanceOfObject.distanceToObject(focal_length, cur.getInt("w"), cur.getInt("h"), 500, 1000, width, height, 5500);

                String name = ((JSONObject) array.get(0)).getString("name");
                op.append("There is a ").append(name).append(" ").append(distanceMeter).append(" meters away in front of you");
                return op.toString();
            } catch (JSONException e) {
                op.append("Error in processing JSON");
                return op.toString();
            }
        } else {
            op.append("There is a ");
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject cur = (JSONObject) array.get(i);

                    int distanceMeter = (int) GetDistanceOfObject.distanceToObject(focal_length, cur.getInt("w"), cur.getInt("h"), 1500, 1300, width, height, 5500);

                    if (i == array.length() - 1) {
                        op.append(" and ");
                    }
                    op.append(cur.getString("name")).append(" ").append(distanceMeter).append(" meters away in front of you, ");

//                    if (objectRecord.containsKey(cur.getString("name"))) {
//                        objectRecord.put(cur.getString("name"), objectRecord.get(cur.getString("name")) + 1);
////                        objectRecord.replace(cur.getString("name"), objectRecord.get(cur.getString("name")) + 1);
//                    } else {
//                        objectRecord.put(cur.getString("name"), 1);
//                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
//            StringBuilder temp = new StringBuilder();
//            for (String key : objectRecord.keySet()) {
//                int count = objectRecord.get(key);
//                if (count > 1) {
//                    isOrAre = " are ";
//                    temp = new StringBuilder().append(count).append(" ").append(key).append(" ").append(temp);
//                } else {
//                    temp.append(" ").append(key).append(", ");
//                }
//            }
//            op.append(isOrAre).append(" ").append(temp);
            return op.toString();
        }
    }
}
