package br.com.knowledge.stockonyou.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.dto.request.SupplierRequest;
import br.com.knowledge.stockonyou.mapper.SupplierMapper;
import br.com.knowledge.stockonyou.model.Supplier;
import br.com.knowledge.stockonyou.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id " + id));
    }

    public Supplier create(SupplierRequest request) {
        Supplier supplier = supplierMapper.toEntity(request);
        return supplierRepository.save(supplier);
    }

    public void delete(Long id) {
        supplierRepository.deleteById(id);
    }

    public Supplier update(Long id, SupplierRequest request) {
        Supplier existing = findById(id);
        if (existing.getId() == null) {
            throw new RuntimeException("Supplier not found with id " + id);
        }
        Supplier updated = supplierMapper.toEntity(request);
        updated.setId(existing.getId()); // mantem o ID original
        return supplierRepository.save(updated);
    }
}
