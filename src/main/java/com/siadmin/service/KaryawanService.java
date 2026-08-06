package com.siadmin.service;

import com.siadmin.model.Karyawan;
import com.siadmin.repository.KaryawanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KaryawanService {

    private final KaryawanRepository karyawanRepository;

    public KaryawanService(KaryawanRepository karyawanRepository) {
        this.karyawanRepository = karyawanRepository;
    }

    public List<Karyawan> findAll() {
        return karyawanRepository.findAll();
    }

    public List<Karyawan> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return karyawanRepository.findByNamaLengkapContainingIgnoreCaseOrNipContainingIgnoreCase(keyword, keyword);
    }

    public Karyawan findById(Long id) {
        return karyawanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Karyawan dengan id " + id + " tidak ditemukan"));
    }

    public boolean isNipDuplicate(String nip, Long excludeId) {
        return karyawanRepository.findByNip(nip)
                .map(k -> !k.getId().equals(excludeId))
                .orElse(false);
    }

    public Karyawan save(Karyawan karyawan) {
        return karyawanRepository.save(karyawan);
    }

    public void deleteById(Long id) {
        karyawanRepository.deleteById(id);
    }

    public long count() {
        return karyawanRepository.count();
    }
}
