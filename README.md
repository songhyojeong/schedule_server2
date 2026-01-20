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
### 회원 관리

---

#### 회원가입

**Endpoint**
```
POST /api/signup
```

**Request Body**
```json
{
  "email": "test@example.com",
  "pw": "password123",
  "nickname": "홍길동"
}
```

**Response**
```json
{
  "result": true,
  "message": "회원가입 성공",
  "data": null
}
```

**Status Code**
- `200` : 회원가입 성공
- `400` : 중복된 이메일 또는 닉네임

**구현 내용**
- 이메일과 trim() 처리 후 소문자 변환
- 닉네임은 trim() 처리만 수행
- 이메일/닉네임 중복 체크 선행
- 비밀번호는 BCrypt로 암호화하여 저장
- DB unique 제약조건으로 이중 검증

---

#### 이메일 중복 체크

**Endpoint**
```
GET /api/checkEmail?value={email}
```

**Request**
```
GET /api/checkEmail?value=test@example.com
```

**Response**
```json
{
  "result": true,
  "message": "사용 가능한 이메일입니다.",
  "data": null
}
```

**Status Code**
- `200` : 사용 가능한 이메일
- `400` : 이미 사용 중인 이메일

**구현 내용**
- 이메일 공백 및 빈 값 검증 (isBlank)
- trim()+ 소문자 변환 후 DB 조회로 중복 여부 확인

---

#### 닉네임 중복 체크

**Endpoint**
```
GET /api/checkNickname?value={nickname}
```

**Request**
```
GET /api/checkNickname?value=홍길동
```

**Response**
```json
{
  "result": true,
  "message": "사용 가능한 닉네임입니다.",
  "data": null
}
```

**Status Code**
- `200` : 사용 가능한 닉네임
- `400` : 이미 사용 중인 닉네임

**구현 내용**
- 닉네임 빈 값 검증(isBlank)
- trim 처리 후 DB 조회로 중복 여부 확인

---

#### 로그인

**Endpoint**
```
POST /api/login
```

**Request Body**
```json
{
  "email": "test@example.com",
  "pw": "password123"
}
```

**Response**
```json
{
  "result": true,
  "message": "로그인 성공",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Status Code**
- `200` : 로그인 성공 (JWT 토큰 반환)
- `400` : 이메일 또는 비밀번호 불일치

**구현 내용**
- 이메일로 사용자 조회 후 비밀번호 검증
- PasswordEncoder(BCrypt)로 비밀번호 매칭
- JWT 토큰 생성 (HS256 알고리즘, 만료시간 1시간)
- 생성된 토큰을 data 필드에 담아 반환

---

#### 비밀번호 재설정 요청

**Endpoint**
```
POST /api/forgot
```

**Request Body**
```json
{
  "email": "test@example.com"
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
- `200` : 인증코드 전송 성공

**구현 내용**
- 6자리 랜덤 OTP 생성 (SecureRandom)
- OTP를 BCrypt로 해싱하여 DB 저장
- 기존 재설정 요청 삭제 후 새로 생성
- 만료시간 10분, 최대 시도 횟수 5회 설정
- JavaMailSender로 이메일 전송

---

#### 인증코드 검증

**Endpoint**
```
POST /api/verify
```

**Request Body**
```json
{
  "email": "test@example.com",
  "code": "123456"
}
```

**Response**
```json
{
  "result": true,
  "message": "인증에 성공했습니다.",
  "data": null
}
```

**Status Code**
- `200` : 인증 성공
- `400` : 코드 불일치 또는 만료
- `429` : 시도 횟수 초과 (5회)

**구현 내용**
- 만료시간 검증 (10분 초과 시 삭제 및 실패)
- 시도 횟수 검증 (5회 초과 시 삭제 및 실패)
- PasswordEncoder로 코드 해시 비교
- 실패 시 시도 횟수 1 증가

---

#### 비밀번호 재설정

**Endpoint**
```
POST /api/reset
```

**Request Body**
```json
{
  "email": "test@example.com",
  "code": "123456",
  "pw": "newPassword123"
}
```

**Response**
```json
{
  "result": true,
  "message": "비밀번호가 변경되었습니다.",
  "data": null
}
```

**Status Code**
- `200` : 비밀번호 변경 성공
- `400` : 코드 불일치 또는 사용자 없음

**구현 내용**
- 코드 검증 (만료시간, 시도횟수, 일치여부)
- 새 비밀번호를 BCrypt로 암호화하여 업데이트
- 재설정 요청 정보 DB에서 삭제 (1회용)

---

**보안 처리**
- 비밀번호: BCrypt 단방향 해싱
- JWT: HS256 알고리즘, 1시간 만료
- OTP: 6자리, 10분 만료, 5회 시도 제한
- 모든 인증 정보는 암호화하여 저장

---

### 일정 관리

---

#### 일정 등록

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
  "start_date": "2026-01-10T09:00:00",
  "end_date": "2026-01-10T10:00:00",
  "start_time": "09:00:00",
  "end_time": "10:00:00",
  "color": 1
}
```

**Response**
```json
{
  "s_id": 1,
  "email": "test@example.com",
  "title": "회의",
  "content": "팀 미팅",
  "start_date": "2026-01-10T09:00:00",
  "end_date": "2026-01-10T10:00:00",
  "start_time": "09:00:00",
  "end_time": "10:00:00",
  "color": 1
}
```

**Status Code**
- `200` : 일정 등록 성공
- `401` : 토큰 유효하지 않음
- `500` : 서버 오류

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- start_date, end_date, start_time, end_time이 null이면 현재 시간으로 설정
- 일정 정보를 DB에 저장 후 저장된 엔티티 반환

---

#### 특정 날짜 일정 제목 조회

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
GET /api/title?date=2026-01-10
```

**Response**
```json
[
  "회의",
  "점심 약속",
  "운동"
]
```

**Status Code**
- `200` : 조회 성공
- `401` : 토큰 유효하지 않음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 해당 날짜의 00:00:00 ~ 23:59:59 범위로 일정 조회
- start_date <= dayEnd AND end_date >= dayStart 조건으로 겹치는 일정 검색
- 일정 제목만 리스트로 반환

---

#### 날짜 범위 일정 조회

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
GET /api/range?start=2026-01-11&end=2026-01-11
```

**Response**
```json
[
  {
    "s_id": 1,
    "email": "test@example.com",
    "title": "회의",
    "content": "팀 미팅",
    "start_date": "2026-01-10T00:00:00",
    "end_date": "2026-01-12T123:59:59",
    "start_time": "00:00:00",
    "end_time": "23:59:59",
    "color": 1
  }
]
```

**Status Code**
- `200` : 조회 성공
- `401` : 토큰 유효하지 않음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 시작일 00:00:00 ~ 종료일 23:59:59 범위로 일정 조회
- start_date <= rangeEnd AND end_date >= rangeStart 조건으로 겹치는 일정 검색
- (예:1/10~1/12일정은 1/11 조회 시에도 표시)
- start_date 오름차순 정렬하여 반환

---

#### 일정 상세 조회

**Endpoint**
```
GET /api/{s_id}
```

**Request**
```
GET /api/1
```

**Response**
```json
{
  "s_id": 1,
  "email": "test@example.com",
  "title": "회의",
  "content": "팀 미팅",
  "start_date": "2026-01-10T09:00:00",
  "end_date": "2026-01-10T10:00:00",
  "start_time": "09:00:00",
  "end_time": "10:00:00",
  "color": 1
}
```

**Status Code**
- `200` : 조회 성공
- `404` : 일정을 찾을 수 없음

**구현 내용**
- s_id로 일정 조회
- 존재하지 않으면 404 반환
- 인증 없이 조회 가능

---

#### 일정 수정

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
  "content": "팀 미팅 시간 변경",
  "start_date": "2026-01-10T10:00:00",
  "end_date": "2026-01-10T11:00:00"
}
```

**Response**
```json
{
  "s_id": 1,
  "email": "test@example.com",
  "title": "회의 변경",
  "content": "팀 미팅 시간 변경",
  "start_date": "2026-01-10T10:00:00",
  "end_date": "2026-01-10T11:00:00",
  "start_time": "09:00:00",
  "end_time": "10:00:00",
  "color": 1
}
```

**Status Code**
- `200` : 수정 성공
- `401` : 토큰 유효하지 않음
- `403` : 본인 일정이 아님
- `404` : 일정을 찾을 수 없음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- s_id로 일정 조회 후 본인 일정인지 확인
- 요청 본문에 포함된 필드만 부분 수정 (null이 아닌 값만)
- @Transactional로 변경 감지 자동 반영

---

#### 일정 삭제

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

**Response**
```json
"삭제 완료"
```

**Status Code**
- `200` : 삭제 성공
- `401` : 토큰 유효하지 않음
- `403` : 본인 일정이 아님
- `404` : 일정을 찾을 수 없음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- s_id로 일정 조회 후 본인 일정인지 확인
- 본인 일정만 삭제 가능하도록 권한 검증
- @Transactional로 삭제 처리

---

**인증 처리**
- 모든 일정 관리 API는 JWT 토큰 필수 (일정 상세 조회 제외)
- Authorization 헤더에서 Bearer 토큰 추출
- 토큰에서 이메일 추출하여 본인 확인
- 유효하지 않은 토큰 시 401 Unauthorized 반환

---

### 친구 관리

---

#### 친구 검색

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
GET /api/searchfriend?q=test
```

**Response**
```json
[
  {
    "email": "test@example.com",
    "nickname": "홍길동"
  },
  {
    "email": "test2@example.com",
    "nickname": "김철수"
  }
]
```

**Status Code**
- `200` : 검색 성공
- `401` : 토큰 유효하지 않음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 검색어가 포함된 이메일을 가진 사용자 최대 10명 조회
- 본인과 이미 친구인 사람은 제외
- 이메일과 닉네임만 반환

---

#### 친구 추가

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

**Response**
```json
"친구추가 완료"
```

**Status Code**
- `200` : 친구 추가 성공
- `400` : 본인 추가 시도 또는 존재하지 않는 사용자
- `401` : 토큰 유효하지 않음
- `409` : 이미 친구 관계

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 본인을 친구로 추가하는 것 방지
- 대상 사용자 존재 여부 확인
- 이미 친구 관계인지 검증
- 두 이메일을 정렬하여 중복 방지 (A-B와 B-A를 같은 관계로 처리)

---

#### 친구 목록 조회

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
[
  {
    "email": "friend1@example.com",
    "nickname": "친구1"
  },
  {
    "email": "friend2@example.com",
    "nickname": "친구2"
  }
]
```

**Status Code**
- `200` : 조회 성공
- `401` : 토큰 유효하지 않음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 친구 테이블에서 나와 연결된 모든 이메일 조회
- 사용자 정보(이메일, 닉네임) 조회하여 반환

---

#### 친구 삭제

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
"친구 삭제 완료"
```

**Status Code**
- `200` : 삭제 성공
- `401` : 토큰 유효하지 않음

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 본인을 삭제 대상으로 하면 무시
- 친구 관계 양방향 모두 삭제 (A-B, B-A 순서 무관)

---

#### 친구 월별 일정 조회

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
GET /api/friend/calendar?friendEmail=friend@example.com&year=2026&month=1
```

**Response**
```json
[
  {
    "s_id": 1,
    "email": "friend@example.com",
    "title": "회의",
    "content": "팀 미팅",
    "start_date": "2026-01-10T09:00:00",
    "end_date": "2026-01-10T10:00:00",
    "start_time": "09:00:00",
    "end_time": "10:00:00",
    "color": 1
  }
]
```

**Status Code**
- `200` : 조회 성공
- `401` : 토큰 유효하지 않음
- `403` : 친구 관계가 아님
- `500` : 서버 오류

**구현 내용**
- JWT 토큰에서 사용자 이메일 추출
- 본인 일정이거나 친구 관계인 경우만 조회 가능
- 해당 월의 1일 00:00:00 ~ 말일 23:59:59 범위로 일정 조회
- 친구가 아닌 경우 403 Forbidden 반환

---

**친구 관계 관리**
- 친구 관계는 양방향으로 저장 (A-B 관계 저장 시 B-A도 동일하게 처리)
- 두 이메일을 정렬하여 저장함으로써 중복 방지
- 친구 삭제 시 양방향 모두 삭제

---
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
