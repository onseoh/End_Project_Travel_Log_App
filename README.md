============================================================
PROJECT: 여행 기록 및 위치 기반 관리 시스템
DEVELOPER: 이선호
DEPARTMENT: 순천향대학교 컴퓨터소프트웨어공학과
============================================================

[1. 프로젝트 아키텍처 및 설계]
- Pattern: MVVM (Model-View-ViewModel) 구조를 준수하여 로직과 UI 분리
- Asynchronous Processing: Coroutines (Dispatchers.IO)를 이용한 DB 접근 최적화
- Resource Management: Light/Night Mode 대응을 위한 리소스 분리 (values vs values-night)

[2. 구현 단계 및 개발 상세]
Step 1: 데이터 모델 및 로컬 DB 구축 (SQLite 기반)
- TravelRecord 모델 정의 및 DBHelper를 통한 데이터 영속성 확보
  Step 2: 목록 화면 및 인터랙션 구현 (RecyclerView)
- Coroutine을 활용하여 메인 스레드 블로킹 없는 리스트 로딩 구현
- 롱 클릭 컨텍스트 메뉴를 통한 수정/삭제 로직 처리
  Step 3: 지도 API 연동 및 고도화
- Google Maps SDK 연동 및 API Key 보안 관리
- 사진 EXIF 데이터 파싱 및 위도/경도 기반 마커 생성 로직 구현
- Geocoder 서비스를 활용한 실시간 장소 검색 기능 연동
  Step 4: 사용자 환경 최적화 (UX/UI)
- 시스템 권한 런처(ActivityResultContracts) 구현
- Day/Night 테마별 Color Palette(background/text_color) 자동 전환 로직 적용

[3. 핵심 기술 모듈 분석]
├── DB Layer: SQLite (CRUD)
│    └── getAllRecords(), deleteRecord(id) 등
├── Map Layer: Google Maps SDK
│    └── extractGPSFromUri() -> 마커 표시 -> 애니메이션 이동
├── UX Layer: Theme Control
│    └── AppCompatDelegate를 활용한 런타임 테마 전환
└── Security: local.properties
└── API Key 숨김을 통한 버전 관리 보안 준수

[4. 실행 및 빌드 환경]
- Compile SDK: API 34+
- Build System: Gradle
- Dependency: Material Design, Coroutines, Google Maps, ExifInterface

[5. 개발자 한마디]
- 본 프로젝트는 실제 사용자가 사진 데이터를 바탕으로 여행 경로를 시각화할 수 있도록
  위치 기반 서비스의 기초를 다지는 데 주력하였습니다.
  다크 모드와 같은 UX 디테일을 추가하여 가독성과 사용자 환경을 개선했습니다.
  ============================================================