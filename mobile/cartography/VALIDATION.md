# Cartography validation — 2026-11-27
Source artifact FDK-cartography-candidates, OSM replication 4986, 2026-11-26T21:21:01Z. ZIP CRC check passed. 56,696 GeoJSON sequence records, 442 toll point candidates (191 gantries, 251 booths), 317 main-road tunnel segments. CSV: no duplicate IDs or coordinate pairs, no missing coordinates, 204 missing names, 303 missing references. 423 candidates lack operator tags. Tunnel segments: 288 oneway=yes, 25 no, 3 missing, 1 alternating. Segments are not complete tunnel routes.

## Release gates (pending)
- Cross-check each activated gantry against official concession/MOP source, direction, road geometry and vehicle category; exclude manual booths from electronic detection unless verified.
- Reconstruct tunnel topology including surface alternatives; preserve OSM node connectivity in future extraction, do not infer exclusivity from proximity alone.
- Verify effective tariffs and entry/exit pricing where applicable.
- Run rollback-contained integration tests for forward/reverse crossings, parallel roads, multiple gantries, duplicates, delayed GPS, tunnel ambiguity, missing tariff, permissions and reconciliation.
- Version candidate catalog separately; no production activation until verification. Preserve historical crossings and prior active catalog.
- No APK release on the basis of this audit alone. Official cross-check and database integration tests remain pending.
