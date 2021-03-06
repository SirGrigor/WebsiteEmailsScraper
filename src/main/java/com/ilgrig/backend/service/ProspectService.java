package com.ilgrig.backend.service;

import com.ilgrig.backend.entity.Prospect;
import com.ilgrig.backend.repository.ProspectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProspectService {
    private final ProspectRepository prospectRepository;

    public ProspectService(ProspectRepository prospectRepository) {
        this.prospectRepository = prospectRepository;
    }

    public List<Prospect> getAllProspects(){
       return prospectRepository.findAll();
    }

}
