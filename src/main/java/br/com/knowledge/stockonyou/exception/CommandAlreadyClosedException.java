package br.com.knowledge.stockonyou.exception;

public class CommandAlreadyClosedException extends RuntimeException {
    public CommandAlreadyClosedException(Long commandId) {
        super("Command already closed and cannot be modified: ID:" + commandId);
    }

    public CommandAlreadyClosedException(String message) {
        super(message);
    }
}
