# 취미 위시리스트와 예산 추천

Spring Boot 기반으로 취미 활동에 필요한 상품을 등록하고, 사용자의 예산에 맞는 상품을 추천하는 웹 서비스입니다.

Controller-Service-Repository 계층 구조를 적용하고, 순수 HTML·CSS·JavaScript로 작성한 프론트엔드와 REST API를 연동했습니다.

## 1. 개발 환경

| 구분 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build Tool | Gradle |
| Frontend | HTML, CSS, JavaScript |
| API 통신 | Fetch API |
| 데이터 저장 | ArrayList 기반 In-Memory Repository |
| Test | JUnit 5, Spring Boot Test |

외부 프론트엔드 라이브러리 및 CDN을 사용하지 않았습니다.

## 2. 주요 기능

### 상품 등록

- 상품명과 가격을 입력하여 상품 등록
- `AtomicLong`을 활용한 상품 ID 자동 생성
- 등록한 상품을 메모리 저장소에 저장

### 상품 조회

- 전체 상품 목록 조회
- 상품 ID를 통한 상세 조회
- Stream API를 활용한 ID 검색

### 예산별 상품 추천

- 사용자가 입력한 예산 이하의 상품 필터링
- 가격 오름차순 정렬
- 최대 3개의 상품 추천
- 음수 예산 입력 시 예외 발생

### 프론트엔드

- 상품 등록 폼
- 전체 상품 목록 및 상세 정보 표시
- 예산 입력 및 추천 결과 표시
- `fetch()`를 이용한 REST API 연동

## 3. 프로젝트 구조

```text
personal-project/
├── src/
│   ├── main/
│   │   ├── java/com/example/personal_project/
│   │   │   ├── application/
│   │   │   │   └── ItemService.java
│   │   │   ├── domain/
│   │   │   │   ├── CollectionItem.java
│   │   │   │   └── ItemRepository.java
│   │   │   ├── infrastructure/
│   │   │   │   └── ListItemRepository.java
│   │   │   └── presentation/
│   │   │       ├── ItemController.java
│   │   │       └── ItemDto.java
│   │   └── resources/
│   │       └── static/
│   │           └── index.html
│   └── test/
│       └── java/
├── build.gradle
├── gradlew
└── README.md
```

## 4. API 명세

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/items` | 상품 등록 |
| GET | `/items` | 전체 상품 조회 |
| GET | `/items/{id}` | 상품 상세 조회 |
| GET | `/recommend?budget=30000` | 예산별 추천 |

### 상품 등록 요청 예시

```http
POST /items
Content-Type: application/json
```

```json
{
  "name": "키보드",
  "price": 25000
}
```

성공 응답:

```text
등록
```

### 예산별 추천 예시

```http
GET /recommend?budget=30000
```

예산 이하 상품 중 가격이 낮은 순서대로 최대 3개를 반환합니다.

## 5. 실행 방법

프로젝트 디렉터리로 이동합니다.

```bash
cd personal-project
```

Spring Boot 애플리케이션을 실행합니다.

Windows:

```powershell
.\gradlew.bat bootRun
```

macOS / Linux:

```bash
./gradlew bootRun
```

실행 후 브라우저에서 접속합니다.

```text
http://localhost:8080
```

## 6. 테스트

Windows:

```powershell
.\gradlew.bat test
```

macOS / Linux:

```bash
./gradlew test
```

### 테스트 및 디버깅 내용

**1. Repository 메서드 호출 오류**

- 원인: `ItemRepository.add()`를 정적 메서드처럼 호출
- 해결: 생성자 주입된 `itemRepository.add()`로 변경

**2. 상품 ID 조회 오류**

- 원인: 상품 ID를 `ArrayList`의 인덱스로 사용하여 `IndexOutOfBoundsException` 발생
- 해결: Stream API로 상품의 실제 ID를 비교하도록 변경

### 최종 검증 결과

| 검증 항목 | 결과    |
|---|-------|
| 상품 등록 | 확인 완료 |
| 전체 상품 조회 | 확인 완료 |
| 상품 ID 조회 | 확인 완료 |
| 예산별 추천 | 확인 완료 |
| 프론트엔드 API 연동 | 확인 완료 |
| `./gradlew test` | 확인 완료 |

## 7. 구현 시 고려 사항

- **계층 분리:** Controller는 HTTP 요청 처리, Service는 비즈니스 로직, Repository는 데이터 저장 및 조회를 담당하도록 구성했습니다.
- **의존성 주입:** `ItemRepository` 인터페이스를 사용하여 저장소 구현에 대한 의존성을 줄였습니다.
- **Stream API:** 예산 필터링, 가격 정렬, 최대 3개 제한을 선언적으로 구현했습니다.
- **메모리 저장소:** 데이터베이스 없이 기능을 구현했으며, 애플리케이션 재시작 시 저장된 데이터는 초기화됩니다.
