package com.example.em.service;

import com.example.em.domain.EMData;
import com.example.em.domain.EMDto;
import com.example.em.repository.EMRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EMService {
    private final EMRepository emRepository;

    public EMData saveEMD(EMData data) {

        return emRepository.save(data);
    }

    @Transactional(readOnly = true)
    public Page<EMData> getLogList(Pageable pageable) {

        return emRepository.findAll(pageable);
    }

    public List<EMDto.Hospital> transformData(EMDto.Info data) {

        List<EMDto.Hospital> hospitals = new ArrayList<>();

        // 응급 등급에 따라 병원 추천 정보 업데이트
        if (data.getEm_class() >= 1 && data.getEm_class() <= 3) {

            EMDto.Hospital hospital1 = new EMDto.Hospital(
                    data.getHospital1(),
                    data.getAddr1(),
                    data.getTel1(),
                    data.getEta1(),
                    data.getDist1(),
                    data.getFee1()
            );
            EMDto.Hospital hospital2 = new EMDto.Hospital(
                    data.getHospital2(),
                    data.getAddr2(),
                    data.getTel2(),
                    data.getEta2(),
                    data.getDist2(),
                    data.getFee2()
            );
            EMDto.Hospital hospital3 = new EMDto.Hospital(
                    data.getHospital3(),
                    data.getAddr3(),
                    data.getTel3(),
                    data.getEta3(),
                    data.getDist3(),
                    data.getFee3()
            );

            hospitals.add(hospital1);
            hospitals.add(hospital2);
            hospitals.add(hospital3);
        }

        return hospitals;
    }
}
