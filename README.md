# fcnami-lyrics-site
A personal Japanese song lyrics translation platform (FCNami T_T) featuring Kanji, Romaji, and Thai translations with structured song management, request system, and developer-driven tools.

## Backend

The backend lives in backend/ and starts as a Spring Boot API for the v1 lyrics catalog, admin song editing, and Google Sheet backed request queue.

`powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
`

Useful endpoints:

- GET /api/songs
- GET /api/songs/{slug}
- POST /api/admin/songs
- PUT /api/admin/songs/{id}
- GET /api/queue?search=&status=&tier=
- POST /api/queue/sync

Runtime config is environment-driven. Local development defaults to in-memory H2; production can set DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD, and DATABASE_DRIVER.

## Frontend

The frontend lives in frontend/ and is a Vite + React app for the public lyrics archive, request queue, request instructions, and channel/about pages.

`powershell
cd frontend
pnpm install
pnpm run dev
pnpm run build
`

The dev server proxies /api to http://localhost:8080. If the backend is not running, the UI falls back to demo data so the public flow remains reviewable.
