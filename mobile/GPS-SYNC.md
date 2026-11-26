# GPS synchronization — verified database contract (2026-11-25)
Read-only inspection of existing Supabase confirmed gps_positions has shift_id, staff_id, vehicle_id, latitude, longitude, speed_kmh and recorded_at (timestamptz). No accuracy or client event identifier column currently exists. Existing INSERT policy requires own staff and matching active shift; SELECT permits manager or self. Historical replay after shift closure is therefore not supported by the current policy.
## Required implementation
- Add a nullable unique client event UUID for idempotency without altering existing rows.
- Provide authenticated replay RPC deriving staff and vehicle from the shift, checking ownership and timestamp bounds against started_at and ended_at (or server now for active shifts). Do not broaden existing direct INSERT policy. Validate coordinate ranges, finite values and batch size. Explicitly revoke anonymous execution.
- Native queue must persist event UUID and capture time, acknowledge only confirmed server acceptance, retain on transient errors, and isolate accounts. Never store service-role keys in client code.
- Test duplicate replay, foreign shift, closed shift in/out of bounds, anonymous access and network loss before enabling production replay.
- Current Android service stores JSONL locally but has no drain/ack or authenticated transport; bridge starts/stops service but has not been compiled. iOS native remains unimplemented.
No schema or data changes were made in this inspection. Acceptance remains unchecked.
