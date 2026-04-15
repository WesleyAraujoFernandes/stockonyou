package br.com.knowledge.stockonyou.exception;

public class CommandClosedException extends RuntimeException {
    public CommandClosedException(Long commandId) {
        super("Command is closed and cannot be modified: ID:" + commandId);
    }

    public CommandClosedException(String message) {
        super(message);
    }
}
