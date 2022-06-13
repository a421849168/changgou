import io.jsonwebtoken.*;

import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    /**
     * 创建令牌
     */
    @Test
    public void testCreateJwt(){
        //1、创建Jwt构建器-jwtBuilder = Jwts.builder()
        JwtBuilder jwtBuilder = Jwts.builder();
        //2、设置唯一编号-setId
        jwtBuilder.setId("007");
        //3、设置主题，可以是JSON数据-setSubject()
        jwtBuilder.setSubject("测试主题");
        //4、设置签发日期-setIssuedAt
        jwtBuilder.setIssuedAt(new Date());
        //5、设置签发人-setIssuer
        jwtBuilder.setIssuer("www.itheima.com");

        //设置过期时间-30秒后
//        Date exp = new Date(System.currentTimeMillis() + 30000);
//        jwtBuilder.setExpiration(exp);


        //自定义claims
        Map<String, Object> user = new HashMap<>();
        user.put("name", "steven");
        user.put("age", "18");
        user.put("address", "深圳市.黑马程序员");
        //注意这里使用add方法，代表追加内容
        jwtBuilder.addClaims(user);


        //6、设置签证
        jwtBuilder.signWith(SignatureAlgorithm.HS256, "itheima.steven");
        //7、生成令牌-compact()
        String token = jwtBuilder.compact();
        //8、输出结果
        System.out.println(token);
    }

    /**
     * 解析令牌
     */
    @Test
    public void testParseJwt(){
        String token = "123456";
        //1、创建Jwt解析器-jwtParser = Jwts.parser();
        JwtParser jwtParser = Jwts.parser();
        //2、设置签名-密钥
        jwtParser.setSigningKey("itheima.steven");
        //3、设置要解析的密文，并读取结果
        Claims claims = jwtParser.parseClaimsJws(token).getBody();
        //4、输出结果
        System.out.println(claims);
    }

}
