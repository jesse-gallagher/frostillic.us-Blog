package bean;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * This bean caches Gravatar images so they can be served from the app without
 * having the user's browser access a remote host
 */
@ApplicationScoped
public class UserPhotoBean {
	private final Map<String, byte[]> cache = new ConcurrentHashMap<>();
	
	public byte[] getThumbnailData(String hash) {
		// TODO expire cache
		synchronized(cache) {
			if(!cache.containsKey(hash)) {
				try(var http = HttpClient.newHttpClient()) {
					HttpRequest req = HttpRequest.newBuilder(URI.create("https://www.gravatar.com/avatar/" + hash + "?d=wavatar&s=256"))
						.GET()
						.build();
					cache.put(hash, http.send(req, BodyHandlers.ofByteArray()).body());
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				} catch (InterruptedException e) {
					return new byte[0];
				}
			}
			return cache.get(hash);
		}
		
	}
}

