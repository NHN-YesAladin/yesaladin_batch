# yesaladin_batch
YesAladin Batch는 Spring Batch와 Spring Scheduler를 사용하여 Yesaladin 내 여러 배치 작업을 처리하는 시스템입니다. 회원 등급 갱신, 갱신된 등급에 따른 포인트 지급, 생일 쿠폰 지급, 주문 상태 변경, 구독 갱신 알림을 수행합니다.

## Getting Started
```bash
./mvnw spring-boot:run
```

## Features
### [@서민지](https://github.com/narangd0)

- **회원 등급 갱신**
  - 매월 1일 전달 주문 금액 산정 후 회원 등급 갱신
    - Shop Database에 접근하여 읽기/처리/쓰기 작업 수행
    - CompositeItemWriter를 사용한 회원 등급 갱신/등급 갱신 내역에 대한 복합적인 쓰기 작업 수행
- **포인트 지급**
  - 매월 1일 갱신된 등급에 따른 포인트 지급 및 포인트 내역 작성
- **생일 쿠폰 지급**
  - 매일 7일 후 생일인 회원에게 쿠폰 자동 발행
    - Shop, Coupon 서버와의 API 통신을 통한 생일 회원 조회/쿠폰 지급 처리
- **Project Management**
  - NHN Cloud Log & Crash 연동을 통해 모니터링 환경 구축
  - Spring Cloud Config 연동을 통해 설정 정보 외부화

### [@이수정](https://github.com/sujeong68)

- 매일 자정 주문(ORDER) 상태로 3일이 지난 주문을 조회하여 취소(CANCEL) 상태로 이력 추가
  - DB에 접근하여 테이블 order_status_change_logs(주문 상태 변경 이력)에서 주문(ORDER) 상태로 3일이 지나고, 3일 사이 주문(ORDER) 상태 외 이력이 존재하지 않는 주문 조회
  - 조회된 주문을 대상으로 취소(CANCEL) 상태의 이력을 테이블 order_status_change_logs(주문 상태 변경 이력)에 추가
- 매일 10시 구독 갱신까지 1달, 1주, 1일 남은 구독주문을 조회하여 두레이 훅을 사용해 구독자에게 알림
  - DB에 접근하여 지정한 기간만큼 남은 구독주문 상품, 그와 관련된 구독자, 결제일, 구독 기간을 조회
  - ItemWriter를 사용하여 구독 정보를 DoorayHookSender를 통해 구독자에게 알림

## Project Architecture

<img width="1055" alt="스크린샷 2023-02-22 오전 10 15 46" src="https://user-images.githubusercontent.com/60968342/220496124-61dc2fb4-a423-4ce6-ad5a-d8afec4b2600.png">

## Technical Issue

### (있다면 작성해주시고 없으면 Technical Issue는 지우셔도 됩니다.)

## Tech Stack

### Languages

![Java](https://img.shields.io/badge/Java-007396?style=flat-square&logo=Java)

### Frameworks

![SpringBoot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=SpringBoot&logoColor=white)
![SpringCloud](https://img.shields.io/badge/Spring%20Cloud-6DB33F?style=flat&logo=Spring&logoColor=white)
![SpringBatch](https://img.shields.io/badge/Spring%20Batch-6DB33F?style=flat&logo=Spring&logoColor=white)
![SpringBatch](https://img.shields.io/badge/Spring%20Scheduler-6DB33F?style=flat&logo=Spring&logoColor=white)

### Build Tool

![ApacheMaven](https://img.shields.io/badge/Maven-C71A36?style=flat&logo=ApacheMaven&logoColor=white)

### Database

![MySQL](http://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=MySQL&logoColor=white)

### DevOps

![NHN Cloud](https://img.shields.io/badge/-NHN%20Cloud-blue?style=flat&logo=iCloud&logoColor=white)
![Jenkins](http://img.shields.io/badge/Jenkins-D24939?style=flat-square&logo=Jenkins&logoColor=white)
![SonarQube](https://img.shields.io/badge/SonarQube-4E98CD?style=flat&logo=SonarQube&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=flat&logo=Grafana&logoColor=white)

### 형상 관리 전략

![Git](https://img.shields.io/badge/Git-F05032?style=flat&logo=Git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=GitHub&logoColor=white)

- Git Flow 전략을 사용하여 Branch를 관리하며 Main/Develop Branch로 Pull Request 시 코드 리뷰 진행 후 merge 합니다.
  ![image](https://user-images.githubusercontent.com/60968342/219870689-9b9d709c-aa55-47db-a356-d1186b434b4a.png)
- Main: 배포시 사용
- Develop: 개발 단계가 끝난 부분에 대해 Merge 내용 포함
- Feature: 기능 개발 단계
- Hot-Fix: Merge 후 발생한 버그 및 수정 사항 반영 시 사용

## Contributors

<a href="https://github.com/NHN-YesAladin/yesaladin_batch/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=NHN-YesAladin/yesaladin_front" />
</a>
