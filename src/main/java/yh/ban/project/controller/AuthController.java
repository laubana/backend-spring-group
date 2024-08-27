package yh.ban.project.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import com.stripe.StripeClient;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import yh.ban.project.dto.UserDto;
import yh.ban.project.factories.SecretKeyFactory;
import yh.ban.project.factories.StripeClientFactory;
import yh.ban.project.helper.StringHelper;
import yh.ban.project.mapper.UserMapper;
import yh.ban.project.model.User;
import yh.ban.project.type.Response;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private static final Logger logger = LogManager.getLogger(AuthController.class);

	@Autowired
	MongoTemplate mongoTemplate;

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

			CustomerCreateParams customerCreateParams = CustomerCreateParams.builder().setEmail(body.getEmail())
					.setName(body.getName()).build();
			StripeClient stripeClient = StripeClientFactory.getStripeClient();
			Customer customer = stripeClient.customers().create(customerCreateParams);
			body.setCustomerId(customer.getId());

			User newUser = mongoTemplate.insert(UserMapper.INSTANCE.userDtoToUser(body));

			return ResponseEntity.created(null).body(new Response("User created successfully", newUser));
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
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Sign-in Failed."));
			}

			BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
			Boolean isMatch = bcryptPasswordEncoder.matches(body.getPassword(), existingUser.getPassword());

			if (!isMatch) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Sign-in Failed."));
			}

			String accessToken = Jwts.builder().claim("id", existingUser.get_id()).subject("accessToken")
					.issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 5000))
					.signWith(SecretKeyFactory.getSecretKey()).compact();

			String refreshToken = Jwts.builder().claim("id", existingUser.get_id()).subject("refreshToken")
					.issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 60000))
					.signWith(SecretKeyFactory.getSecretKey()).compact();

			ResponseCookie tokenCookie = ResponseCookie.from("refreshToken", refreshToken).httpOnly(true).maxAge(3600)
					.build();

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

	@GetMapping("/refresh")
	public ResponseEntity<Response> refresh(@CookieValue String refreshToken) {
		try {
			if (StringHelper.isNullOrBlank(refreshToken)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Refresh Failed."));
			}

			Jws<Claims> jws = Jwts.parser().verifyWith(SecretKeyFactory.getSecretKey()).build()
					.parseSignedClaims(refreshToken);

			if (jws.getPayload().get("id") == null
					|| StringHelper.isNullOrBlank(jws.getPayload().get("id").toString())) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Refresh Failed."));
			}

			String id = jws.getPayload().get("id").toString();

			User existingUser = mongoTemplate.findById(id, User.class);

			if (existingUser == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Refresh Failed."));
			}

			String accessToken = Jwts.builder().claim("id", existingUser.get_id()).subject("accessToken")
					.issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 5000))
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

	@PostMapping("/sign-out")
	public ResponseEntity<Response> signOut() {
		ResponseCookie tokenCookie = ResponseCookie.from("refreshToken", null).httpOnly(true).maxAge(0).build();

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString())
				.body(new Response("Signed out successfully."));
	}
}
