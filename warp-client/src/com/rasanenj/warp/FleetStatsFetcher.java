package com.rasanenj.warp;

import com.badlogic.gdx.utils.Array;
import com.google.gwt.http.client.*;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.messaging.ShipStatsMessage;

import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * @author Janne Rasanen
 */
public class FleetStatsFetcher {
    public FleetStatsFetcher(long id) {

    }

    public void loadJSON(final ServerConnection serverConnection) {
        String url = "http://" + Constants.PERSIST_SERVER_HOST + "/userships/1/?format=json";
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));

        try {
            Request request = builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    log("error fetching JSON");
                }

                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        String json = response.getText();
                        // log("Successfully loaded JSON: " + json);
                        parse(json);

                    } else {
                        log("Error response while fetching Fleet JSON: " + response.getStatusText());
                        log("Falling back to offline mode");
                        parse(Constants.OFFLINE_FLEET);
                    }
                }

                public void parse(String json) {
                    int length = getFleetLength(json);

                    final Array<ShipStatsMessage> msgs = new Array<ShipStatsMessage>(false, length);

                    for (int i=0; i < length; i++) {
                        ShipJSON ship = parseJson(json, i);
                        msgs.add(new ShipStatsMessage(ship.getMaxSpeed(), ship.getAcceleration(), ship.getTurnSpeed(), ship.getMaxHealth()));
                    }

                    serverConnection.sendShipStats(msgs);
                }
            });

        } catch (RequestException e) {
            log("Couldn't contact server for fleet data");
        }
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
