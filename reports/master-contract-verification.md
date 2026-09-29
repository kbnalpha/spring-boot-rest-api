# Complete master API verification

Verified against `source/all master api with response.docx` on 2026-09-29.

- All 24 unique source routes exist in the generated OpenAPI definition. The employee and contract-employee create workflows share `/api/User/CreateEmployee`.
- `MYSQL_MIGRATION_TEST=true mvn -q verify`: 20 tests passed, zero failures/errors/skips. Includes H2 migration replay, actual MySQL schema validation, RBAC, source permission compatibility, persistence, and validation.
- Live MySQL verification: 87 HTTP requests passed, covering all 47 OpenAPI operations. See [requests and responses](api-smoke-results.json) and [results](api-smoke-results.md).
- [Executable HTTP examples](../docs/master-api-tests.http): all 35 requests passed when their payloads and JavaScript response handlers were executed sequentially against the verification server. This validates the file contents and ID handoffs; the IntelliJ UI itself was not automated.
- Liquibase: 37 applied changesets, 22 application tables plus 2 history/lock tables. Migrations 001–004 were preserved; additions are 005 and 006.
- The source permission tree contains 129 ID/name entries. Explicit aliases preserve existing canonical IDs and grants, including source IDs that conflict with existing permissions.

Synthetic `EHS-SMOKE-` and `EHS-TEST-` records are retained for inspection. The verification server used port 18080 and was stopped afterward. Restart the normal application to load the updated code. MySQL migrations have already been applied to `ehs_db`.
