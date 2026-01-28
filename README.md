# Schedule Manager Backend

## 프로젝트 소개
일정 관리 및 친구와의 일정 공유를 위한 캘린더 애플리케이션의 백엔드 API 서버입니다.
사용자는 개인 일정을 등록하고 관리하며, 친구를 추가하여 일정을 공유할 수 있습니다.

## 주요 기능
- **회원 관리**: JWT 기반 인증/인가, 이메일 인증을 통한 비밀번호 찾기
- **일정 관리**: 일정 등록, 조회, 수정, 삭제 및 기간별 일정 조회
- **친구 기능**: 친구 추가 및 검색 기능
- **보안**: Spring Security를 활용한 인증 처리 및 비밀번호 암호화

## 기술 스택
- **Backend**: Java 17, Spring Boot 3.5.4
- **Database**: MySQL 8.x
- **Infra**: AWS (EC2, RDS)
- **Security**: JWT, Spring Security
- **Build Tool**: Gradle

## ERD
-<img width="749" height="700" alt="scheduleApp erd" src="https://github.com/user-attachments/assets/1df532a5-0554-4990-9bb6-b49ceb069033" />

## API 명세
## API 명세

### 공통 응답 형식

모든 API는 다음과 같은 공통 응답 형식을 사용합니다.
```json
{
  "result": true,
  "message": "성공 메시지",
  "data": null
}
```

**응답 필드:**
- `result`: 성공 여부 (true/false)
- `message`: 응답 메시지
- `data`: 응답 데이터 (없으면 null)

---

### 1. 회원 관리 (User)

---

#### 1.1 회원가입

**Endpoint**
```
POST /api/signup
```

**Request Body**
```json
{
  "email": "user@example.com",
  "pw": "password123",
  "nickname": "사용자"
}
```

**Response (성공)**
```json
{
  "result": true,
  "message": "회원가입 성공",
  "data": null
}
```

**Response (실패 - 이메일 중복)**
```json
{
  "result": false,
  "message": "중복된 이메일",
  "data": null
}
```

**Response (실패 - 닉네임 중복)**
```json
{
  "result": false,
  "message": "중복된 닉네임",
  "data": null
}
```

**Status Code**
- `200`: 회원가입 성공
- `409`: 중복된 이메일 또는 닉네임

**구현 내용**
- 이메일은 trim() 처리 후 소문자 변환
- 닉네임은 trim() 처리만 수행
- 이메일/닉네임 중복 체크 선행
- 비밀번호는 BCrypt로 암호화하여 저장
- DB unique 제약조건으로 이중 검증
- GlobalExceptionHandler가 예외를 자동으로 ResponseDTO 형식으로 변환

---

#### 1.2 이메일 중복 확인

**Endpoint**
```
GET /api/checkEmail?value={email}
```

**Request**
```
GET /api/checkEmail?value=user@example.com
```

**Response (사용 가능)**
```json
{
  "result": true,
  "message": "사용 가능한 이메일입니다.",
  "data": null
}
```

**Response (사용 불가)**
```json
{
  "result": false,
  "message": "이미 사용중인 이메일입니다.",
  "data": null
}
```

**Status Code**
- `200`: 사용 가능한 이메일
- `400`: 이메일이 비어있음 또는 이미 사용 중

**구현 내용**
- 이메일 공백 및 빈 값 검증 (isBlank)
- trim() + 소문자 변환 후 DB 조회로 중복 여부 확인
- Controller에서 직접 검증 후 ResponseDTO 반환

---

#### 1.3 닉네임 중복 확인

**Endpoint**
```
GET /api/checkNickname?value={nickname}
```

**Request**
```
GET /api/checkNickname?value=사용자
```

**Response (사용 가능)**
```json
{
  "result": true,
  "message": "사용 가능한 닉네임입니다.",
  "data": null
}
```

**Response (사용 불가)**
```json
{
  "result": false,
  "message": "이미 사용중인 닉네임입니다.",
  "data": null
}
```

**Status Code**
- `200`: 사용 가능한 닉네임
- `400`: 닉네임이 비어있음 또는 이미 사용 중

**구현 내용**
- 닉네임 빈 값 검증 (isBlank)
- trim() 처리 후 DB 조회로 중복 여부 확인
- Controller에서 직접 검증 후 ResponseDTO 반환

---

#### 1.4 로그인

**Endpoint**
```
POST /api/login
```

**Request Body**
```json
{
  "email": "user@example.com",
  "pw": "password123"
}
```

**Response (성공)**
```json
{
  "result": true,
  "message": "로그인 성공",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (실패)**
```json
{
  "result": false,
  "message": "이메일 또는 비밀번호 불일치",
  "data": null
}
```

**Status Code**
- `200`: 로그인 성공 (JWT 토큰 반환)
- `400`: 이메일 또는 비밀번호 불일치

**구현 내용**
- 이메일로 사용자 조회 후 비밀번호 검증
- PasswordEncoder(BCrypt)로 비밀번호 매칭
- JWT 토큰 생성 (HS256 알고리즘, 만료시간 1시간)
- 생성된 토큰을 data 필드에 담아 반환
- 보안을 위해 이메일/비밀번호 오류 메시지 통일

---

#### 1.5 비밀번호 재설정 요청

**Endpoint**
```
POST /api/forgot
```

**Request Body**
```json
{
  "email": "user@example.com"
}
```

**Response**
```json
{
  "result": true,
  "message": "해당 이메일로 인증코드를 전송했습니다.",
  "data": null
}
```

**Status Code**
- `200`: 인증코드 전송 성공

**구현 내용**
- 6자리 랜덤 OTP 생성 (SecureRandom)
- OTP를 BCrypt로 해싱하여 DB 저장
- 기존 재설정 요청 삭제 후 새로 생성
- 만료시간 10분, 최대 시도 횟수 5회 설정
- JavaMailSender로 이메일 전송
- 존재하지 않는 이메일이어도 동일한 응답 반환 (보안)

---

#### 1.6 인증코드 검증

**Endpoint**
```
POST /api/verify
```

**Request Body**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**Response (성공)**
```json
{
  "result": true,
  "message": "인증에 성공했습니다.",
  "data": null
}
```

**Response (실패 - 코드 불일치)**
```json
{
  "result": false,
  "message": "인증코드가 일치하지 않습니다.",
  "data": null
}
```

**Response (실패 - 만료)**
```json
{
  "result": false,
  "message": "코드가 만료되었습니다.",
  "data": null
}
```

**Response (실패 - 시도 초과)**
```json
{
  "result": false,
  "message": "시도 횟수를 초과했습니다.",
  "data": null
}
```

**Status Code**
- `200`: 인증 성공
- `400`: 유효하지 않은 요청
- `409`: 코드 불일치, 만료, 시도 횟수 초과

**구현 내용**
- 만료시간 검증 (10분 초과 시 삭제 및 실패)
- 시도 횟수 검증 (5회 초과 시 삭제 및 실패)
- PasswordEncoder로 코드 해시 비교
- 실패 시 시도 횟수 1 증가
- validateCode() 메서드로 검증 로직 공통화

---

#### 1.7 비밀번호 재설정

**Endpoint**
```
POST /api/reset
```

**Request Body**
```json
{
  "email": "user@example.com",
  "code": "123456",
  "pw": "newPassword123"
}
```

**Response (성공)**
```json
{
  "result": true,
  "message": "비밀번호가 변경되었습니다.",
  "data": null
}
```

**Response (실패)**
```json
{
  "result": false,
  "message": "인증코드가 일치하지 않습니다.",
  "data": null
}
```

**Status Code**
- `200`: 비밀번호 변경 성공
- `400`: 사용자 없음
- `409`: 코드 불일치 또는 만료

**구현 내용**
- 코드 검증 (만료시간, 시도횟수, 일치여부)
- 새 비밀번호를 BCrypt로 암호화하여 업데이트
- 재설정 요청 정보 DB에서 삭제 (1회용)
- validateCode() 메서드 재사용으로 검증 로직 통일

---

**보안 처리**
- 비밀번호: BCrypt 단방향 해싱
- JWT: HS256 알고리즘, 1시간 만료
- OTP: 6자리, 10분 만료, 5회 시도 제한
- 모든 인증 정보는 암호화하여 저장
- 예외 처리 통일 (IllegalArgumentException, IllegalStateException)

---

### 2. 일정 관리 (Plan)

**공통 헤더**: `Authorization: Bearer {token}`

---

#### 2.1 일정 등록

**Endpoint**
```
POST /api/saveschedule
```

**Headers**
```
Authorization: Bearer {token}
```

**Request Body**
```json
{
  "title": "회의",
  "content": "팀 미팅",
  "start_date": "2025-01-20T14:00:00",
  "end_date": "2025-01-20T15:00:00",
  "start_time": "14:00:00",
  "end_time": "15:00:00",
  "color": 1
}
```

**Response**
```json
{
  "result": true,
  "message": "일정 등록 성공",
  "data": {
    "s_id": 1,
    "email": "user@example.com",
    "title": "회의",
    "content": "팀 미팅",
    "start_date": "2025-01-20T14:00:00",
    "end_date": "2025-01-20T15:00:00",
    "start_time": "14:00:00",
    "end_time": "15:00:00",
    "color": 1
  }
}
```

**Status Code**
- `200`: 일정 등록 성공
- `400`: 토큰 없음 또는 유효하지 않음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- start_date, end_date, start_time, end_time이 null이면 현재 시간으로 설정
- 일정 정보를 DB에 저장 후 PlanDTO로 변환하여 반환
- 등록된 일정을 즉시 프론트엔드에서 사용 가능

---

#### 2.2 특정 날짜 일정 제목 조회

**Endpoint**
```
GET /api/title?date={날짜}
```

**Headers**
```
Authorization: Bearer {token}
```

**Request**
```
GET /api/title?date=2025-01-20
```

**Response**
```json
{
  "result": true,
  "message": "조회 성공",
  "data": ["회의", "점심약속", "운동"]
}
```

**Status Code**
- `200`: 조회 성공
- `400`: 토큰 없음 또는 유효하지 않음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 해당 날짜의 00:00:00 ~ 23:59:59 범위로 일정 조회
- start_date <= dayEnd AND end_date >= dayStart 조건으로 겹치는 일정 검색
- 일정 제목만 리스트로 반환
- @DateTimeFormat으로 날짜 파라미터 자동 변환

---

#### 2.3 날짜 범위 일정 조회

**Endpoint**
```
GET /api/range?start={시작날짜}&end={종료날짜}
```

**Headers**
```
Authorization: Bearer {token}
```

**Request**
```
GET /api/range?start=2025-01-01&end=2025-01-31
```

**Response**
```json
{
  "result": true,
  "message": "조회 성공",
  "data": [
    {
      "s_id": 1,
      "email": "user@example.com",
      "title": "회의",
      "content": "팀 미팅",
      "start_date": "2025-01-20T14:00:00",
      "end_date": "2025-01-20T15:00:00",
      "start_time": "14:00:00",
      "end_time": "15:00:00",
      "color": 1
    }
  ]
}
```

**Status Code**
- `200`: 조회 성공
- `400`: 토큰 없음 또는 유효하지 않음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 시작일 00:00:00 ~ 종료일 23:59:59 범위로 일정 조회
- start_date <= rangeEnd AND end_date >= rangeStart 조건으로 겹치는 일정 검색
- 예: 1/10~1/12 일정은 1/11 조회 시에도 표시됨
- start_date 오름차순 정렬하여 반환
- Entity → DTO 변환하여 안전한 데이터 전송

---

#### 2.4 일정 상세 조회

**Endpoint**
```
GET /api/{s_id}
```

**Request**
```
GET /api/1
```

**Response (성공)**
```json
{
  "result": true,
  "message": "조회 성공",
  "data": {
    "s_id": 1,
    "email": "user@example.com",
    "title": "회의",
    "content": "팀 미팅",
    "start_date": "2025-01-20T14:00:00",
    "end_date": "2025-01-20T15:00:00",
    "start_time": "14:00:00",
    "end_time": "15:00:00",
    "color": 1
  }
}
```

**Response (실패)**
```json
{
  "result": false,
  "message": "일정을 찾을 수 없습니다.",
  "data": null
}
```

**Status Code**
- `200`: 조회 성공
- `404`: 일정을 찾을 수 없음

**구현 내용**
- s_id로 일정 조회
- 존재하지 않으면 404 반환
- 인증 없이 조회 가능 (공개 API)
- Entity → DTO 변환하여 반환

---

#### 2.5 일정 수정

**Endpoint**
```
PATCH /api/updateplan/{s_id}
```

**Headers**
```
Authorization: Bearer {token}
```

**Request Body**
```json
{
  "title": "회의 변경",
  "content": "팀 미팅 변경"
}
```

**Response (성공)**
```json
{
  "result": true,
  "message": "수정 성공",
  "data": {
    "s_id": 1,
    "email": "user@example.com",
    "title": "회의 변경",
    "content": "팀 미팅 변경",
    "start_date": "2025-01-20T14:00:00",
    "end_date": "2025-01-20T15:00:00",
    "start_time": "14:00:00",
    "end_time": "15:00:00",
    "color": 1
  }
}
```

**Response (실패 - 권한 없음)**
```json
{
  "result": false,
  "message": "본인의 일정만 수정할 수 있습니다",
  "data": null
}
```

**Response (실패 - 일정 없음)**
```json
{
  "result": false,
  "message": "존재하지 않는 일정입니다.",
  "data": null
}
```

**Status Code**
- `200`: 수정 성공
- `400`: 토큰 없음 또는 일정 없음
- `409`: 본인 일정이 아님

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- s_id로 일정 조회 후 본인 일정인지 확인
- 요청 본문에 포함된 필드만 부분 수정 (null이 아닌 값만)
- @Transactional로 변경 감지 자동 반영
- IllegalArgumentException: 일정 없음
- IllegalStateException: 권한 없음

---

#### 2.6 일정 삭제

**Endpoint**
```
DELETE /api/deleteplan/{s_id}
```

**Headers**
```
Authorization: Bearer {token}
```

**Request**
```
DELETE /api/deleteplan/1
```

**Response (성공)**
```json
{
  "result": true,
  "message": "삭제 성공",
  "data": null
}
```

**Response (실패 - 권한 없음)**
```json
{
  "result": false,
  "message": "본인의 일정만 삭제할 수 있습니다.",
  "data": null
}
```

**Status Code**
- `200`: 삭제 성공
- `400`: 토큰 없음 또는 일정 없음
- `409`: 본인 일정이 아님

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- s_id로 일정 조회 후 본인 일정인지 확인
- 본인 일정만 삭제 가능하도록 권한 검증
- @Transactional로 삭제 처리
- 예외 처리: IllegalArgumentException (일정 없음), IllegalStateException (권한 없음)

---

**인증 처리**
- 모든 일정 관리 API는 JWT 토큰 필수 (일정 상세 조회 제외)
- Authorization 헤더에서 Bearer 토큰 추출
- 토큰에서 이메일 추출하여 본인 확인
- 유효하지 않은 토큰 시 400 Bad Request 반환
- emailFromAuth() 메서드로 토큰 검증 로직 공통화

---

### 3. 친구 관리 (Friend)

**공통 헤더**: `Authorization: Bearer {token}`

---

#### 3.1 친구 검색

**Endpoint**
```
GET /api/searchfriend?q={검색어}
```

**Headers**
```
Authorization: Bearer {token}
```

**Request**
```
GET /api/searchfriend?q=user
```

**Response**
```json
{
  "result": true,
  "message": "검색 성공",
  "data": [
    {
      "email": "user2@example.com",
      "nickname": "사용자2"
    },
    {
      "email": "user3@example.com",
      "nickname": "사용자3"
    }
  ]
}
```

**Status Code**
- `200`: 검색 성공
- `400`: 토큰 없음 또는 유효하지 않음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 검색어가 포함된 이메일을 가진 사용자 최대 10명 조회
- 본인과 이미 친구인 사람은 제외
- Stream API로 필터링 처리
- 이메일과 닉네임만 UserSummary DTO로 반환

---

#### 3.2 친구 추가

**Endpoint**
```
POST /api/addfriend
```

**Headers**
```
Authorization: Bearer {token}
```

**Request Body**
```json
{
  "targetEmail": "friend@example.com"
}
```

**Response (성공)**
```json
{
  "result": true,
  "message": "친구추가 완료",
  "data": null
}
```

**Response (실패 - 본인 추가)**
```json
{
  "result": false,
  "message": "본인은 친구로 추가할 수 없습니다",
  "data": null
}
```

**Response (실패 - 존재하지 않는 사용자)**
```json
{
  "result": false,
  "message": "존재하지 않는 사용자입니다",
  "data": null
}
```

**Response (실패 - 이미 친구)**
```json
{
  "result": false,
  "message": "이미 친구입니다",
  "data": null
}
```

**Status Code**
- `200`: 친구 추가 성공
- `400`: 본인 추가 시도 또는 존재하지 않는 사용자
- `409`: 이미 친구 관계

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 본인을 친구로 추가하는 것 방지 (IllegalArgumentException)
- 대상 사용자 존재 여부 확인 (IllegalArgumentException)
- 이미 친구 관계인지 검증 (IllegalStateException)
- 두 이메일을 정렬하여 중복 방지 (A-B와 B-A를 같은 관계로 처리)
- Comparator.naturalOrder()로 이메일 정렬 후 저장

---

#### 3.3 친구 목록 조회

**Endpoint**
```
GET /api/listfriend
```

**Headers**
```
Authorization: Bearer {token}
```

**Response**
```json
{
  "result": true,
  "message": "조회 성공",
  "data": [
    {
      "email": "friend1@example.com",
      "nickname": "친구1"
    },
    {
      "email": "friend2@example.com",
      "nickname": "친구2"
    }
  ]
}
```

**Status Code**
- `200`: 조회 성공
- `400`: 토큰 없음 또는 유효하지 않음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 친구 테이블에서 나와 연결된 모든 이메일 조회 (Native Query)
- 사용자 정보(이메일, 닉네임) 조회하여 UserSummary DTO로 반환
- Stream API로 DTO 변환 처리

---

#### 3.4 친구 삭제

**Endpoint**
```
DELETE /api/deletefriend
```

**Headers**
```
Authorization: Bearer {token}
```

**Request Body**
```json
{
  "targetEmail": "friend@example.com"
}
```

**Response**
```json
{
  "result": true,
  "message": "친구 삭제 완료",
  "data": null
}
```

**Status Code**
- `200`: 삭제 성공
- `400`: 토큰 없음 또는 유효하지 않음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 본인을 삭제 대상으로 하면 무시 (조기 종료)
- 친구 관계 양방향 모두 삭제 (A-B, B-A 순서 무관)
- @Modifying, @Transactional로 삭제 쿼리 실행
- deletePair() 메서드로 양방향 삭제 처리

---

#### 3.5 친구 월별 일정 조회

**Endpoint**
```
GET /api/friend/calendar?friendEmail={친구이메일}&year={년도}&month={월}
```

**Headers**
```
Authorization: Bearer {token}
```

**Request**
```
GET /api/friend/calendar?friendEmail=friend@example.com&year=2025&month=1
```

**Response (성공)**
```json
{
  "result": true,
  "message": "조회 성공",
  "data": [
    {
      "s_id": 10,
      "email": "friend@example.com",
      "title": "친구 일정",
      "content": "친구의 일정",
      "start_date": "2025-01-15T10:00:00",
      "end_date": "2025-01-15T11:00:00",
      "start_time": "10:00:00",
      "end_time": "11:00:00",
      "color": 2
    }
  ]
}
```

**Response (실패 - 권한 없음)**
```json
{
  "result": false,
  "message": "친구만 열람할 수 있습니다",
  "data": null
}
```

**Status Code**
- `200`: 조회 성공
- `400`: 토큰 없음 또는 유효하지 않음
- `409`: 친구 관계가 아님

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 본인 일정이거나 친구 관계인 경우만 조회 가능
- isFriend() 메서드로 친구 관계 검증
- 해당 월의 1일 00:00:00 ~ 말일 23:59:59 범위로 일정 조회
- 친구가 아닌 경우 IllegalStateException 발생
- Entity → DTO 변환하여 반환

---

**친구 관계 관리**
- 친구 관계는 양방향으로 저장 (A-B 관계 저장 시 B-A도 동일하게 처리)
- 두 이메일을 정렬하여 저장함으로써 중복 방지
- 친구 삭제 시 양방향 모두 삭제
- 복합키 (FriendId) 사용으로 DB 레벨 중복 방지
- @Embeddable, @EmbeddedId로 복합키 구현

---

### 4. 에러 응답

모든 에러는 다음과 같은 형식으로 반환됩니다:
```json
{
  "result": false,
  "message": "에러 메시지",
  "data": null
}
```

**GlobalExceptionHandler 처리:**
- `IllegalArgumentException` → 400 Bad Request (잘못된 요청)
- `IllegalStateException` → 409 Conflict (상태 문제: 중복, 권한)
- `Exception` → 500 Internal Server Error (기타 오류)

**주요 에러 메시지:**

**인증/토큰 관련:**
- `"토큰이 없습니다"` - Authorization 헤더 누락
- `"유효하지 않은 토큰입니다"` - 잘못된 토큰

**회원 관리:**
- `"중복된 이메일"` - 이메일 중복
- `"중복된 닉네임"` - 닉네임 중복
- `"이메일 또는 비밀번호 불일치"` - 로그인 실패
- `"코드가 만료되었습니다."` - OTP 만료
- `"시도 횟수를 초과했습니다."` - OTP 시도 초과
- `"인증코드가 일치하지 않습니다."` - OTP 불일치

**일정 관리:**
- `"존재하지 않는 일정입니다"` / `"존재하지 않는 일정입니다."` - 일정 없음
- `"본인의 일정만 수정할 수 있습니다"` - 수정 권한 없음
- `"본인의 일정만 삭제할 수 있습니다."` - 삭제 권한 없음

**친구 관리:**
- `"본인은 친구로 추가할 수 없습니다"` - 본인 추가 시도
- `"존재하지 않는 사용자입니다"` - 사용자 없음
- `"이미 친구입니다"` - 친구 관계 중복
- `"친구만 열람할 수 있습니다"` - 친구 일정 조회 권한 없음

---

### 5. 테스트

**단위 테스트 (JUnit 5 + Mockito):**
- UserServiceTest: 회원가입 성공/실패 케이스 검증
- PlanServiceTest: 일정 등록, 수정 권한 검증
- FriendServiceTest: 친구 추가 성공/실패 케이스 검증

**실행 방법:**
```bash
./gradlew test
```

**테스트 커버리지:**
- 총 9개 테스트 (User 3개, Plan 3개, Friend 3개)
- Mock 객체를 활용한 격리된 단위 테스트
- 성공/실패 케이스 모두 검증
## AWS 아키텍처

### 인프라 구성

**EC2**
- 인스턴스 타입: t3.micro
- 리전: ap-northeast-2 (서울)
- 퍼블릭 IP: 15.164.147.189
- Spring Boot 애플리케이션 배포 (포트 8080)

**RDS**
- MySQL/Aurora
- 포트: 3306
- EC2에서만 접근 가능

**보안 그룹**
- HTTP (80), HTTPS (443): 웹 접속
- Custom TCP (8080): Spring Boot API
- SSH (22): 서버 관리
- MySQL (3306): DB 연결

### 배포 방식

1. Spring Boot 프로젝트 빌드
```bash
   ./gradlew build
```

2. EC2에 JAR 파일 업로드
```bash
   scp -i key.pem build/libs/app.jar ec2-user@15.164.147.189:~/
```

3. EC2에서 실행
```bash
   java -jar app.jar
```

### 네트워크 구성
```
클라이언트 → EC2 (Spring Boot) → RDS (MySQL)
```
## 트러블슈팅

### 친구 관계 중복 저장 및 삭제 문제

#### 상황
앱 프론트엔드와 백엔드 API를 연동하여 친구 기능을 테스트하던 중, 친구 삭제 기능에서 문제를 발견했습니다. 특정 친구는 삭제가 되는데 다른 친구는 삭제되지 않는 현상이 발생했습니다.

#### 문제
친구 관계는 양방향으로 저장하도록 설계했습니다. 예를 들어, A가 B를 친구 추가하면 `(A, B)`, B가 A를 친구 추가하면 `(B, A)`로 각각 저장됩니다. 

그러나 삭제 로직은 한 방향만 처리하도록 구현되어 있어, **내가 추가한 친구만 삭제 가능**하고 **친구가 나를 추가한 경우 내 쪽에서 삭제가 불가능**한 문제가 발생했습니다.

**문제 발견 과정:**
1. 친구 삭제 테스트 중 일부만 삭제되는 현상 발견
2. 캐시 문제 의심 → 재시도해도 동일
3. GPT 검색: "대소문자 구분 문제" → 이메일이 소문자라 해당 없음
4. DB 직접 확인하며 테스트 반복
5. 친구 추가 로직 회상 → 양방향 저장 확인
6. 삭제 로직이 단방향만 처리하는 것 파악

#### 해결

**1단계: 저장 시 정렬로 중복 방지**

친구 관계를 저장할 때 두 이메일을 알파벳 순으로 정렬하여 항상 일관된 형태로 저장하도록 수정했습니다.
```java
// 두 이메일을 정렬하여 일관성 유지
String lo = (Comparator.<String>naturalOrder()
    .compare(me.toLowerCase(), targetEmail.toLowerCase()) <= 0) 
    ? me : targetEmail;
String hi = lo.equals(me) ? targetEmail : me;

// 정렬된 형태로 저장
friendRepository.save(FriendEntity.of(lo, hi));
```

**장점:**
- A와 B가 서로 친구 추가해도 `(A, B)` 하나만 저장
- 중복 데이터 방지
- 조회/삭제 로직 단순화

**2단계: 삭제 쿼리 양방향 처리**

만약 이미 양방향으로 저장된 데이터가 있다면, 삭제 시 두 방향 모두 처리하도록 쿼리를 작성했습니다.
```java
@Query("""
    delete from FriendEntity f
    where (f.friendId.email = :a and f.friendId.fEmail = :b)
       or (f.friendId.email = :b and f.friendId.fEmail = :a)
""")
void deletePair(@Param("a") String a, @Param("b") String b);
```

#### 배운 점
1. **양방향 관계 설계 시 저장과 삭제 모두 고려해야 함**: 저장만 생각하고 삭제 로직을 간과했던 실수를 통해, 양방향 관계를 다룰 때는 CRUD 전체를 일관되게 설계해야 한다는 것을 배웠습니다.

2. **데이터 정규화의 중요성**: 정렬을 통해 `(A, B)`와 `(B, A)`를 동일한 관계로 통일함으로써 중복을 방지하고 로직을 단순화할 수 있었습니다.

3. **문제 해결 과정**: 구글 검색으로 해결되지 않을 때 GPT를 활용하고, 제안된 해결책이 맞지 않으면 직접 DB를 확인하며 문제를 추적하는 체계적인 접근 방법을 익혔습니다.

---
### 프론트엔드-백엔드 분업 협업 시 문서화의 중요성

#### 상황
이전 풀스택 프로젝트에서는 DB 설계 문서를 작성하며 개발했으나, 프론트엔드와 처음 분업 협업할 때 API 명세서 작성 경험은 없었습니다. 프로젝트 시작 시 문서화의 필요성을 제안했으나, 상대방이 불필요하다고 판단하여 문서 없이 진행하게 되었습니다.

#### 문제
실제 API 연동 과정에서 다음과 같은 문제가 발생했습니다:
- 필드명 불일치 (`nickname` vs `nick_name`)
- 데이터 타입 불일치 (String vs Int)
- 사전에 합의한 수정 사항이 반영되지 않음

#### 해결
DB 스키마 문서와 API 명세서를 작성하여 공유했고, 프론트엔드 작업 지연에 대비해 Postman을 활용한 독립 테스트 환경을 구축했습니다.

#### 배운 점
분업 협업에서는 문서가 필수적인 소통 수단임을 다시 한번 확인했습니다. 특히 풀스택에서는 경험하지 못했던 API 명세서의 중요성을 배웠으며, 역할과 책임(API 명세서 작성 주체, 일정 관리 등)을 프로젝트 시작 전 명확히 정의하는 것이 중요함을 깨달았습니다.

---
