package dk.crypto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
@Getter
public class CurrencyPair {
    private String base;
    private String term;


    @Override
    public String toString() {
        return base + "/" + term;
    }
}
