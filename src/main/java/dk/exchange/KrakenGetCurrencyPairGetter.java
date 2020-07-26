package dk.exchange;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dk.crypto.CurrencyPair;
import lombok.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.net.http.HttpRequest.newBuilder;


public class KrakenGetCurrencyPairGetter {

    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Only used for generating the valid currency pair
     * @return
     */
    public Set<String> findValidCurrencyPairs() {
        val currencyPairs = new HashSet<String>();
        Currency.CURRENCIES.forEach(baseCurrency -> {
            Currency.CURRENCIES.forEach(termCurrency -> {
                HttpRequest request = newBuilder()
                        .uri(URI.create("https://api.kraken.com/0/public/Ticker"))
                        .POST(HttpRequest.BodyPublishers.ofString("pair=" + baseCurrency + termCurrency))
                        .build();
                try {
                    val response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    val body = response.body();
                    if (!body.equalsIgnoreCase("{\"error\":[\"EQuery:Unknown asset pair\"]}") && !body.equalsIgnoreCase("{\"error\":[\"EQuery:Invalid asset pair\"],\"result\":{}}")) {
                        System.out.println("CurrencyPair.of(\""+ baseCurrency + "\",\""+termCurrency+"\"),");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        });
        return currencyPairs;
    }




    public Set<CurrencyPair> getCurrencyPairs() {
        val currencyPairVolumens = new HashSet<CurrencyPairVolume>();
        CurrencyPairs.CURRENCY_PAIRS.forEach(currencyPair -> {
            val concatCurrencyPair = currencyPair.getBase() + currencyPair.getTerm();
            HttpRequest request = newBuilder()
                    .uri(URI.create("https://api.kraken.com/0/public/Ticker"))
                    .POST(HttpRequest.BodyPublishers.ofString("pair=" + concatCurrencyPair))
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                val body = response.body();
                val index = body.indexOf(concatCurrencyPair);
                val sub = body.substring(index+concatCurrencyPair.length() + 2, body.length()-2);
                try {
                    Tick tick = new Gson().fromJson(sub, Tick.class);
                    val volume = Double.parseDouble(tick.v.get(1));
                    System.out.println("Added currency pair volume" + CurrencyPairVolume.of(currencyPair, volume));
                    currencyPairVolumens.add(CurrencyPairVolume.of(currencyPair, volume));
                }
                catch (IllegalStateException e){
                    //Do nothing
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JsonSyntaxException e) {
               e.printStackTrace();
            }

        });
        val sortedCurrencyPairVolumens = currencyPairVolumens.stream().sorted();
        sortedCurrencyPairVolumens.forEach(currencyPairVolume -> System.out.println("Currency Pair: " + currencyPairVolume.currencyPair + " Volume: " + currencyPairVolume.volume));
        val topThreeCurrencyPairVolumens  = currencyPairVolumens.stream().sorted().limit(3).collect(Collectors.toList());
        System.out.println("Top three volume is:");
        topThreeCurrencyPairVolumens.forEach(currencyPairVolume -> System.out.println("Currency Pair: " + currencyPairVolume.currencyPair + " Volume: " + currencyPairVolume.volume));
        return topThreeCurrencyPairVolumens.stream().map(CurrencyPairVolume::getCurrencyPair).collect(Collectors.toSet());

    }

    @Data
    class Tick {

        public List<String> a;
        public List<String> b;
        public List<String> c;
        public List<String> v;
        public List<String> p;
        public List<Integer> t;
        public List<String> l;
        public List<String> h;
        public String o;
    }


    @AllArgsConstructor(staticName = "of") @Getter @ToString
    static class CurrencyPairVolume implements Comparable<CurrencyPairVolume> {
        private CurrencyPair currencyPair;
        private double volume;


        @Override
        public int compareTo(CurrencyPairVolume o) {
            return Double.compare(o.volume, volume);
        }
    }
}
