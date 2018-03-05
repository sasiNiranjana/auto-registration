package com.lifefitness.rd.autoregistration;

import com.lifefitness.rd.autoregistration.verticles.MultiCastNode;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.SocketException;
import java.util.stream.Stream;

@SpringBootApplication
public class AutoRegistrationApplication {

	public static void main(String[] args) {

		//SpringApplication.run(AutoRegistrationApplication.class, args);
		Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(40));
		MultiCastNode node=new MultiCastNode(args[0]);
		DeploymentOptions options = new DeploymentOptions().setWorker(true);
		Future<Void> s= Future.future();
		s.setHandler(i-> {
					if (i.succeeded()) {
						System.out.println("Sending....");
						try {
							node.sendMessage("Hello");
						} catch (SocketException e) {
							e.printStackTrace();
						}
					}
				}
			);
		vertx.deployVerticle(node,options,r1->{
			if(r1.succeeded()){
				System.out.println("Verticle deployed");
				s.complete();
			}
		});
	}
}
