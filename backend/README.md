# AI Design Platform Backend

Spring Boot backend for the AI-assisted architectural and landscape design workflow platform.

## Requirements

- Java 21
- Maven 3.9+
- MySQL 8

## Local configuration

Configure the values shown in `.env.example` as environment variables. The application no longer stores database passwords or JWT secrets in source control.

Required variables:

- `DB_PASSWORD`
- `JWT_SECRET` with at least 32 characters

Optional variables include `DB_URL`, `DB_USERNAME`, `APP_CORS_ALLOWED_ORIGINS`, `APP_LOG_LEVEL`, and `JWT_EXPIRATION_SECONDS`.

PowerShell example:

```powershell
$env:DB_PASSWORD = "<your-password>"
$env:JWT_SECRET = "<at-least-32-random-characters>"
mvn spring-boot:run
```

Initialize the database in the order documented in `docs/数据库脚本执行顺序.md`.

## Verification

```powershell
mvn clean test
```

List endpoints remain backward compatible. When `pageNum` or `pageSize` is supplied to the main case, prompt, tool, review, user-case, or workflow-instance list endpoints, `data` becomes:

```json
{
  "records": [],
  "total": 0,
  "pageNum": 1,
  "pageSize": 20,
  "pages": 0
}
```

Without pagination parameters, the original list response is retained.

## Experiment accounts and exports

Administrators can create anonymous experiment accounts through:

- `POST /api/admin/experiment-users/batch`
- `GET /api/admin/experiment-users`
- `PUT /api/admin/experiment-users/{id}/status`
- `POST /api/admin/experiment-users/{id}/reset-password`

CSV exports are available under `/api/admin/exports/*.csv`. They support experiment batch, group, and date filters.

Workflow step iteration results are exported from `GET /api/admin/exports/workflow-iterations.csv`.
Each row contains the external tool, prompt, result, four quality scores, improvement note, and selected status.

Allowed usage event types are:

`login`, `view_tool`, `select_workflow_template`, `render_prompt`, `copy_prompt`,
`complete_workflow_step`, `submit_tool_rating`, `submit_workflow_rating`, and `submit_survey`.

## External AI result iterations

The backend does not call external AI APIs. Authenticated users can record and compare results produced in external tools:

- `POST /api/workflow-instances/{id}/steps/{nodeId}/iterations`
- `GET /api/workflow-instances/{id}/steps/{nodeId}/iterations`
- `PUT /api/workflow-instances/{id}/steps/{nodeId}/iterations/{iterationId}/select`

An iteration records the tool, prompt, output or result URL, effect, accuracy, controllability, usability, and the next improvement note. Only one iteration per workflow node is selected as the final result.

## Flyway transition

Flyway is enabled by default. Existing non-empty databases are baselined at version 26, then versioned migrations are applied. Empty databases must first run the 26 historical scripts in `docs/数据库脚本执行顺序.md`; Docker Compose performs that initialization automatically.

## Real project cases

The public case library is seeded from verified project materials under the workspace-level `assets` directory. Large originals remain there for archival use. Web-ready images and downloadable process PDFs are served by the backend:

```text
GET /assets/cases/{case-folder}/{file}
```

The database stores only relative URLs. `GET /api/cases/{id}` returns:

- `assets`: optimized boards and process documents
- `toolUsages`: the evidence-backed tool sequence, stage, and purpose

Prompt details include `sourceDesc` so original project prompts can be distinguished from reconstructed or demo content. Run `scripts/generate_case_assets.py` with the bundled Python/Pillow runtime whenever the original boards are replaced.

## Docker deployment

1. Copy `.env.example` to `.env` and replace every secret and public origin.
2. Build the independent frontend and set `FRONTEND_DIST` to its `dist` directory.
3. Run `docker compose up -d --build`.
4. Confirm `GET /actuator/health` returns `UP`.
5. Remove `ADMIN_BOOTSTRAP_PASSWORD` after the administrator has been created.

The default Nginx configuration serves HTTP. For public deployment, install a valid certificate and replace `deploy/nginx/default.conf` with the provided HTTPS template.

Daily database backup:

```sh
./scripts/backup-mysql.sh
```

Restore into the running MySQL container:

```sh
./scripts/restore-mysql.sh ./backups/ai_design_platform_YYYYMMDD_HHMMSS.sql.gz
```
