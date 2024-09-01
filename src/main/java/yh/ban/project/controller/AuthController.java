package yh.ban.project.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.gson.GsonFactory;
import com.stripe.model.Customer;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import yh.ban.project.dto.UserDto;
import yh.ban.project.factory.SecretKeyFactory;
import yh.ban.project.helper.StringHelper;
import yh.ban.project.helper.StripeHelper;
import yh.ban.project.mapper.UserMapper;
import yh.ban.project.model.User;
import yh.ban.project.type.Response;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private static final Logger logger = LogManager.getLogger(AuthController.class);

	@Value("${front-url}")
	private String frontUrl;

	@Value("${backend-url}")
	private String backendUrl;

	@Value("${google-client-id}")
	private String googleClientId;

	@Value("${google-secret}")
	private String googleSecret;

	@Autowired
	MongoTemplate mongoTemplate;

	@GetMapping("/oauth")
	public ResponseEntity<Response> oauth(@RequestParam String code) {
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("client_id", googleClientId);
			data.put("client_secret", googleSecret);
			data.put("redirect_uri", backendUrl + "/auth/oauth");
			data.put("code", code);
			data.put("scope", "");
			data.put("grant_type", "authorization_code");
			HttpContent httpContent = new JsonHttpContent(new GsonFactory(), data);
			HttpRequest tokenRequest = new NetHttpTransport().createRequestFactory()
					.buildPostRequest(new GenericUrl("https://oauth2.googleapis.com/token"), httpContent);
			HttpResponse tokenResponse = tokenRequest.execute();
			Map<?, ?> tokenParsedResponse = new GsonFactory().createJsonObjectParser()
					.parseAndClose(tokenResponse.getContent(), tokenResponse.getContentCharset(), Map.class);

			String accessToken = (String) tokenParsedResponse.get("access_token");

			HttpRequest userinfoRequest = new NetHttpTransport().createRequestFactory()
					.buildGetRequest(new GenericUrl("https://www.googleapis.com/oauth2/v3/userinfo"));
			userinfoRequest.getHeaders().setAuthorization("Bearer " + accessToken);
			HttpResponse userinfoResponse = userinfoRequest.execute();
			Map<?, ?> userinfoParsedResponse = new GsonFactory().createJsonObjectParser()
					.parseAndClose(userinfoResponse.getContent(), userinfoResponse.getContentCharset(), Map.class);

			String email = (String) userinfoParsedResponse.get("email");
			String picture = (String) userinfoParsedResponse.get("picture");
			String name = (String) userinfoParsedResponse.get("name");

			User existingUser = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("email").is(email)),
					User.class);

			String userId;
			if (existingUser != null) {
				userId = existingUser.get_id();
			} else {
				Customer stripeCustomer = StripeHelper.createCustomer(email, name);

				User newUser = mongoTemplate.insert(UserMapper.INSTANCE.userDtoToUser(new UserDto(null, email, null,
						picture, name, null, null, null, null, stripeCustomer.getId(), null, null)));

				userId = newUser.get_id();
			}

			String refreshToken = Jwts.builder().claim("id", userId).claim("email", email).subject("refreshToken")
					.issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000))
					.signWith(SecretKeyFactory.getSecretKey()).compact();

			ResponseCookie tokenCookie = ResponseCookie.from("refreshToken", refreshToken).httpOnly(true)
					.sameSite("None").secure(true).maxAge(7 * 24 * 3600 * 1000).build();

			return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.SET_COOKIE, tokenCookie.toString())
					.header(HttpHeaders.LOCATION, frontUrl).body(new Response("", data));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@GetMapping("/refresh")
	public ResponseEntity<Response> refresh(@CookieValue(required = false) String refreshToken) {
		try {
			if (StringHelper.isNullOrBlank(refreshToken)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Refresh failed."));
			}

			Jws<Claims> jws = Jwts.parser().verifyWith(SecretKeyFactory.getSecretKey()).build()
					.parseSignedClaims(refreshToken);

			if (jws.getPayload().get("id") == null
					|| StringHelper.isNullOrBlank(jws.getPayload().get("id").toString())) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Refresh failed."));
			}

			String id = jws.getPayload().get("id").toString();

			User existingUser = mongoTemplate.findById(id, User.class);

			if (existingUser == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Refresh failed."));
			}

			String accessToken = Jwts.builder().claim("id", existingUser.get_id())
					.claim("email", existingUser.getEmail()).subject("accessToken").issuedAt(new Date())
					.expiration(new Date(System.currentTimeMillis() + 24 * 3600 * 1000))
					.signWith(SecretKeyFactory.getSecretKey()).compact();

			Map<String, Object> data = new HashMap<>();
			data.put("accessToken", accessToken);
			data.put("id", existingUser.get_id());
			data.put("email", existingUser.getEmail());

			return ResponseEntity.ok().body(new Response("Refreshed successfully.", data));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@PostMapping("/sign-in")
	public ResponseEntity<Response> signIn(@RequestBody UserDto body) {
		try {
			if (StringHelper.isNullOrBlank(body.getEmail()) || StringHelper.isNullOrBlank(body.getPassword())) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			User existingUser = mongoTemplate
					.findOne(new Query().addCriteria(Criteria.where("email").is(body.getEmail())), User.class);

			if (existingUser == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Sign-in failed."));
			}

			BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
			Boolean isMatch = bcryptPasswordEncoder.matches(body.getPassword(), existingUser.getPassword());

			if (!isMatch) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Sign-in failed."));
			}

			String accessToken = Jwts.builder().claim("id", existingUser.get_id())
					.claim("email", existingUser.getEmail()).subject("accessToken").issuedAt(new Date())
					.expiration(new Date(System.currentTimeMillis() + 24 * 3600 * 1000))
					.signWith(SecretKeyFactory.getSecretKey()).compact();

			String refreshToken = Jwts.builder().claim("id", existingUser.get_id())
					.claim("email", existingUser.getEmail()).subject("refreshToken").issuedAt(new Date())
					.expiration(new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000))
					.signWith(SecretKeyFactory.getSecretKey()).compact();

			ResponseCookie tokenCookie = ResponseCookie.from("refreshToken", refreshToken).httpOnly(true)
					.sameSite("None").secure(true).maxAge(7 * 24 * 3600 * 1000).build();

			Map<String, Object> data = new HashMap<>();
			data.put("accessToken", accessToken);
			data.put("id", existingUser.get_id());
			data.put("email", existingUser.getEmail());

			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString())
					.body(new Response("Signed in successfully.", data));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@PostMapping("/sign-out")
	public ResponseEntity<Response> signOut() {
		try {
			ResponseCookie tokenCookie = ResponseCookie.from("refreshToken", null).httpOnly(true).maxAge(0).build();

			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString())
					.body(new Response("Signed out successfully."));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@PostMapping("/sign-up")
	public ResponseEntity<Response> signUp(@RequestBody UserDto body) {
		try {
			if (StringHelper.isNullOrBlank(body.getEmail()) || StringHelper.isNullOrBlank(body.getPassword())
					|| StringHelper.isNullOrBlank(body.getImageUrl()) || StringHelper.isNullOrBlank(body.getName())) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			User existingUser = mongoTemplate
					.findOne(new Query().addCriteria(Criteria.where("email").is(body.getEmail())), User.class);

			if (existingUser != null) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response("User already exists."));
			}

			BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
			body.setPassword(bcryptPasswordEncoder.encode(body.getPassword()));

			Customer stripeCustomer = StripeHelper.createCustomer(body.getEmail(), body.getName());
			body.setCustomerId(stripeCustomer.getId());

			User newUser = mongoTemplate.insert(UserMapper.INSTANCE.userDtoToUser(body));

			return ResponseEntity.created(null).body(new Response("User created successfully.", newUser));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}
}
