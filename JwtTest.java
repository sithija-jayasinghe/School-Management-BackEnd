import java.util.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
public class JwtTest {
    public static void main(String[] args) {
        String secret = "iTNEw2qQZ2d2Hqeu9C/BHQ7NP116jgfalSEHRTPEIC0=";
        Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        System.out.println("Signing key created successfully.");
    }
}
