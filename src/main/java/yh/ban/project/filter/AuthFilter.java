package yh.ban.project.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import yh.ban.project.factories.SecretKeyFactory;
import yh.ban.project.helper.StringHelper;

public class AuthFilter extends OncePerRequestFilter {
	private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			FilterChain filterChain) throws IOException, ServletException {
		String path = (String) httpServletRequest.getServletPath();
		if (path.startsWith("/auth")) {
			filterChain.doFilter(httpServletRequest, httpServletResponse);

			return;
		}

		String authorization = httpServletRequest.getHeader("Authorization");

		if (authorization == null || !authorization.startsWith("Bearer ")) {
			httpServletResponse.setStatus(401);

			return;
		}

		try {
			String accessToken = authorization.split(" ")[1].trim();
			Jws<Claims> jws = Jwts.parser().verifyWith(SecretKeyFactory.getSecretKey()).build()
					.parseSignedClaims(accessToken);

			if (jws.getPayload().get("id") == null
					|| StringHelper.isNullOrBlank(jws.getPayload().get("id").toString())) {
				httpServletResponse.setStatus(401);
			}

			httpServletRequest.setAttribute("id", jws.getPayload().get("id").toString());

			filterChain.doFilter(httpServletRequest, httpServletResponse);
		} catch (Exception exception) {
			httpServletResponse.setStatus(401);

			filterChain.doFilter(httpServletRequest, httpServletResponse);
		}
	}
}