# fcnami-lyrics-site
A personal Japanese song lyrics translation platform (FCNami T_T) featuring Kanji, Romaji, and Thai translations with structured song management, request system, and developer-driven tools.

## Backend

The backend lives in `backend/` and starts as a Spring Boot API for the v1 lyrics catalog, admin song editing, and Google Sheet backed request queue.

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Useful endpoints:

- `GET /api/songs`
- `GET /api/songs/{slug}`
- `POST /api/admin/songs`
- `PUT /api/admin/songs/{id}`
- `GET /api/queue?search=&status=&tier=`
- `POST /api/queue/sync`

Runtime config is environment-driven. Local development defaults to in-memory H2; production can set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `DATABASE_DRIVER`.
