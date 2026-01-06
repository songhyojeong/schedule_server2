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

##ERD
- <img width="749" height="700" alt="scheduleApp erd" src="https://github.com/user-attachments/assets/1df532a5-0554-4990-9bb6-b49ceb069033" />
