# spring-gift-refactoring

## 1단계 - 리팩터링 준비하기

> 핵심 목표: 작동을 바꾸기 쉬운 상태를 만든다. 구조 변경을 통해 변경 난이도를 낮추되 작동은 유지한다.

---

## 기능 목록

### 1. 스타일 정리 (작동 변경 없음)

- [x] `@Autowired` 제거 통일 — 단일 생성자 빈에서 불필요한 `@Autowired` 제거
  - 대상: `MemberController`, `AdminMemberController`, `AuthenticationResolver`, `JwtProvider`
  - 근거: Spring 4.3+부터 단일 생성자 빈은 `@Autowired` 없이도 자동 주입되므로 불필요한 어노테이션을 제거하여 코드 노이즈를 줄인다.
- [x] `@RequestMapping` 속성 스타일 통일 — `path = "/..."` 대신 `"/..."` 축약형으로 통일
  - 대상: `KakaoAuthController`, `OptionController`
  - 근거: `value`가 기본 속성이므로 `path = "..."` 명시는 불필요한 장식이며, 축약형으로 통일하여 일관성을 높인다.
- [x] 변수 선언 스타일 통일 — `var` 제거 후 명시적 타입으로 통일, 불필요한 `final` 제거
  - 대상: `OrderController`, `WishController`, `MemberController`, `AdminMemberController`, `AuthenticationResolver`
  - 근거: 명시적 타입 선언으로 가독성을 높이고, 지역 변수의 `final`은 재할당 방지 실익이 적어 제거하여 선언부를 간결하게 한다.
- [x] 주석/Javadoc 스타일 통일 — Javadoc(`/** */`) 또는 블록 주석(`/* */`) 중 하나로 통일
  - 대상: `WishController`, `OrderController`, `Order`, `Wish`, `Member`의 한 줄 주석(`//`)을 Javadoc(`/** */`)으로 변환
  - 근거: 기존 `member`·`auth` 패키지에서 이미 Javadoc 스타일을 사용하고 있으므로, 나머지 한 줄 주석도 Javadoc으로 통일하여 프로젝트 전체의 주석 형식을 일관되게 한다.
- [x] HTTP 상태 코드 표현 통일 — 매직 넘버(`401`, `403`) 대신 `HttpStatus` 열거형 사용
  - 대상: `WishController`, `OrderController`
  - 근거: 매직 넘버는 의미를 즉시 파악하기 어려우므로, `HttpStatus.UNAUTHORIZED`·`HttpStatus.FORBIDDEN` 열거형으로 대체하여 의도를 명확히 한다.
- [x] Stream 종결 연산 통일 — `.collect(Collectors.toList())` → `.toList()`로 통일
  - 대상: `OptionController`
  - 근거: `.toList()`는 불변 리스트를 반환하며 내부 최적화가 적용되어 더 간결하고 효율적이다. 수집한 리스트를 응답으로 반환만 하므로 가변성이 불필요하여 안전하게 전환할 수 있다.
- [x] 에러 메시지 언어 통일 — 영어 메시지를 한국어로 통일
  - 대상: `MemberController`, `AdminMemberController`, `Member`
  - 근거: Validator와 도메인 로직 대부분이 이미 한국어 메시지를 사용하므로, 나머지 영어 메시지도 한국어로 통일하여 사용자에게 일관된 오류 안내를 제공한다.
- [x] `ResponseEntity` 제네릭 타입 통일 — `ResponseEntity<?>` 대신 구체적 타입 사용
  - 대상: `OrderController`의 `getOrders`(`Page<OrderResponse>`), `createOrder`(`OrderResponse`)
  - 근거: 와일드카드(`?`)는 반환 타입 정보를 숨겨 API 문서 자동 생성이나 호출부에서 타입 추론이 불가능하므로, 구체적 제네릭 타입을 명시하여 컴파일 타임 타입 안전성과 가독성을 높인다.

### 2. 불필요한 코드 제거 (작동 변경 없음)

- [x] `OrderController`에서 미사용 `WishRepository` 의존성 제거
  - 필드, 생성자 파라미터, import 모두 제거 (주석 "6. cleanup wish"도 함께 제거)
  - 근거: 주석에 위시 정리 의도가 있으나 실제 구현이 없는 미사용 의존성이므로, 제거하여 불필요한 빈 주입과 혼란을 방지한다.
- [x] `OptionController`에서 `import java.util.stream.Collectors` 제거 (`.toList()` 전환 후)
  - 근거: `.toList()` 전환으로 `Collectors` 클래스를 더 이상 참조하지 않으므로, 미사용 import를 제거하여 불필요한 의존을 없앤다.
- [x] 각 Controller의 중복 `@ExceptionHandler` 제거
  - `ProductController`, `OptionController`, `MemberController`의 동일한 `IllegalArgumentException` 핸들러를 `GlobalExceptionHandler`(`@RestControllerAdvice`)로 통합
  - 근거: 동일한 예외 처리 로직이 3개 컨트롤러에 중복되어 있으므로, `@RestControllerAdvice`로 한 곳에서 관리하여 변경 시 누락 위험을 없앤다.

### 3. 서비스 계층 추출 (구조 변경, 작동 변경 없음)

- [x] **ProductService** 추출
  - `ProductController`의 상품 CRUD 로직 (이름 검증, 카테고리 조회, 생성/수정/삭제)
  - `AdminProductController`의 상품 CRUD 로직 (동일 로직 중복 제거)
  - 근거: 두 컨트롤러에 분산된 상품 비즈니스 로직을 `ProductService`로 통합하여 중복을 제거하고, 컨트롤러는 HTTP 요청/응답 처리만 담당하도록 역할을 분리한다.
- [x] **CategoryService** 추출
  - `CategoryController`의 카테고리 CRUD 로직 (조회, 생성, 수정, 삭제)
  - 근거: `CategoryController`가 `CategoryRepository`를 직접 사용하던 구조에서 `CategoryService`를 추출하여 비즈니스 로직을 분리하고, 컨트롤러는 HTTP 요청/응답 처리만 담당하도록 역할을 나눈다. `updateCategory`의 null 체크 패턴을 `orElseThrow`로 교체하여 `GlobalExceptionHandler`와 일관된 예외 처리를 적용한다.
- [x] **MemberService** 추출
  - `MemberController`의 회원 가입/로그인 로직
  - `AdminMemberController`의 회원 관리/포인트 충전 로직
  - 근거: 회원 가입·로그인·관리 로직을 `MemberService`로 통합하여, 컨트롤러는 HTTP/뷰 처리만 담당하고 비즈니스 로직은 서비스에서 일관되게 관리한다.
- [x] **OptionService** 추출
  - `OptionController`의 옵션 CRUD 로직 (이름 검증, 중복 확인, 최소 1개 보장 규칙)
  - 근거: `OptionController`가 `OptionRepository`·`ProductRepository`를 직접 사용하며 이름 검증, 중복 확인, 최소 1개 보장 등 비즈니스 로직을 수행하고 있으므로, `OptionService`로 추출하여 컨트롤러는 HTTP 요청/응답 처리만 담당하도록 역할을 분리한다.
- [x] **OrderService** 추출 (`@Transactional` 적용)
  - `OrderController`의 주문 생성 로직 (재고 차감 → 포인트 차감 → 주문 저장 → 카카오 메시지)
  - 근거: 주문 생성 시 재고 차감·포인트 차감·주문 저장이 원자적으로 처리되어야 하므로 `@Transactional`을 적용한 서비스로 추출하고, 컨트롤러는 인증 확인과 HTTP 응답 처리만 담당하도록 분리한다.
- [x] **WishService** 추출
  - `WishController`의 위시리스트 조회/추가/삭제 로직 (중복 확인, 소유권 검증)
  - 근거: `WishController`의 비즈니스 로직을 `WishService`로 추출하여 컨트롤러는 HTTP 요청/응답 처리만 담당하도록 역할을 분리한다. 소유권 검증 실패(403)를 예외 기반으로 처리하기 위해 `ForbiddenException`을 추가하고 `GlobalExceptionHandler`에 핸들러를 등록한다. `addWish`의 중복 확인·생성 분기는 HTTP 상태 코드 분기가 아닌 비즈니스 규칙이므로 서비스 내부에서 멱등하게 처리하고, 컨트롤러는 항상 200 OK를 반환하도록 단순화한다.
- [x] **KakaoAuthController** 리팩터링
  - `MemberRepository`를 직접 사용하여 회원 조회·저장·카카오 액세스 토큰 업데이트를 수행하고 있음
  - 근거: `KakaoAuthController`가 `MemberRepository`를 직접 사용하여 회원 조회·생성·카카오 토큰 업데이트를 수행하고 있으므로, `MemberService`에 `findOrCreateByKakaoLogin` 메서드를 추가하고 컨트롤러는 서비스에 위임하도록 변경하여 회원 관련 영속성 로직을 한 곳에서 관리한다.
- [x] **AdminProductController** 리팩터링
  - `CategoryRepository`를 직접 사용하여 카테고리 목록을 조회하고 있음
  - 근거: `AdminProductController`가 `CategoryRepository`를 직접 사용하여 카테고리 목록을 조회하고 있으므로, 이미 존재하는 `CategoryService.getCategories()`에 위임하도록 변경하여 컨트롤러가 Repository에 직접 의존하지 않도록 한다.
- [x] 각 Controller가 요청 검증 + Service 위임만 수행하는지 최종 확인

---

## 구현 전략

### 작업 순서

작동 변경 없이 구조만 개선하므로, 안전한 순서대로 진행한다.

```
스타일 정리 → 불필요한 코드 제거 → 서비스 계층 추출
```

### 단계별 전략

**1단계: 스타일 정리**

- KtLint/포매터를 먼저 적용하여 자동으로 잡을 수 있는 항목을 처리한다.
- 이후 수동으로 `@Autowired`, `var`/명시적 타입, 주석 스타일 등을 통일한다.
- 한 가지 스타일 항목씩 커밋한다 (예: "style: remove unnecessary @Autowired annotations").

**2단계: 불필요한 코드 제거**

- IDE 정적 분석(미사용 import, 미사용 필드)을 활용하여 제거 대상을 식별한다.
- `OrderController`의 `WishRepository`는 주석에 의도("cleanup wish")가 있으나 미구현 상태이므로, 이 단계에서는 미사용 코드로 판단하여 제거한다.
- 제거 전 `git blame`으로 추가 의도를 확인한다.

**3단계: 서비스 계층 추출**

- 도메인별로 하나씩 Service를 추출하며, 각 Service 추출마다 별도 커밋한다.
- 추출 순서: 의존이 적은 것부터 → 의존이 많은 것 순서로 진행한다.
  1. `CategoryService` (의존 없음, 가장 단순)
  2. `ProductService` (Category 의존)
  3. `MemberService` (독립적)
  4. `OptionService` (Product 의존)
  5. `WishService` (Member, Product 의존)
  6. `OrderService` (Member, Option, KakaoMessageClient 의존 — 가장 복잡, `@Transactional` 필수)
- API Controller와 Admin Controller가 동일 Service를 공유하도록 하여 중복 로직을 제거한다.
- 각 추출 후 전체 테스트를 실행하여 작동이 유지되는지 확인한다.

### 커밋 컨벤션

[AngularJS Git Commit Message Conventions](https://gist.github.com/stephenparish/9941e89d80e2bc58a153) 을 따른다.

| 접두사 | 용도 |
|---|---|
| `style` | 스타일 정리 (코드 포맷, 네이밍 통일 등) |
| `refactor` | 구조 변경 (서비스 계층 추출 등) |
| `chore` | 불필요한 코드 제거, 빌드 설정 등 |
| `docs` | README 등 문서 작성 |

### 검증 방법

- 매 커밋 전 `./gradlew test` 전체 테스트 통과 확인
- `./gradlew ktlintCheck`로 스타일 위반 확인
- 구조 변경 커밋에 작동 변경이 섞이지 않았는지 `git diff`로 확인

---

## 테스트 전략

### 목표

리팩터링된 코드의 **정확성을 보장**하고, 이후 변경에 대한 **안전망**을 구축한다.

### 테스트 피라미드

```
        ╱  인수 테스트  ╲          ← API 전체 흐름 (Cucumber + REST Assured)
       ╱               ╲
      ╱  서비스 단위 테스트  ╲       ← 비즈니스 로직 (Mockito)
     ╱                    ╲
    ╱  도메인 모델 단위 테스트   ╲    ← 핵심 규칙 (순수 Java, 의존성 없음)
```

### 1단계: 도메인 모델 단위 테스트 (우선순위 높음)

Spring 컨텍스트 없이 순수 Java로 작성한다. 실행 속도가 빠르고 핵심 비즈니스 규칙을 촘촘하게 검증한다.

| 대상 | 테스트 항목 |
|---|---|
| `Member.deductPoint()` | 정상 차감, 잔액 부족 예외, 음수/0 금액 예외, 잔액 정확히 일치 |
| `Member.chargePoint()` | 정상 충전, 음수/0 금액 예외 |
| `Option.subtractQuantity()` | 정상 차감, 재고 부족 예외, 재고 정확히 일치 |

### 2단계: 인수 테스트 확장 (우선순위 높음)

기존 Cucumber 인프라를 활용하여 Order 외 도메인으로 확장한다.

| Feature | 주요 시나리오 |
|---|---|
| `product.feature` | 상품 CRUD, 이름 검증 실패, 존재하지 않는 카테고리 |
| `option.feature` | 옵션 추가/삭제, 중복 이름 방지, 마지막 옵션 삭제 방지 |
| `wish.feature` | 위시 추가/삭제, 중복 추가 멱등성, 타인 위시 삭제 시 403 |
| `member.feature` | 회원가입, 로그인, 중복 이메일 거부 |
| `category.feature` | 카테고리 CRUD |

### 3단계: 서비스 단위 테스트 (우선순위 보통)

Mockito로 Repository를 모킹하여 서비스 로직만 격리 테스트한다. 복잡한 서비스부터 작성한다.

| 대상 | 테스트 항목 |
|---|---|
| `OrderService` | 정상 주문 생성, 재고 부족 시 실패, 포인트 부족 시 실패, 카카오 토큰 없을 때 메시지 미전송 |
| `OptionService` | 마지막 옵션 삭제 방지, 중복 이름 검증, 존재하지 않는 상품 예외 |
| `WishService` | 중복 위시 멱등 처리, 타인 위시 삭제 시 ForbiddenException |
| `MemberService` | 중복 이메일 가입 거부, 비밀번호 불일치 로그인 실패 |

> `CategoryService`는 단순 CRUD이므로 인수 테스트로 충분히 커버되어 단위 테스트를 생략한다.

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 단위 테스트만 (Spring 컨텍스트 불필요)
./gradlew test --tests "gift.domain.*"

# 인수 테스트만 (Cucumber)
./gradlew test --tests "gift.CucumberTest"
```

---

## 도메인 모델 단위 테스트 기능 목록

Spring 컨텍스트 없이 순수 Java로 작성한다. 엔티티의 비즈니스 메서드가 핵심 규칙을 올바르게 지키는지 검증한다.

### 1. `Member` 단위 테스트 (`MemberTest`) — 7개 케이스

| 테스트 케이스 | 검증 내용 | 상태 |
|-------------|----------|------|
| `chargePoint` — 정상 충전 | 1000 충전 → 포인트 1000 | [ ] |
| `chargePoint` — 0 이하 금액 | 0 충전 → `IllegalArgumentException` | [ ] |
| `chargePoint` — 음수 금액 | -100 충전 → `IllegalArgumentException` | [ ] |
| `deductPoint` — 정상 차감 | 10000에서 3000 차감 → 포인트 7000 | [ ] |
| `deductPoint` — 잔액 정확히 일치 | 5000에서 5000 차감 → 포인트 0 | [ ] |
| `deductPoint` — 잔액 부족 | 1000에서 5000 차감 → `IllegalArgumentException` | [ ] |
| `deductPoint` — 0 이하 금액 | 0 차감 → `IllegalArgumentException` | [ ] |

### 2. `Option` 단위 테스트 (`OptionTest`) — 4개 케이스

| 테스트 케이스 | 검증 내용 | 상태 |
|-------------|----------|------|
| `subtractQuantity` — 정상 차감 | 재고 10에서 3 차감 → 재고 7 | [ ] |
| `subtractQuantity` — 재고 정확히 일치 | 재고 10에서 10 차감 → 재고 0 | [ ] |
| `subtractQuantity` — 재고 부족 | 재고 5에서 10 차감 → `IllegalArgumentException` | [ ] |
| `subtractQuantity` — 0개 차감 | 재고 변동 없음 확인 | [ ] |

---

## 도메인 모델 단위 테스트 작성 계획

### 작성 순서

| 순서 | 대상 | 케이스 수 | 이유 |
|------|------|---------|------|
| 1 | `MemberTest` | 7 | `OrderService.createOrder`의 포인트 차감 전제 조건 |
| 2 | `OptionTest` | 4 | `OrderService.createOrder`의 재고 차감 전제 조건 |
| **합계** | | **11** | |

### 파일 구조

```
src/test/java/gift/
├── member/
│   └── MemberTest.java          # (신규)
└── option/
    └── OptionTest.java          # (신규)
```

---

## 서비스 단위 테스트 기능 목록

Mockito로 Repository를 모킹하여 서비스 로직만 격리 테스트한다. `OrderService.createOrder`만 대상으로 한다.

### `OrderService.createOrder` 단위 테스트 (`OrderServiceTest`) — 6개 케이스

| 테스트 케이스 | 검증 내용 | 상태 |
|-------------|----------|------|
| 정상 주문 생성 | 옵션 재고 차감 + 회원 포인트 차감 + 주문 저장 확인 | [ ] |
| 존재하지 않는 옵션 | 없는 optionId → `NoSuchElementException` | [ ] |
| 재고 부족 | 재고보다 많은 수량 → `IllegalArgumentException` (옵션의 `subtractQuantity`에서 발생) | [ ] |
| 포인트 부족 | 잔액보다 큰 금액 → `IllegalArgumentException` (회원의 `deductPoint`에서 발생) | [ ] |
| 카카오 토큰이 없는 회원 | `kakaoAccessToken == null` → `kakaoMessageClient.sendToMe` 미호출 | [ ] |
| 카카오 메시지 전송 실패 | `sendToMe`에서 예외 발생 → 주문은 정상 저장 | [ ] |

---

## 서비스 단위 테스트 작성 계획

### 전제 조건

도메인 모델 단위 테스트(`MemberTest`, `OptionTest`)를 먼저 작성하여 엔티티의 비즈니스 메서드가 올바르게 동작함을 보장한 뒤 작성한다.

### 모킹 대상

| 의존성 | 모킹 방식 | 이유 |
|--------|---------|------|
| `OptionRepository` | `@Mock` | `findById`로 옵션 조회 |
| `MemberRepository` | `@Mock` | `save`로 포인트 차감 반영 |
| `OrderRepository` | `@Mock` | `save`로 주문 저장 |
| `KakaoMessageClient` | `@Mock` | 외부 API 호출 격리 |

### 파일 구조

```
src/test/java/gift/
└── order/
    └── OrderServiceTest.java    # (신규)
```

---

## 인수 테스트 기능 목록

### 현황

| 구분 | 상태 |
|------|------|
| 작성 완료 | `order.feature` — 7개 시나리오 |
| 미작성 | member, category, product, option, wish — 5개 도메인 |

### 1. 회원 (`member.feature`) — 5개 시나리오

| 시나리오 | 검증 내용 | 상태 |
|---------|----------|------|
| 회원가입 성공 | POST `/api/members/register` → 201 + 토큰 발급 | [ ] |
| 중복 이메일 회원가입 | 이미 등록된 이메일 → 400 | [ ] |
| 로그인 성공 | POST `/api/members/login` → 200 + 토큰 발급 | [ ] |
| 잘못된 비밀번호 로그인 | 비밀번호 불일치 → 400 | [ ] |
| 존재하지 않는 이메일 로그인 | 미등록 이메일 → 400 | [ ] |

### 2. 카테고리 (`category.feature`) — 5개 시나리오

| 시나리오 | 검증 내용 | 상태 |
|---------|----------|------|
| 카테고리 생성 | POST `/api/categories` → 201 + 카테고리 정보 | [ ] |
| 카테고리 목록 조회 | GET `/api/categories` → 200 + 등록된 개수 확인 | [ ] |
| 카테고리 수정 | PUT `/api/categories/{id}` → 200 + 변경된 이름 확인 | [ ] |
| 카테고리 삭제 | DELETE `/api/categories/{id}` → 204 | [ ] |
| 존재하지 않는 카테고리 수정 | 없는 ID로 수정 → 404 | [ ] |

### 3. 상품 (`product.feature`) — 8개 시나리오

| 시나리오 | 검증 내용 | 상태 |
|---------|----------|------|
| 상품 생성 | POST `/api/products` → 201 + 상품 정보 | [ ] |
| 상품 목록 페이징 조회 | GET `/api/products` → 200 + 등록된 개수 확인 | [ ] |
| 상품 단건 조회 | GET `/api/products/{id}` → 200 + 이름 확인 | [ ] |
| 상품 수정 | PUT `/api/products/{id}` → 200 + 변경된 이름/가격 확인 | [ ] |
| 상품 삭제 | DELETE `/api/products/{id}` → 204 | [ ] |
| 존재하지 않는 상품 조회 | 없는 ID로 조회 → 404 | [ ] |
| 15자 초과 이름으로 생성 | 이름 길이 검증 → 400 | [ ] |
| "카카오" 포함 이름으로 생성 | 금지어 검증 → 400 | [ ] |

### 4. 옵션 (`option.feature`) — 7개 시나리오

| 시나리오 | 검증 내용 | 상태 |
|---------|----------|------|
| 옵션 추가 | POST `/api/products/{id}/options` → 201 + 옵션 정보 | [ ] |
| 옵션 목록 조회 | GET `/api/products/{id}/options` → 200 + 등록된 개수 확인 | [ ] |
| 중복 옵션명 추가 | 같은 이름의 옵션 → 400 | [ ] |
| 옵션 삭제 (2개 이상) | DELETE → 204 | [ ] |
| 마지막 옵션 삭제 | 옵션이 1개뿐일 때 삭제 → 400 | [ ] |
| 50자 초과 이름으로 추가 | 이름 길이 검증 → 400 | [ ] |
| 존재하지 않는 상품에 추가 | 없는 상품 ID → 404 | [ ] |

### 5. 위시리스트 (`wish.feature`) — 7개 시나리오

| 시나리오 | 검증 내용 | 상태 |
|---------|----------|------|
| 위시 추가 | POST `/api/wishes` → 200 + 위시 정보 | [ ] |
| 중복 위시 추가 | 이미 있는 상품 → 기존 위시 반환 (멱등) | [ ] |
| 위시 목록 조회 | GET `/api/wishes` → 200 + 등록된 개수 확인 | [ ] |
| 위시 삭제 | DELETE `/api/wishes/{id}` → 204 | [ ] |
| 타인의 위시 삭제 | 다른 회원의 위시 삭제 → 403 | [ ] |
| 인증 없이 조회 | Authorization 헤더 없이 → 400 | [ ] |
| 존재하지 않는 상품 추가 | 없는 상품 ID → 404 | [ ] |

### 6. 주문 (`order.feature`) — 7개 시나리오 (작성 완료)

| 시나리오 | 검증 내용 | 상태 |
|---------|----------|------|
| 유효한 주문 | 201 + 재고 차감 + 포인트 차감 | [x] |
| 존재하지 않는 옵션으로 주문 | 404 | [x] |
| 재고 초과 주문 | 400 | [x] |
| 포인트 부족 주문 | 400 | [x] |
| 인증 없이 주문 | 400 | [x] |
| 재고와 동일한 수량 주문 | 201 + 재고 0 | [x] |
| 주문 후 목록 조회 | 200 + 주문 내역 포함 | [x] |

---

## 인수 테스트 작성 계획

### 작성 순서

| 순서 | Feature | 시나리오 수 | 이유 |
|------|---------|-----------|------|
| 1 | `member.feature` | 5 | 인증 토큰 발급의 기반, 다른 테스트의 전제 조건 |
| 2 | `category.feature` | 5 | 상품 생성의 필수 의존 대상 |
| 3 | `product.feature` | 8 | 옵션/위시/주문의 전제 조건, 이름 검증 규칙 포함 |
| 4 | `option.feature` | 7 | 주문의 전제 조건, 비즈니스 규칙 다수 |
| 5 | `wish.feature` | 7 | 인증 기반 도메인, 권한 검증 포함 |
| **합계** | | **32** | (기존 7 + 신규 32 = 총 39) |

### 공통 Step 분리

기존 `OrderSteps`에 정의된 Step 중 여러 Feature에서 재사용되는 것을 `CommonSteps.java`로 분리한다.

| 공통 Step | 사용하는 Feature |
|-----------|----------------|
| `이름이 {string}인 카테고리가 등록되어 있고` | category, product, option, wish, order |
| `{string} 상품이 가격 {int}, 이미지 {string}로 등록되어 있고` | product, option, wish, order |
| `재고 {int}개인 {string} 옵션이 등록되어 있고` | option, order |
| `포인트 {int}을 가진 회원 {string}이 등록되어 있고` | wish, order |
| `응답 상태 코드는 {int}이다` | 모든 Feature |

### 파일 구조 (완성 후)

```
src/test/java/gift/
├── CucumberTest.java
├── CucumberSpringConfiguration.java
└── steps/
    ├── Hooks.java              # DB 초기화 (기존)
    ├── SharedContext.java       # 상태 공유 (기존)
    ├── CommonSteps.java         # 공통 Step (신규 — OrderSteps에서 분리)
    ├── MemberSteps.java         # (신규)
    ├── CategorySteps.java       # (신규)
    ├── ProductSteps.java        # (신규)
    ├── OptionSteps.java         # (신규)
    ├── WishSteps.java           # (신규)
    └── OrderSteps.java          # (기존 — 공통 Step 분리 후 정리)

src/test/resources/features/
├── member.feature               # (신규, 5개 시나리오)
├── category.feature             # (신규, 5개 시나리오)
├── product.feature              # (신규, 8개 시나리오)
├── option.feature               # (신규, 7개 시나리오)
├── wish.feature                 # (신규, 7개 시나리오)
└── order.feature                # (기존, 7개 시나리오)
```

---

## AI 활용 기록

- Claude Code를 활용하여 프로젝트 전체 코드를 분석하고 스타일 불일치, 미사용 코드, 서비스 추출 대상을 식별함
- 분석 결과를 바탕으로 기능 목록과 구현 전략을 직접 정리하고, 작업 순서와 커밋 단위를 설계함
