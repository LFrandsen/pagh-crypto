package dk.crypto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of") @Getter
public class BidAsk {
    double bid;
    double ask;
}
