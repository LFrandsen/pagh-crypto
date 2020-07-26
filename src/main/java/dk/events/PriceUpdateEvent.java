package dk.events;


import dk.crypto.BidAsk;
import dk.crypto.CurrencyPair;
import dk.crypto.Exchange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(staticName = "of") @Getter @ToString
public class PriceUpdateEvent {
    private BidAsk bidAsk;
    private CurrencyPair currencyPair;
    private Exchange exchange;
}
