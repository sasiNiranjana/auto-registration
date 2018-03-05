package com.lifefitness.rd.autoregistration.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class MultiCastNode extends AbstractVerticle {

    private DatagramSocket receiveSocket;
    private DatagramSocket sendSocket;
    private String node;
    public MultiCastNode(String node){
        this.node=node;
    }

    public void start(Future<Void> startFuture){
        this.receiveSocket=vertx.createDatagramSocket(new DatagramSocketOptions().setBroadcast(true));
        this.sendSocket=vertx.createDatagramSocket(new DatagramSocketOptions().setBroadcast(true));
        receiveSocket.listen(7415, "0.0.0.0", asyncResult -> {
            if (asyncResult.succeeded()) {
                receiveSocket.handler(packet -> {
                    System.out.println("Message received by : "+packet.sender().host());
                    System.out.println("Data : "+packet.data().toString());
                });

                startFuture.complete();
            } else {
                startFuture.fail(asyncResult.cause());
            }
        });
    }

    List<InetAddress> listAllBroadcastAddresses() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces
                = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(a -> a.getBroadcast())
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
    }

    public void sendMessage(String message) throws SocketException {
        listAllBroadcastAddresses().stream().forEach(i->{
            sendSocket.send(node+" : "+message, 7415, i.getHostAddress(), asyncResult -> {
                if(asyncResult.succeeded()){
                    System.out.println("Message broadcasted by : "+node+" and Message: "+message);
                }
            });
        });

    }

    public void stop(Future<Void> stopFuture){
        this.sendSocket.close(r1->{
            if(r1.succeeded()){
                this.receiveSocket.close(r2->{
                    if(r2.succeeded())
                        stopFuture.complete();
                    else
                        stopFuture.fail(r2.cause());
                });
            }
            else
                stopFuture.fail(r1.cause());
        });
    }
}
