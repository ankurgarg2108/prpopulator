package com.aurea.faster.prpopulator;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PRPopulatorConfig {

	@Bean("restTemplateJIRA")
	public RestTemplate restTemplateJIRA(@Value("${pr-populator.jira.credentials.userName}") String username,
			@Value("${pr-populator.jira.credentials.password}") String password) {
		RestTemplate restTemplate = new RestTemplate();
		if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
			restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(username, password));
		}
		return restTemplate;
	}

	@Bean("restTemplateGithub")
	public RestTemplate restTemplateGithub(@Value("${pr-populator.git-hub.oauth-token}") String token) {
		RestTemplate restTemplate = new RestTemplate();
		if (!StringUtils.isEmpty(token)) {
			restTemplate.getInterceptors().add(new OAuthTokenHeaderInterceptor(token));
		}
		return restTemplate;
	}

	class OAuthTokenHeaderInterceptor implements ClientHttpRequestInterceptor {
		String token;

		public OAuthTokenHeaderInterceptor(String token) {
			this.token = token;
		}

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			request.getHeaders().set("Authorization", "Token " + token);
			request.getHeaders().set("Accept", "application/vnd.github.hellcat-preview+json");
			return execution.execute(request, body);
		}

	}
}
