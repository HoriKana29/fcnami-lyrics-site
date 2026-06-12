# Backend Test Report

อัปเดตล่าสุด: 2026-05-25

## 1. Pass/Fail Status

PASS

## 2. Test Cases Run

- `.\mvnw.cmd -Dtest=QueueCsvParserTest test`
- `.\mvnw.cmd "-Dtest=RequestServiceTest,RequestInternalServiceTest" test`
- `.\mvnw.cmd -Dtest=RequestServiceConcurrencyTest test`
- `.\mvnw.cmd test`

Final full run:

- Tests run: 114
- Failures: 0
- Errors: 0
- Skipped: 0

## 3. Issues Found

- Initial compile failure: `User#setMaxRequests` did not exist
- Concurrency delete failed with stale entity delete when two threads deleted the same request
- Stress test hung because fixed thread pool size was smaller than the latch count
- Earlier timed-out Maven runs left Java/Surefire processes running in the background
- Controller test found `/api/songs` raw `PageImpl` serialization failed with `HttpMessageNotWritableException`; fixed with `PageResponse`

## 4. Missing Coverage

- No direct service test for live `GoogleSheetQueueService.refresh()` with mocked HTTP client
- No test yet for YouTube ID extraction variants in `SongCatalogService`
- No auth/permission tests yet because admin auth is not implemented

## 5. Recommended Fixes

- Add focused `SongCatalogService` tests for YouTube URL parsing and null tag safety
- Consider replacing noisy expected constraint violation logs in tests with cleaner assertions/setup
- Add admin auth/permission tests when auth lands

## 6. Handoff

Next agent: Frontend Agent

Next file to read:

- `frontend-task.md`
