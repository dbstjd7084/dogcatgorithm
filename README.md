# 멍냥고리즘

## 프로젝트 소개

이 프로젝트는 [반려동물의 인적사항, 일기, 추천 영상, 반려동물 동반 가능 시설 찾기]을(를) 제공합니다.&#x20;

주요 기능:

- 반려동물의 인적사항

  자신의 반려동물 인적사항을 작성하고, 사진을 등록해 홈 화면을 내 반려동물로 꾸밀 수 있습니다.

- *일기*

  반려동물과 지내면서 생긴 일과 평소 일상을 기록해보세요! 사진도 일기에 넣을 수 있습니다.

- 추천 영상

  작성된 반려동물 정보를 AI가 고려해 유튜브 영상을 추천해줍니다. 예를 들어 생일의 경우 축하 메세지와 함께 반려동물이 좋아하는 것을 고려한 영상 키워드를 토대로 상위 10개의 영상을 추천하고, 그 영상을 프로젝트 내에서 시청합니다.

- 반려동물 동반 가능 시설 찾기

  등록된 반려동물 동반 시설 정보를 토대로 근처 반경의 시설을 표시해주거나 특정 위치의 시설을 표시해줍니다. 클릭 시 해당 시설에 대한 세부 정보를 표시합니다.

## 설치 방법

1. 이 저장소를 클론합니다:
   ```bash
     git clone https://github.com/dbstjd7084/dogcatgorithm.git
   ```
2. 디렉토리로 이동합니다:
   ```bash
   cd dogcatgorithm
   ```

## 사용 방법

1. local.properties 파일에 필요 API키를 작성해야 합니다.

   ```properties
   GOOGLE_MAPS_API_KEY=""
   YOUTUBE_API_KEY=""
   AI_API_KEY=""
   PET_FRIENDLY_FACILITIES_DATA_API_KEY=""
   ```

   작성할 목록은 다음과 같습니다.

2. 위에서부터 [Google Maps API, Youtube Data API v3, ChatGPT, 공공데이터 인증키]

3. 반려동물 동반 가능 문화시설 위치 데이터에서 활용 신청 후 일반 인증키(Decoding)

   - [공공데이터 포털 반려동물 동반 가능 문화시설 데이터](https://www.data.go.kr/tcs/dss/selectFileDataDetailView.do?publicDataPk=15111389, "공공데이터 포털 사이트로 이동")

## 버전 및 업데이트 정보

- **현재 버전**: v1.0.0
- **업데이트 내역**:
  - v1.0.0: 초기 릴리스
