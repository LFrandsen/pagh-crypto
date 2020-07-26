package dk.exchange;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import dk.crypto.BidAsk;
import dk.crypto.CurrencyPair;
import dk.crypto.Exchange;
import dk.events.PriceUpdateEvent;
import lombok.SneakyThrows;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class KrakenExchange implements Runnable {

    private static final String SERVER = "wss://ws.kraken.com";
    private static final int TIMEOUT   = 5000;
    private static final Gson GSON     = new Gson();

    private final EventBus outBoundBus;
    private final String currencyPairs;

    private boolean run = true;

    public KrakenExchange(EventBus outBoundBus, Set<CurrencyPair> currencyPairs) {
        this.outBoundBus = outBoundBus;
        val currencyPairDivided = currencyPairs.stream().map(currencyPair -> "\"" + currencyPair.getBase() + "/" + currencyPair.getTerm() + "\"").collect(Collectors.toSet());
        this.currencyPairs = String.join(",", currencyPairDivided);
    }

    @Override @SneakyThrows
    public void run() {
        WebSocket ws = connect();

        ws.sendText("{\"event\":\"subscribe\", \"subscription\":{\"name\":\"ticker\"}, \"pair\":["+currencyPairs+"]}");

        while (run) { }

        ws.disconnect();
    }

    public void stop() {
        run = false;
    }

    /**
     * Connect to the server.
     */
    private WebSocket connect() throws Exception
    {
        return new WebSocketFactory()
                .setConnectionTimeout(TIMEOUT)
                .createSocket(SERVER)
                .addListener(new WebSocketAdapter() {
                    // A text message arrived from the server.
                    public void onTextMessage(WebSocket websocket, String message) {
                        if(message.contains("ticker")) {
                            List<Object> decodeMessage = GSON.fromJson(message, new TypeToken<List<Object>>() {}.getType());
                            if (decodeMessage.size() == 4) {
                                val ticker = decodeMessage.get(2);
                                if (ticker instanceof String && ((String) ticker).equalsIgnoreCase("ticker")) {
                                    Map<String, List<Object>> ticks = (Map<String, List<Object>>) decodeMessage.get(1);
                                    val ask                = Double.parseDouble((String) ticks.get("a").get(0));
                                    val bid                = Double.parseDouble((String) ticks.get("b").get(0));
                                    val krakenCurrencyPair = (String)decodeMessage.get(3);
                                    val currencyPair       = CurrencyPair.of(krakenCurrencyPair.substring(0,3),  krakenCurrencyPair.substring(4,7));
                                    outBoundBus.post(PriceUpdateEvent.of(BidAsk.of(bid,ask),currencyPair, Exchange.KRAKEN));
                                }
                            }

                        }
                    }
                })
                .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                .connect();
    }
}
