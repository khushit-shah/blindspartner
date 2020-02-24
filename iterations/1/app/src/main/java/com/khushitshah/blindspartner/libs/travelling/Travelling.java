package com.khushitshah.blindspartner.libs.travelling;

import android.app.Activity;
import android.location.Location;

import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.GeocodedWaypointStatus;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.Arrays;

public class Travelling {
    private GeoApiContext apiContext;

    public Travelling() {
        apiContext = new GeoApiContext.Builder().apiKey("YOUR KEY HERE").build();
    }


    public void getAllModeTime(Location origin, String destination, TravellingInterface callback, Activity activity) {
        new Thread(() -> {
            try {
                DirectionsApiRequest request = new DirectionsApiRequest(apiContext);
                request.alternatives(false);
                request.destination(destination);
                request.origin(new LatLng(origin.getLatitude(), origin.getLongitude()));
                request.mode(TravelMode.WALKING);
                DirectionsResult result;
                try {
                    result = request.await();
                } catch (ApiException | InterruptedException | IOException e) {
                    result = null;
                }

                DirectionsApiRequest request2 = new DirectionsApiRequest(apiContext);
                request2.alternatives(false);
                request2.destination(destination);
                request2.origin(new LatLng(origin.getLatitude(), origin.getLongitude()));
                request2.mode(TravelMode.TRANSIT);
                DirectionsResult result2;
                try {
                    result2 = request2.await();
                } catch (ApiException | InterruptedException | IOException e) {
                    result2 = null;
                }

                int length = 0;

                String result1Duration = "";
                String result2Duration = "";

                if (result != null && result.geocodedWaypoints[0].geocoderStatus == GeocodedWaypointStatus.OK) {
                    length++;
                    result1Duration = result.routes[0].legs[0].duration.humanReadable;
                }

                if (result2 != null && result2.geocodedWaypoints[0].geocoderStatus == GeocodedWaypointStatus.OK) {
                    length++;
                    result2Duration = result2.routes[0].legs[0].duration.humanReadable;
                }

                String[][] results = new String[length][2];
                int count = 0;
                if (result != null && result.geocodedWaypoints[0].geocoderStatus == GeocodedWaypointStatus.OK) {
                    results[count][0] = "walking";
                    results[count][1] = result1Duration;
                    count++;
                }
                if (result2 != null && result2.geocodedWaypoints[0].geocoderStatus == GeocodedWaypointStatus.OK) {
                    results[count][0] = "transit";
                    results[count][1] = result2Duration;
                }

                DirectionsResult finalResult2 = result2;
                DirectionsResult finalResult1 = result;
                callback.getTravelMode(results, (modeChosed) -> {

                    if (modeChosed.equals("transit")) {
                        travelByTransit(finalResult2, callback, activity, 0, 0);
                    } else if (modeChosed.equals("walking")) {
                        travelByWalking(finalResult1, callback, activity);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                callback.speakUpdate("Can't find travelling information, Sorry for inconvinience!");
            }
        }).start();
    }

    private void travelByTransit(DirectionsResult finalResult, TravellingInterface callback, Activity activity, int stIndex, int subStepIndex) {

        DirectionsStep[] steps = finalResult.routes[0].legs[0].steps;
        if (stIndex >= steps.length) {
            callback.speakUpdate("You reached your destination!");
            return;
        }
        if (steps[stIndex].steps == null) {
            System.out.println("sub step index " + stIndex + " " + steps[stIndex].toString());
            System.out.println("Sub Steps null : " + steps[stIndex].toString());
            System.out.println("Sub Steps null : " + Arrays.toString(steps));
        } else {

            if (subStepIndex >= steps[stIndex].steps.length) {
                travelByTransit(finalResult, callback, activity, ++stIndex, 0);
                return;
            }

            if (subStepIndex == 0) {
                callback.speakUpdate(steps[stIndex].htmlInstructions.replaceAll("<[\\/a-zA-Z=\\s:;\\-.\"0-9]+>", " "));
            }

            final int[] curStep = {0};


            if (steps[stIndex].steps.length > 0) {
                DirectionsStep[] subSteps = finalResult.routes[0].legs[0].steps[stIndex].steps;

                Location curStepDestinationLocation = new Location("");


                curStepDestinationLocation.setLatitude(steps[stIndex].steps[subStepIndex].endLocation.lat);
                curStepDestinationLocation.setLongitude(steps[stIndex].steps[subStepIndex].endLocation.lng);

                callback.speakUpdate(steps[stIndex].steps[subStepIndex].htmlInstructions.replaceAll("<[\\/a-zA-Z=\\s:;\\-.\"0-9]+>", " "));
                com.khushitshah.blindspartner.libs.Location.Location location = new com.khushitshah.blindspartner.libs.Location.Location(activity);

                int finalStIndex = stIndex;
                location.getConstantLocation((strLoc) -> {
                    String lat = strLoc.split(",")[0];
                    String lng = strLoc.split(",")[1];
                    Location curUserLocation = new Location("");
                    curUserLocation.setLatitude(Double.parseDouble(lat));
                    curUserLocation.setLongitude(Double.parseDouble(lng));

                    if (curStepDestinationLocation.distanceTo(curUserLocation) < 3f) {
                        location.stopConstantLocation();
                        travelByTransit(finalResult, callback, activity, finalStIndex, subStepIndex + 1);
//                    subStepsIndex[0]++;
//                    if (subStepsIndex[0] > subSteps.length) {
//                        subStepsIndex[0] = 0;
//                        curStep[0]++;
//                    }
//                    curStepDestinationLocation.setLatitude(steps[curStep[0]].steps[subStepsIndex[0]].endLocation.lat);
//                    curStepDestinationLocation.setLongitude(steps[curStep[0]].steps[subStepsIndex[0]].endLocation.lng);
//                    if (curStep[0] >= steps.length) {
//                        callback.speakUpdate("You reached your destination!");
//                    }
                    }
                }, activity);
            }
        }
    }

    private void travelByWalking(DirectionsResult finalResult, TravellingInterface callback, Activity activity) {
        System.out.println(finalResult.routes[0].toString() + " " + finalResult.routes[0].legs[0].toString());
        DirectionsStep[] steps = finalResult.routes[0].legs[0].steps;
        final int[] curStep = {0};

        Location curStepDestinationLocation = new Location("");

        curStepDestinationLocation.setLatitude(steps[curStep[0]].endLocation.lat);
        curStepDestinationLocation.setLongitude(steps[curStep[0]].endLocation.lng);
        callback.speakUpdate(steps[curStep[0]].htmlInstructions.replaceAll("<[\\/a-zA-Z=\\s:;\\-.\"0-9]+>", " "));
        com.khushitshah.blindspartner.libs.Location.Location location = new com.khushitshah.blindspartner.libs.Location.Location(activity);

        location.getConstantLocation((strLoc) -> {
            String lat = strLoc.split(",")[0];
            String lng = strLoc.split(",")[1];
            Location curUserLocation = new Location("");
            curUserLocation.setLatitude(Double.parseDouble(lat));
            curUserLocation.setLongitude(Double.parseDouble(lng));

            if (curStepDestinationLocation.distanceTo(curUserLocation) < 3f) {
                curStep[0]++;
                if (curStep[0] >= steps.length) {
                    location.stopConstantLocation();
                    callback.speakUpdate("You reached your destination!");
                    return;
                }
                curStepDestinationLocation.setLatitude(steps[curStep[0]].endLocation.lat);
                curStepDestinationLocation.setLongitude(steps[curStep[0]].endLocation.lng);
            }
        }, activity);

    }
}
