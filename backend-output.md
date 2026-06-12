# Backend Output

อัปเดตล่าสุด: 2026-05-25

## 1. What I Did

- แยก CSV parsing, queue row normalization, และ tier normalization ออกจาก `GoogleSheetQueueService`
- เพิ่ม validation ให้ `QueueProperties`
- ปรับ `RequestInternalService` ให้แบ่ง validation, duplicate check, queue counter lock, และ order generation เป็น method ย่อย
- เพิ่ม `maxRequests` ให้ `User` และตรวจ user request limit แบบ optional
- ปรับ `RequestService.deleteRequest` ให้ concurrent delete ไม่พังจาก stale entity delete
- ปรับ `SongCatalogService` ให้ handle tags null ได้ และ extract YouTube ID ได้หลาย format ขึ้น
- ปรับ controller response/error behavior:
  - admin create ตอบ `201 Created`
  - public songs default sort เป็น `createdAt DESC`
  - error response มี fallback และ `errorId`
- cleanup comments ใน repository/model/factory ที่เป็น task note เก่า
- ปรับ factory ให้ validate required fields และใช้ slug/normalized key logic กลาง
- ยุบ `QueueCounterRepository.lockQueue` ให้เหลือ `findForUpdate`
- เปลี่ยน public song list response จาก raw `PageImpl` เป็น `PageResponse` DTO เพื่อ serialize ได้เสถียร
- เพิ่ม controller tests สำหรับ admin songs, public songs, และ queue

## 2. What I Found

- GitHub PR #2 ไม่มี review threads/comments แยกต่างหาก
- comment ที่ต้องแก้เป็น inline comments ใน code
- full test suite เดิมติด compile เพราะ test เรียก `User#setMaxRequests` แต่ model ยังไม่มี field นี้
- concurrency stress test ค้างเพราะ `CountDownLatch` รอจำนวน task มากกว่า thread pool ที่เริ่มได้จริง
- Maven timeout ก่อนหน้าเหลือ Java/Surefire processes ค้าง ต้อง stop ก่อนรันซ้ำ
- `PageImpl` response ของ `/api/songs` serialize แล้วแตกเป็น 400 ใน Spring Boot 4 จึงเปลี่ยนเป็น DTO ชัดเจน

## 3. What I Recommend

- Backend พร้อมพอสำหรับเริ่ม Frontend v1 แล้ว
- รอบ backend ถัดไปควรเน้น auth/admin policy และ Google Sheet refresh integration test
- ถ้าจะเพิ่ม request limit จริงจัง ควรเพิ่ม user row lock หรือ service-level policy ที่ชัดกว่านี้

## 4. Files Updated

- `backend/src/main/java/com/fcnami/backend/Api/AdminSongController.java`
- `backend/src/main/java/com/fcnami/backend/Api/ApiExceptionHandler.java`
- `backend/src/main/java/com/fcnami/backend/Api/PublicSongController.java`
- `backend/src/main/java/com/fcnami/backend/Api/QueueController.java`
- `backend/src/main/java/com/fcnami/backend/Api/SongDtos.java`
- `backend/src/main/java/com/fcnami/backend/Config/QueueProperties.java`
- `backend/src/main/java/com/fcnami/backend/Factory/QueueSnapshotFactory.java`
- `backend/src/main/java/com/fcnami/backend/Factory/RequestFactory.java`
- `backend/src/main/java/com/fcnami/backend/Factory/SongFactory.java`
- `backend/src/main/java/com/fcnami/backend/Factory/TagFactory.java`
- `backend/src/main/java/com/fcnami/backend/Factory/UserFactory.java`
- `backend/src/main/java/com/fcnami/backend/Model/AdminUser.java`
- `backend/src/main/java/com/fcnami/backend/Model/QueueRequest/QueueCounter.java`
- `backend/src/main/java/com/fcnami/backend/Model/QueueRequest/QueueSnapshot.java`
- `backend/src/main/java/com/fcnami/backend/Model/QueueRequest/QueueType.java`
- `backend/src/main/java/com/fcnami/backend/Model/QueueRequest/Request.java`
- `backend/src/main/java/com/fcnami/backend/Model/QueueRequest/RequestStatus.java`
- `backend/src/main/java/com/fcnami/backend/Model/SongTags/Lyrics.java`
- `backend/src/main/java/com/fcnami/backend/Model/SongTags/Song.java`
- `backend/src/main/java/com/fcnami/backend/Model/SongTags/SongStatus.java`
- `backend/src/main/java/com/fcnami/backend/Model/SongTags/Tag.java`
- `backend/src/main/java/com/fcnami/backend/Model/SongTags/TagType.java`
- `backend/src/main/java/com/fcnami/backend/Model/User.java`
- `backend/src/main/java/com/fcnami/backend/Repository/AdminUserRepository.java`
- `backend/src/main/java/com/fcnami/backend/Repository/LyricsRepository.java`
- `backend/src/main/java/com/fcnami/backend/Repository/QueueCounterRepository.java`
- `backend/src/main/java/com/fcnami/backend/Repository/QueueSnapshotRepository.java`
- `backend/src/main/java/com/fcnami/backend/Repository/RequestRepository.java`
- `backend/src/main/java/com/fcnami/backend/Repository/SongRepository.java`
- `backend/src/main/java/com/fcnami/backend/Repository/TagRepository.java`
- `backend/src/main/java/com/fcnami/backend/Repository/UserRepository.java`
- `backend/src/main/java/com/fcnami/backend/Service/GoogleSheetQueueService.java`
- `backend/src/main/java/com/fcnami/backend/Service/QueueCsvParser.java`
- `backend/src/main/java/com/fcnami/backend/Service/QueueRowNormalizer.java`
- `backend/src/main/java/com/fcnami/backend/Service/QueueTierNormalizer.java`
- `backend/src/main/java/com/fcnami/backend/Service/RequestInternalService.java`
- `backend/src/main/java/com/fcnami/backend/Service/RequestService.java`
- `backend/src/main/java/com/fcnami/backend/Service/SongCatalogService.java`
- `backend/src/main/java/com/fcnami/backend/Support/SlugUtil.java`
- `backend/src/test/java/com/fcnami/backend/Api/AdminSongControllerTest.java`
- `backend/src/test/java/com/fcnami/backend/Api/PublicSongControllerTest.java`
- `backend/src/test/java/com/fcnami/backend/Api/QueueControllerTest.java`
- `backend/src/test/java/com/fcnami/backend/Service/QueueCsvParserTest.java`
- `backend/src/test/java/com/fcnami/backend/Service/RequestServiceConcurrencyTest.java`
- `backend/src/test/java/com/fcnami/backend/Service/RequestServiceIntegrationTest.java`
- `backend/src/test/java/com/fcnami/backend/Service/RequestServiceTest.java`

## 5. Handoff

Next agent: Frontend Agent

Next file to read:

- `frontend-task.md`
