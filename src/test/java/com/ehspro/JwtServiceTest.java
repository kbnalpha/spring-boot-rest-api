package com.ehspro;
import com.ehspro.security.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {
    @Test void signedTokensExpireAndTamperedTokensAreRejected() {
        byte[] bytes=new byte[32];new java.security.SecureRandom().nextBytes(bytes);
        String secret=Base64.getEncoder().encodeToString(bytes);
        var identities=mock(AccountDetailsService.class);
        var principal=new EhsPrincipal("user@example.com","stored-password-hash",true,1L,1L,List.of());
        when(identities.loadUserByUsername(principal.getUsername())).thenReturn(principal);
        var service=new JwtService(secret,3600,identities,"test-super-password");
        String token=service.issue(principal);
        assertThat(service.verify(token)).isSameAs(principal);
        String expired=Jwts.builder().issuer("ehspro-api").subject(principal.getUsername())
            .expiration(new Date(System.currentTimeMillis()-60000)).signWith(Keys.hmacShaKeyFor(bytes)).compact();
        assertThatThrownBy(() -> service.verify(expired)).isInstanceOf(ExpiredJwtException.class);
        String forged=Jwts.builder().issuer("ehspro-api").subject(principal.getUsername())
            .expiration(new Date(System.currentTimeMillis()+60000)).signWith(Jwts.SIG.HS256.key().build()).compact();
        assertThatThrownBy(() -> service.verify(forged)).isInstanceOf(JwtException.class);
        when(identities.loadUserByUsername(principal.getUsername())).thenReturn(new EhsPrincipal("user@example.com","new-password-hash",true,1L,1L,List.of()));
        assertThatThrownBy(() -> service.verify(token)).isInstanceOf(JwtException.class);
    }
}
