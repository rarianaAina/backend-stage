package com.nrstudio.portail.securite;
import io.jsonwebtoken.Jwts; import io.jsonwebtoken.SignatureAlgorithm; import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component;
import javax.crypto.SecretKey; import java.util.Date;
@Component
public class JwtSimple {
  @Value("${app.securite.jwtSecret}") private String secret;
  @Value("${app.securite.jwtExpirationMs}") private long expirationMs;
  private SecretKey cle() { return Keys.hmacShaKeyFor(secret.getBytes()); }
  public String generer(String sujet){
    return Jwts.builder().setSubject(sujet).setIssuedAt(new Date())
      .setExpiration(new Date(System.currentTimeMillis()+expirationMs))
      .signWith(cle(), SignatureAlgorithm.HS256).compact();
  }
}