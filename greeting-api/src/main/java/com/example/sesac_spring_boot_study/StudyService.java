package com.example.sesac_spring_boot_study;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StudyService {
    private Map<Integer, String> participants = new HashMap<>();
    private Map<Integer, String> ready = new LinkedHashMap<>();

    private int id = 0;
    private int capacity = 3;

    public String participate(String name) {
        if (name == null) return "입력이 완료되지 않았습니다";
        name = name.strip();
        if (name.isEmpty()) return "이름을 정확히 입력해주세요";
        if (participants.containsValue(name) || ready.containsValue(name)) return "이미 등록된 사용자입니다";
        if (participants.size() >= capacity) {
            ready.put(++id, name);
            return "신청 완료: id=" + id + ", 이름=" + name + ", 상태=대기";
        }
        participants.put(++id, name);
        return "신청 완료: id=" + id + ", 이름=" + name + ", 상태=확정";
    }

    public String getParticipants() {
        if (participants.isEmpty() && ready.isEmpty()) return "참가자가 없습니다";

        StringJoiner result = new StringJoiner("\n");
        result.add("확정 명단:");
        if (participants.isEmpty()) result.add("없음");
        for (Map.Entry<Integer, String> participant : participants.entrySet()) {
            result.add("id=" + participant.getKey() + ", 이름=" + participant.getValue()
                    + ", 상태=확정");
        }

        result.add("대기 명단 (신청 순서):");
        if (ready.isEmpty()) result.add("없음");
        for (Map.Entry<Integer, String> participant : ready.entrySet()) {
            result.add("id=" + participant.getKey() + ", 이름=" + participant.getValue()
                    + ", 상태=대기");
        }
        return result.toString();
    }

    public String cancel(int memberId) {
        if (participants.containsKey(memberId)) {
            String canceledName = participants.remove(memberId);
            String result = "취소 완료: id=" + memberId + ", 이름=" + canceledName;

            if (!ready.isEmpty()) {
                Iterator<Map.Entry<Integer, String>> iterator = ready.entrySet().iterator();
                Map.Entry<Integer, String> promoted = iterator.next();
                int promotedId = promoted.getKey();
                String promotedName = promoted.getValue();
                iterator.remove();
                participants.put(promotedId, promotedName);

                return result + " / 승격: id=" + promotedId
                        + ", 이름=" + promotedName + ", 상태=확정";
            }
            return result + " / 승격 대상 없음";
        }
        else if (ready.containsKey(memberId)) {
            ready.remove(memberId);
            return "" + memberId + "번 대기자 취소 처리되었습니다";
        }
        else {
            return "올바르지 않은 입력입니다";
        }
    }

    public String changeCapacity(int num) {
        if (num < 1 || num > 10) {
            return "정원 변경 실패: 1~10 사이의 정수만 가능합니다 / 현재 정원=" + capacity;
        }
        if (num < participants.size()) {
            return "정원 변경 실패: 현재 확정 인원보다 작을 수 없습니다 / 현재 정원=" + capacity;
        }
        if (num == capacity) {
            return "기존 정원과 같습니다. 명단을 유지합니다 / 현재 정원=" + capacity;
        }

        capacity = num;
        StringJoiner promotedMembers = new StringJoiner(" / ");
        Iterator<Map.Entry<Integer, String>> iterator = ready.entrySet().iterator();
        while (participants.size() < capacity && iterator.hasNext()) {
            Map.Entry<Integer, String> promoted = iterator.next();
            int promotedId = promoted.getKey();
            String promotedName = promoted.getValue();
            iterator.remove();
            participants.put(promotedId, promotedName);
            promotedMembers.add("id=" + promotedId + ", 이름=" + promotedName + ", 상태=확정");
        }

        return "정원 변경 완료: 현재 정원=" + capacity + " / 승격: "
                + (promotedMembers.length() == 0 ? "없음" : promotedMembers.toString());
    }

    public int getCapacity() {
        return capacity;
    }
}
