package br.com.redemaisfarma.adapters.outbound.sms.adapter;

public interface SmsSenderAdapter {
    String send(String destination, String message);
}

