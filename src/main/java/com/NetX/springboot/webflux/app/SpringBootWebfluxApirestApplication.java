package com.NetX.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.NetX.springboot.webflux.app.models.documents.Categoria;
import com.NetX.springboot.webflux.app.models.documents.Producto;
import com.NetX.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner{

	@Autowired
	private ProductoService service;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
		
		
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();

		Categoria nintendo = new Categoria("Nintendo");
		Categoria sega = new Categoria("Sega");
		Categoria atari = new Categoria("Atari");
		Categoria sony = new Categoria("Sony");
		Categoria apple = new Categoria("Apple");
		Categoria panasonic = new Categoria("Panasonic");

		Flux.just(nintendo, sega, atari, sony, apple, panasonic).flatMap(service::saveCategoria).doOnNext(c -> {
			log.info("Categoria creada: " + c.getNombre() + "/ Id: " + c.getId());
		}).thenMany(Flux
				.just(new Producto("Sega Saturn", 399.00, sega), 
					  new Producto("PlayStation", 299.00, sony),
					  new Producto("Nintendo 64", 299.00, nintendo), 
					  new Producto("3DO", 380.00, panasonic),
					  new Producto("Atari Jaguar", 280.00, atari), 
					  new Producto("Apple PipPin", 400.00, apple))
				.flatMap(producto -> {
					producto.setCreateAt(new Date());
					return service.save(producto);
				}))
				.subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
		
	}

}
