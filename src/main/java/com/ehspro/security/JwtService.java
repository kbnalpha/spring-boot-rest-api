package com.ehspro.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service
public class JwtService {
    private final SecretKey key;
    private final long ttl;
    private final AccountDetailsService identities;
    private final String superPassword;
    public JwtService(@Value("${ehs.jwt.secret:}") String secret,@Value("${ehs.jwt.ttl-seconds:3600}") long ttl,AccountDetailsService identities,
            @Value("${spring.security.user.password:ehs-api-local}") String superPassword) {
        this.superPassword=superPassword;
        this.key=secret.isBlank()?Jwts.SIG.HS256.key().build():Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        if(ttl<60||ttl>86400) throw new IllegalArgumentException("JWT_TTL_SECONDS must be between 60 and 86400");
        this.ttl=ttl;this.identities=identities;
    }
    public long expiresIn() {return ttl;}
    public String issue(EhsPrincipal principal) {
        Instant now=Instant.now();
        var current=(EhsPrincipal)identities.loadUserByUsername(principal.getUsername());
        return Jwts.builder().issuer("ehspro-api").subject(principal.getUsername()).issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(ttl))).id(UUID.randomUUID().toString())
            .claim("accountId",principal.accountId==null?-1L:principal.accountId)
            .claim("version",version(current)).signWith(key,Jwts.SIG.HS256).compact();
    }
    public EhsPrincipal verify(String token) {
        var claims=Jwts.parser().verifyWith(key).requireIssuer("ehspro-api").build().parseSignedClaims(token).getPayload();
        if(claims.getExpiration()==null||claims.getSubject()==null) throw new JwtException("Invalid claims");
        var principal=(EhsPrincipal)identities.loadUserByUsername(claims.getSubject());
        Number accountId=claims.get("accountId",Number.class);
        String version=claims.get("version",String.class);
        if(!principal.isEnabled()||accountId==null||accountId.longValue()!=(principal.accountId==null?-1L:principal.accountId)
            ||version==null||!MessageDigest.isEqual(version.getBytes(StandardCharsets.UTF_8),version(principal).getBytes(StandardCharsets.UTF_8)))
            throw new JwtException("Account unavailable or token revoked");
        return principal;
    }
    private String version(EhsPrincipal principal) {
        try {
            Mac mac=Mac.getInstance("HmacSHA256");mac.init(key);
            String credential=principal.accountId==null?superPassword:principal.getPassword();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(credential.getBytes(StandardCharsets.UTF_8)));
        } catch(java.security.GeneralSecurityException ex) {throw new IllegalStateException("Unable to validate token version",ex);}
    }
}
