package se.magnus.microservices.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.support.GenericMessage;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.product.persistence.ProductRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.NOOP;

@SpringBootTest(properties = {"spring.data.mongodb.port: 0"})
public class MLTests {

    @Autowired
   	private Sink channels;

	@Autowired
	private ProductRepository repository;

	@Autowired
	private ProductService service;

   	private AbstractMessageChannel input = null;

   	@BeforeEach
   	public void setupTest() {
   		input = (AbstractMessageChannel) channels.input();
   		repository.deleteAll().block(); // *** PROBLEM IS HERE ***
   	}

   	@Test
   	public void createProductUsingStream() {
		Product product = new Product(1, "Name 1", 1);
		Event<Integer, Product> event = new Event(CREATE, 1, product);
		input.send(new GenericMessage<>(event));
   	}

	@Test
	public void createProductDirect() {
		assertEquals(0, repository.count().block());
		Product product = new Product(1, "Name 1", 1);
		Product newProduct = service.createProduct(product);
		assertEquals(1, repository.count().block());
	}

	@Test
	public void createProductNoop() {
		Product product = new Product(1, "Name 1", 1);
		Event<Integer, Product> event = new Event(NOOP, 1, product);
		input.send(new GenericMessage<>(event));
	}

}
