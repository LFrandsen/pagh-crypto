package dk;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import dk.crypto.BidAsk;
import dk.crypto.CurrencyPair;
import dk.crypto.Exchange;
import dk.events.PriceUpdateEvent;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

public class Crypto {

    private final Map<CurrencyPair, Map<Exchange,BidAsk>> currencyToExchangeToBidAsk = new HashMap<>();

    private final EventBus outBoundBus;

    public Crypto(EventBus outBoundBus) {
        this.outBoundBus = outBoundBus;
    }

    @Subscribe
    public void handlePriceUpdateEvent(PriceUpdateEvent priceUpdateEvent) {
        val currencyPair     = priceUpdateEvent.getCurrencyPair();
        val exchangeToBidAsk = currencyToExchangeToBidAsk.getOrDefault(currencyPair, new HashMap<>());
        val bidAsk           = priceUpdateEvent.getBidAsk();

        exchangeToBidAsk.put(priceUpdateEvent.getExchange(), bidAsk);
        currencyToExchangeToBidAsk.put(currencyPair, exchangeToBidAsk);
        System.out.println("Currency Pair: " + currencyPair + " Ask: " + bidAsk.getAsk() + " Bid: " + bidAsk.getBid() + " Exchange: " + priceUpdateEvent.getExchange());
    }
}
