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
                    data.getFee1(),
                    data.getPath1()
            );
            EMDto.Hospital hospital2 = new EMDto.Hospital(
                    data.getHospital2(),
                    data.getAddr2(),
                    data.getTel2(),
                    data.getEta2(),
                    data.getDist2(),
                    data.getFee2(),
                    data.getPath2()
            );
            EMDto.Hospital hospital3 = new EMDto.Hospital(
                    data.getHospital3(),
                    data.getAddr3(),
                    data.getTel3(),
                    data.getEta3(),
                    data.getDist3(),
                    data.getFee3(),
                    data.getPath3()
            );

            hospitals.add(hospital1);
            hospitals.add(hospital2);
            hospitals.add(hospital3);
        }

        return hospitals;
    }

    public List<EMDto.HospitalLog> transformLog(List<EMDto.Log> logs) {

        List<EMDto.HospitalLog> results = new ArrayList<>();
        for(EMDto.Log log : logs) {
            EMDto.HospitalLog result = new EMDto.HospitalLog();

            result.setDatetime(log.getDatetime());
            result.setInput_text(log.getInput_text());
            result.setInput_latitude(log.getInput_latitude());
            result.setInput_longitude(log.getInput_longitude());
            result.setEm_class(log.getEm_class());

            // 응급 등급에 따라 병원 추천 정보 업데이트
            if (log.getEm_class() >= 1 && log.getEm_class() <= 3) {
                EMDto.Hospital hospital1 = new EMDto.Hospital(
                        log.getHospital1(),
                        log.getAddr1(),
                        log.getTel1(),
                        log.getEta1(),
                        log.getDist1(),
                        log.getFee1(),
                        null
                );
                EMDto.Hospital hospital2 = new EMDto.Hospital(
                        log.getHospital2(),
                        log.getAddr2(),
                        log.getTel2(),
                        log.getEta2(),
                        log.getDist2(),
                        log.getFee2(),
                        null
                );
                EMDto.Hospital hospital3 = new EMDto.Hospital(
                        log.getHospital3(),
                        log.getAddr3(),
                        log.getTel3(),
                        log.getEta3(),
                        log.getDist3(),
                        log.getFee3(),
                        null
                );

                result.setHospital1(hospital1);
                result.setHospital2(hospital2);
                result.setHospital3(hospital3);
            }
            else {
                result.setHospital1(null);
                result.setHospital2(null);
                result.setHospital3(null);
            }

            results.add(result);
        }

        return results;
    }
}
