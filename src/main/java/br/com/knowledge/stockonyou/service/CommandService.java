package br.com.knowledge.stockonyou.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import br.com.knowledge.stockonyou.repository.CommandRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommandService {
    private final CommandRepository repository;

    public ResponseEntity<Void> createCommand() {

    }
}
