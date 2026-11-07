package com.example.sesac_spring_boot_study;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudyController {
    private final StudyService service;

    public StudyController(StudyService service) {
        this.service = service;
    }

    @PostMapping("/study/participants")
    public String participate(String name) {
        return service.participate(name);
    }

    @GetMapping("/study/participants")
    public String getParticipants() {
        return service.getParticipants();
    }

    @PostMapping("/study/cancel")
    public String cancel(
            @RequestParam(name = "memberId", required = false) String memberId) {
        if (memberId == null || memberId.isBlank()) {
            return "memberId를 입력해주세요";
        }

        final int parsedId;
        try {
            parsedId = Integer.parseInt(memberId.strip());
        } catch (NumberFormatException e) {
            return "memberId는 정수로 입력해주세요";
        }
        return service.cancel(parsedId);
    }

    @PostMapping("/study/capacity")
    public String capacity(
            @RequestParam(name = "capacity", required = false) String capacity) {
        if (capacity == null || capacity.isBlank()) {
            return "capacity를 입력해주세요 / 현재 정원=" + service.getCapacity();
        }

        final int parsedCapacity;
        try {
            parsedCapacity = Integer.parseInt(capacity.strip());
        } catch (NumberFormatException e) {
            return "capacity는 1~10 사이의 정수로 입력해주세요 / 현재 정원=" + service.getCapacity();
        }
        return service.changeCapacity(parsedCapacity);
    }
}
