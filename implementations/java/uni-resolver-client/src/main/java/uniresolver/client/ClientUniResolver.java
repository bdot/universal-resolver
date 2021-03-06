package uniresolver.client;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniresolver.ResolutionException;
import uniresolver.UniResolver;
import uniresolver.ddo.DDO;

public class ClientUniResolver implements UniResolver {

	private static Logger log = LoggerFactory.getLogger(ClientUniResolver.class);

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final URI DEFAULT_RESOLVER_URI = URI.create("http://localhost:8080/1.0/identifiers/");

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private URI resolverUri = DEFAULT_RESOLVER_URI;

	public ClientUniResolver() {

	}

	@Override
	public DDO resolve(String identifier) throws ResolutionException {

		// prepare HTTP request

		String uriString = this.getResolverUri().toString();
		if (! uriString.endsWith("/")) uriString += "/";
		uriString += identifier;

		HttpGet httpGet = new HttpGet(URI.create(uriString));

		// retrieve DDO

		DDO ddo;

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			if (httpResponse.getStatusLine().getStatusCode() == 404) return null;
			if (httpResponse.getStatusLine().getStatusCode() > 200) throw new ResolutionException("Cannot retrieve DDO for " + identifier + " from " + uriString + ": " + httpResponse.getStatusLine());

			HttpEntity httpEntity = httpResponse.getEntity();

			ddo = DDO.fromString(EntityUtils.toString(httpEntity));
			EntityUtils.consume(httpEntity);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve DDO for " + identifier + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved DDO for " + identifier + " (" + uriString + "): " + ddo);

		// done

		return ddo;
	}

	/*
	 * Getters and setters
	 */

	public URI getResolverUri() {

		return this.resolverUri;
	}

	public void setResolverUri(URI resolverUri) {

		this.resolverUri = resolverUri;
	}

	public void setResolverUri(String resolverUri) {

		this.resolverUri = URI.create(resolverUri);
	}

	public HttpClient getHttpClient() {

		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {

		this.httpClient = httpClient;
	}
}
