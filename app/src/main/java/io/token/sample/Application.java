package io.token.sample;

import static com.google.common.base.Charsets.UTF_8;
import static io.token.proto.common.account.AccountProtos.Account;

import com.google.common.io.Resources;
import io.token.proto.common.transaction.TransactionProtos.Balance;
import io.token.proto.common.transaction.TransactionProtos.Transaction;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import spark.Spark;

/**
 * Application main entry point.
 * To execute, one needs to run something like:
 * <p>
 * <pre>
 * ./gradlew :app:shadowJar
 * java -jar ./app/build/libs/app-1.0.0-all.jar
 * </pre>
 */
public class Application {
    private static TokenService tokenService = new TokenService();

    /**
     * Main function.
     *
     * @param args command line arguments
     * @throws IOException thrown on errors
     */
    public static void main(String[] args) throws IOException {
        // Initializes the server
        Spark.port(3000);

        // Endpoint for transfer payment, called by client side to initiate a payment.
        Spark.post("/request-access", (req, res) -> {
            String tokenRequestId = tokenService.requestAccess();
            String tokenRequestUrl = tokenService.generateTokenRequestUrl(tokenRequestId);

            //send user to Token cloud
            res.status(302);
            res.redirect(tokenRequestUrl);
            return null;
        });

        // Endpoint for access token redemption.
        Spark.get("/redeem", (req, res) -> {
            String callbackUri = req.raw().getRequestURL().toString()
                    + "?"
                    + req.raw().getQueryString();
            String tokenId = tokenService.parseTokenRequestCallback(callbackUri);

            List<Account> accounts = tokenService.getAccounts(tokenId);
            if (accounts.size() == 0) {
                res.status(404);
                return "No Accounts Found!";
            }

            Account account = accounts.get(0);
            Balance balance = tokenService.getBalance(tokenId, account.getId());
            List<Transaction> transactions = tokenService.getTransactions(
                    tokenId,
                    account.getId(),
                    10,
                    null);

            res.status(200);
            return String.format(
                    "First account:<br/> %s <br/><br/> "
                            + "Balance:<br/> %s <br/><br/> "
                            + "First 10 transactions:<br/> %s",
                    accounts.toString(),
                    balance.toString(),
                    transactions.toString());
        });

        // Serve the web page, stylesheet and JS script:
        String script = Resources.toString(Resources.getResource("script.js"), UTF_8);
        Spark.get("/script.js", (req, res) -> script);
        String style = Resources.toString(Resources.getResource("style.css"), UTF_8);
        Spark.get("/style.css", (req, res) -> {
            res.type("text/css");
            return style;
        });
        String page = Resources.toString(Resources.getResource("index.html"), UTF_8);
        Spark.get("/", (req, res) -> page);
    }
}
