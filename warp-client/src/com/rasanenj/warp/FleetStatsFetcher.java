package com.rasanenj.warp;

import com.badlogic.gdx.utils.Array;
import com.google.gwt.http.client.*;
import com.rasanenj.warp.entities.ShipStats;

import static com.rasanenj.warp.Log.log;

/**
 * @author Janne Rasanen
 */
public class FleetStatsFetcher {
    public interface StatsReceiver {
        public abstract void receive(Array<ShipStats> stats);
    }

    public FleetStatsFetcher() {

    }

    public void loadJSON(final StatsReceiver receiver) {
        String url = "http://" + Constants.PERSIST_SERVER_HOST + "/userships/1/?format=json";
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));

        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    log("error fetching JSON");
                }

                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        String json = response.getText();
                        // log("Successfully loaded JSON: " + json);
                        receiver.receive(parse(json));

                    } else {
                        log("Error response while fetching Fleet JSON: " + response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            log("Couldn't contact server for fleet data");
        }
    }

    public static Array<ShipStats> parse(String json) {
        int length = getFleetLength(json);

        final Array<ShipStats> msgs = new Array<ShipStats>(false, length);

        for (int i=0; i < length; i++) {
            ShipJSON ship = parseJson(json, i);
            msgs.add(ship.getStats());
        }

        return msgs;
    }

    /**
     * Takes in a trusted JSON String and evals it.
     */
    public static native ShipJSON parseJson(String jsonStr, int index) /*-{
        return eval(jsonStr)[index];
    }-*/;


    public static native int getFleetLength(String jsonStr) /*-{
        return eval(jsonStr).length;
    }-*/;
}
