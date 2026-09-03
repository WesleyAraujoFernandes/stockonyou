package br.com.knowledge.stockonyou.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.api.dto.ItemVendaRequestDTO;
import br.com.knowledge.stockonyou.api.dto.VendaRequestDTO;
import br.com.knowledge.stockonyou.api.dto.VendaResponseDTO;
import br.com.knowledge.stockonyou.api.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.api.model.Cliente;
import br.com.knowledge.stockonyou.api.model.ItemVenda;
import br.com.knowledge.stockonyou.api.model.Produto;
import br.com.knowledge.stockonyou.api.model.Venda;
import br.com.knowledge.stockonyou.api.repository.ClienteRepository;
import br.com.knowledge.stockonyou.api.repository.ProdutoRepository;
import br.com.knowledge.stockonyou.api.repository.VendaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendaService {
    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public VendaResponseDTO realizarVenda(VendaRequestDTO dto) {
        String username = "Sistema";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            username = jwt.getClaimAsString("preferred_username");
        }

        Long idBusca = (dto.clienteId() != null) ? dto.clienteId() : 1L;
        Cliente cliente = clienteRepository.findById(idBusca)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente Padrão (ID: 1) não cadastrado no banco."));
        Venda venda = Venda.builder()
                .dataVenda(LocalDateTime.now())
                .clienteNome(cliente.getNome())
                .cliente(cliente)
                .usuarioNome(username)
                .valorTotal(BigDecimal.ZERO)
                .itens(new ArrayList<>())
                .build();
        BigDecimal valorTotalVenda = BigDecimal.ZERO;

        for (ItemVendaRequestDTO itemDto : dto.itens()) {
            Produto produto = produtoRepository.findById(itemDto.produtoId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado ID: " + itemDto.produtoId()));
            if (produto.getQuantidade() < itemDto.quantidade()) {
                throw new IllegalArgumentException("Estoque insuficiente para o produto: " + produto.getNome() + ". Estoque atual: " + produto.getQuantidade());
            }
            produto.setQuantidade(produto.getQuantidade() - itemDto.quantidade());
            produtoRepository.save(produto);
            BigDecimal subtotal = itemDto.precoUnitario().multiply(BigDecimal.valueOf(itemDto.quantidade()));
            valorTotalVenda = valorTotalVenda.add(subtotal);
            ItemVenda itemVenda = ItemVenda.builder()
                .venda(venda)
                .produto(produto)
                .quantidade(itemDto.quantidade())
                .precoUnitario(itemDto.precoUnitario())
                .subtotal(subtotal)
                .build();
            venda.getItens().add(itemVenda);
        }
        venda.setValorTotal(valorTotalVenda);
        Venda vendaSalva = vendaRepository.save(venda);
        return VendaResponseDTO.fromEntity(vendaSalva);
    }
}
