package io.token.sample;

import static io.token.proto.common.account.AccountProtos.Account;
import static io.token.proto.common.transaction.TransactionProtos.Balance;
import static io.token.proto.common.transaction.TransactionProtos.Transaction;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonMap;

import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.token.proto.ProtoJson;
import io.token.proto.common.token.TokenProtos.Token;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class TokenService {
    private static final String AUTHORIZATION = "Authorization";
    private static String BASE_URL;

    private final Config config;

    public TokenService() {
        config = ConfigFactory.load();
        BASE_URL = config.getString("proxy-url");
    }

    public Token getToken(String tokenId) {
        String json = HttpClient.sendGet(toURL("/tokens/", tokenId))
                .getAsJsonObject("token")
                .toString();
        return ProtoJson.fromJson(json, Token.newBuilder());
    }

    public String requestAccess() {
        Map<String, String> params = new HashMap<>();
        params.put("callbackUrl", "http://localhost:3000/redeem");

        return HttpClient.sendPost(toURL("/access-token-requests"), params)
                .get("tokenRequestId")
                .getAsString();
    }

    public String generateTokenRequestUrl(String tokenRequestId) {
        return generateTokenRequestUrl(tokenRequestId, null, null);
    }

    public String generateTokenRequestUrl(String tokenRequestId, String state) {
        return generateTokenRequestUrl(tokenRequestId, state, null);
    }

    public String generateTokenRequestUrl(
            String tokenRequestId,
            String state,
            String csrfToken) {
        String query = "?requestId=" + tokenRequestId;

        if (state != null) {
            query += "&state" + state;
        }

        if (csrfToken != null) {
            query += "&csrfToken" + csrfToken;
        }

        return HttpClient.sendGet(toURL("/token-request-url", query))
                .get("url")
                .getAsString();
    }

    public String parseTokenRequestCallback(String callback) {
        String query = "url=" + urlEncode(callback);
        return HttpClient.sendGet(toURL("/parse-token-request-callback?", query))
                .get("tokenId")
                .getAsString();
    }

    public Account getAccount(String tokenId, String accountId) {
        String json = HttpClient.sendGet(
                toURL("/accounts/", accountId),
                singletonMap(AUTHORIZATION, tokenId))
                .getAsJsonObject("account")
                .toString();
        return ProtoJson.fromJson(json, Account.newBuilder());
    }

    public List<Account> getAccounts(String tokenId) {
        List<Account> accounts = new ArrayList<>();
        JsonObject response = HttpClient.sendGet(
                toURL("/accounts"),
                singletonMap(AUTHORIZATION, tokenId));
        if (response.has("accounts")) {
            response.getAsJsonArray("accounts")
                    .forEach(account -> accounts.add(ProtoJson.fromJson(
                            account.toString(),
                            Account.newBuilder())));
        }

        return accounts;
    }

    public Balance getBalance(String tokenId, String accountId) {
        String json = HttpClient.sendGet(
                toURL("/accounts/", accountId, "/balance"),
                singletonMap(AUTHORIZATION, tokenId))
                .getAsJsonObject("balance")
                .toString();
        return ProtoJson.fromJson(json, Balance.newBuilder());

    }

    public Transaction getTransaction(
            String tokenId,
            String accountId,
            String transactionId) {
        String json = HttpClient.sendGet(
                toURL("/accounts/", accountId, "/transactions/", transactionId),
                singletonMap(AUTHORIZATION, tokenId))
                .getAsJsonObject("transaction")
                .toString();
        return ProtoJson.fromJson(json, Transaction.newBuilder());
    }

    public List<Transaction> getTransactions(
            String tokenId,
            String accountId,
            int limit,
            @Nullable String offset) {
        String query = "limit=" + limit + (offset == null ? "" : "&offset" + offset);
        List<Transaction> transactions = new ArrayList<>();

        JsonObject response = HttpClient.sendGet(
                toURL("/accounts/", accountId, "/transactions?", query),
                singletonMap(AUTHORIZATION, tokenId));

        if (response.has("transactions")) {
            response.getAsJsonArray("transactions")
                    .forEach(transaction -> transactions.add(ProtoJson.fromJson(
                            transaction.toString(),
                            Transaction.newBuilder())));
        }
        return transactions;
    }

    private static URL toURL(String... components) {
        try {
            return new URL(BASE_URL + String.join("", components));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    private static String urlEncode(String string) {
        try {
            return URLEncoder.encode(string, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
