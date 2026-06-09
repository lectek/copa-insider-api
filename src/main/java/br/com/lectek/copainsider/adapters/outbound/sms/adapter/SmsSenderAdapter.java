package br.com.lectek.copainsider.adapters.outbound.sms.adapter;

public interface SmsSenderAdapter {
    String send(String destination, String message);
}

