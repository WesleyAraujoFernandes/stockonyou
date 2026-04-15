package br.com.knowledge.stockonyou.exception;

public class CommandItemNotFoundExeption extends RuntimeException {
    public CommandItemNotFoundExeption(String message) {
        super(message);
    }
}
