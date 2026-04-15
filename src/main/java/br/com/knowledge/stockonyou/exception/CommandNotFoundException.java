package br.com.knowledge.stockonyou.exception;

public class CommandNotFoundException extends RuntimeException {
    public CommandNotFoundException(Long id) {
        super("Command not found with id " + id);
    }

    public CommandNotFoundException(String message) {
        super(message);
    }
}
