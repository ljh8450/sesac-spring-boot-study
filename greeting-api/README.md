# 인사 API와 스터디 참가 신청

Spring Boot로 인사 API와 스터디 신청·취소·정원 변경 기능을 구현한 실습입니다. 스터디 입력 화면은 외부 라이브러리 없이 HTML·CSS·JavaScript로 작성했습니다.

## 구현 내용

- `GET /hello`: 고정 인사 응답
- `GET /greet?name=홍길동`: 이름을 포함한 인사 응답, 이름 생략 시 방문자 사용
- `POST /study/participants`: 이름 정리·검증·중복 검사 후 확정 또는 대기 등록
- `GET /study/participants`: 확정·대기 명단과 참가자의 id·이름·상태 조회
- `POST /study/cancel`: id로 신청 취소, 확정자 취소 시 첫 대기자 승격
- `POST /study/capacity`: 1~10 정원 변경, 확정 인원보다 작은 정원 거절, 빈자리만큼 순서대로 승격
- `/study.html`: 신청·취소·정원 변경·명단 조회와 요청·응답 기록 확인

명단과 정원은 서버 메모리에 저장됩니다. 서버를 다시 시작하면 명단은 비워지고 정원은 3명으로 초기화됩니다.

## 실행 방법

Java 21이 필요합니다. 처음 실행할 때 Gradle과 의존성을 다운로드할 수 있는 네트워크 환경이 필요합니다.

Clone한 저장소 루트에서 Windows PowerShell로 실행합니다.

```powershell
cd greeting-api
.\gradlew.bat bootRun
```

macOS·Linux에서는 같은 폴더에서 다음 명령을 실행합니다.

```sh
sh ./gradlew bootRun
```

브라우저에서 <http://localhost:8080/study.html>을 엽니다. 기본 포트는 8080입니다. 서버 종료는 실행 터미널에서 `Ctrl+C`를 누릅니다.

## API 사용 방법

POST 요청의 Content-Type은 `application/x-www-form-urlencoded`입니다. 화면에서는 `URLSearchParams`를 사용합니다.

| 기능 | 요청 | 요청 값 |
| --- | --- | --- |
| 인사 | `GET /hello` | 없음 |
| 이름 인사 | `GET /greet` | `name` (선택) |
| 신청 | `POST /study/participants` | `name` |
| 명단 조회 | `GET /study/participants` | 없음 |
| 취소 | `POST /study/cancel` | `memberId` |
| 정원 변경 | `POST /study/capacity` | `capacity` |

취소에는 신청 응답이나 명단의 실제 id를 사용합니다. id가 연속한다고 가정하지 않습니다. 비즈니스 실패도 문자열로 응답하므로 HTTP 상태와 함께 응답 본문을 확인합니다.

## 인사 API 확인 결과

2026-11-07에 실행 중인 localhost:8080 서버에 HTTP GET 요청을 보내 확인했습니다. 세 요청 모두 HTTP 200으로 기대 응답과 일치했습니다.

| 요청 URL | 기대 응답 | 실제 응답 | 통과 여부 |
| --- | --- | --- | --- |
| `http://localhost:8080/hello` | `Hello, Spring!` | `Hello, Spring!` | 통과 (HTTP 200) |
| `http://localhost:8080/greet` | `안녕하세요, 방문자님` | `안녕하세요, 방문자님` | 통과 (HTTP 200) |
| `http://localhost:8080/greet?name=홍길동` | `안녕하세요, 홍길동님` | `안녕하세요, 홍길동님` | 통과 (HTTP 200) |

## 스터디 QA 체크리스트

상세 실행 순서, 사전 조건, 입력, 기대 결과, 실제 결과와 통과 여부는 [QA 체크리스트](../docs/qa-checklist.md)에 기록했습니다.

2026-11-07 검증 결과입니다. 전체 판정은 **부분 통과**입니다.

| 검증 범위 | 결과 | 확인 방법 |
| --- | --- | --- |
| 컨트롤러·서비스 | 43건 통과 / 실패 0건 | 제출 소스의 Java 메서드를 별도 인스턴스에서 직접 호출 |
| 인사 API | 3건 통과 | 실행 중인 localhost:8080 서버에 실제 HTTP GET 요청 |
| 신청 화면 제공 | `/study.html` HTTP 200 | 페이지 응답 확인 |
| 화면 조작·Network | 9개 확인 항목 미실행 | 브라우저 도구의 현재 URL 확인 제한으로 중단 |

인사 API 3건은 직접 호출 43건에도 포함된 항목입니다. 이를 별개 시나리오 46건으로 합산하지 않습니다. 직접 호출은 HTTP 요청 바인딩과 프론트엔드 동작까지 검증한 결과가 아닙니다. 기존 서버에 참가자가 있어 명단을 변경하지 않고, 신청·취소·정원 변경은 별도 서비스 인스턴스에서 실행했습니다.

| 이번 세션의 변경 | QA 확인 내용 | 결과 |
| --- | --- | --- |
| memberId·capacity 입력 처리 | 누락·빈 값·공백·문자·정수 범위 초과 입력 거절, 명단·정원 유지 | 직접 호출 통과 |
| 신청 응답 보완 | 확정·대기 모두 실제 id·이름·상태 반환 | 직접 호출 통과 |
| 취소 후 승격 | 첫 대기자 1명 승격, 기존 id 유지, 취소·승격 응답 | 직접 호출 통과 |
| 명단 조회 보완 | 확정·대기자의 id·이름·상태 표시 | 직접 호출 통과 |
| 정원 변경 완성 | 1~10 허용, 확정 인원 미만 거절, 같은 정원 유지, 빈자리만큼 순서대로 승격 | 직접 호출 통과 |
| study.html 추가 | 순수 HTML·CSS·JavaScript, URLSearchParams와 form 전송, 요청 후 명단 갱신 | 코드 확인 및 페이지 HTTP 200, 화면·Network 미검증 |

## 검증 상태

Java 21과 캐시된 Spring 7.0.10 JAR로 대상 클래스와 임시 검증 코드를 생성해 실행했습니다. javac의 종료 정리 중 파일 접근 예외가 출력되어 정식 Gradle 빌드 성공으로 판정하지 않았습니다. 브라우저 자동화는 Windows의 현재 URL을 확인하지 못해 중단됐습니다. 사용자가 화면과 Network에서 재확인할 순서는 상세 체크리스트를 참고하세요.

순차 요청 검증에서 기능 실패는 발견하지 않았습니다. 다만 다음 확인이 남아 있습니다.

- 공유 Map·id·정원 변경이 동기화돼 있지 않아 동시 요청 시 경쟁 가능성이 있습니다. 동시성 문제는 이번 QA에서 재현하거나 검증하지 않았습니다.
- 없는 id·이미 취소한 id의 응답이 `올바르지 않은 입력입니다`여서 실패 이유를 더 명확하게 안내할 여지가 있습니다.
- 화면 입력·HTTP 바인딩·Content-Type·Payload·화면 자동 갱신은 직접 브라우저에서 확인해야 합니다. 실패가 발견되면 수정 후 동일 조건으로 다시 실행하고 기록합니다.

## 관련 Issue·PR

- 원본 저장소 Issue: 생성 후 실제 주소 기입
- 원본 저장소 PR: 생성 후 실제 주소 기입

제출 대상은 `sesac-ddm-4-web/sesac-spring-boot-study`의 `main`이며, 강사 리뷰 후 PR·Issue를 닫는 방식입니다. 직접 병합하지 않습니다.
