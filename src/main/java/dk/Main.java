package dk;

import com.google.common.eventbus.EventBus;
import dk.exchange.KrakenExchange;
import dk.exchange.KrakenGetCurrencyPairGetter;
import lombok.val;

public class Main {


    public static void main(String[] args) throws Exception  {
        val currencyPairs  = new KrakenGetCurrencyPairGetter().getCurrencyPairs();
        val cryptoBus      = new EventBus();
        val viewBus        = new EventBus();

        val crypto         = new Crypto(viewBus);
        val krakenExchange = new KrakenExchange(cryptoBus, currencyPairs);

        cryptoBus.register(crypto);

        new Thread(krakenExchange).run();
    }

}
