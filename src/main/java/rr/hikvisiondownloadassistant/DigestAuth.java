// Copyright (c) 2020 Ryan Richard

package rr.hikvisiondownloadassistant;

import lombok.RequiredArgsConstructor;

import java.net.http.HttpHeaders;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

@RequiredArgsConstructor
public class DigestAuth {

    private static MessageDigest messageDigest;
    private static SecureRandom random;

    private final HttpHeaders unauthorizedResponseHeaders;
    private final String requestMethod;
    private final String requestPath;
    private final String username;
    private final String password;

    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(System.currentTimeMillis());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAuthorizationHeaderValue() {
        Optional<String> wwwAuthenticateHeader = unauthorizedResponseHeaders.firstValue("www-authenticate");
        if (wwwAuthenticateHeader.isEmpty()) {
            throw new RuntimeException("Expected digest auth challenge but did not find it");
        }
        String authChallenge = wwwAuthenticateHeader.get();
        if (!authChallenge.startsWith("Digest ")) {
            throw new RuntimeException("Expected digest auth challenge but did not find it");
        }

        String[] authenticateFields = authChallenge.substring("Digest ".length()).replace("\"", "").split(", ");
        Map<String, String> authenticateFieldsMap = Arrays.stream(authenticateFields)
                .map(s -> s.split("="))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));

        String realm = authenticateFieldsMap.get("realm");
        if (realm == null) {
            throw new RuntimeException("Expected auth challenge to specify realm but it didn't");
        }
        String serverNonce = authenticateFieldsMap.get("nonce");
        if (serverNonce == null) {
            throw new RuntimeException("Expected auth challenge to specify nonce but it didn't");
        }
        String qop = authenticateFieldsMap.get("qop");
        if (qop == null || !Arrays.asList(qop.split(",")).contains("auth")) {
            throw new RuntimeException("Expected auth challenge to allow qop=auth but it didn't");
        }

        return getAuthorizationHeaderValue(realm, serverNonce);
    }

    private String getAuthorizationHeaderValue(String realm, String serverNonce) {
        String qop = "auth";
        String nonceCount = "00000001";
        String clientNonce = randomNonce();
        String ha1 = md5(username, realm, password);
        String ha2 = md5(requestMethod, requestPath);
        String responseVal = md5(ha1, serverNonce, nonceCount, clientNonce, qop, ha2);

        return "Digest " +
                "username=\"" + username + "\"," +
                "realm=\"" + realm + "\"," +
                "nonce=\"" + serverNonce + "\"," +
                "uri=\"" + requestPath + "\"," +
                "qop=auth," +
                "nc=" + nonceCount + "," +
                "cnonce=\"" + clientNonce + "\"," +
                "response=\"" + responseVal + "\"";
    }

    private String randomNonce() {
        byte[] nonceBytes = new byte[16];
        random.nextBytes(nonceBytes);
        return Base64.getEncoder().encodeToString(nonceBytes);
    }

    private String md5(String... values) {
        return printHexBinary(messageDigest.digest(String.join(":", values).getBytes(UTF_8))).toLowerCase();
    }

}
