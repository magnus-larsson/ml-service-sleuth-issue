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

    @Autowired private Sink channels;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ProductService service;

    private AbstractMessageChannel input = null;

    private Product product = new Product(1, "Name 1", 1);

    @BeforeEach
    public void setupTest() {
        input = (AbstractMessageChannel) channels.input();
        repository.deleteAll().block();
    }

    @Test
    public void createProductUsingStream() {
      assertEquals(0, getNoOfProductsInDb());
      input.send(new GenericMessage<>(new Event(CREATE, 1, product)));
      assertEquals(1, getNoOfProductsInDb());
    }

    @Test
    public void createProductDirect() {
        assertEquals(0, getNoOfProductsInDb());
        service.createProduct(product);
        assertEquals(1, getNoOfProductsInDb());
    }

    @Test
    public void usingStreamWithoutMongoDb() {
        assertEquals(0, getNoOfProductsInDb());
        input.send(new GenericMessage<>(new Event(NOOP, 1, product)));
        assertEquals(0, getNoOfProductsInDb());
    }

    private Long getNoOfProductsInDb() {
        return repository.count().block();
    }
}
