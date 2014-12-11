\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';


-- add milepoint column to geo_loc
ALTER TABLE iris.geo_loc ADD COLUMN milepoint VARCHAR(16);

-- copy cabinet mile data to geo_loc milepoint
UPDATE iris.geo_loc AS gl
	SET milepoint = c.mile
	FROM iris.cabinet AS c
	WHERE gl.name = c.geo_loc;

-- redefine cabinet_view without cabinet.mile
DROP VIEW cabinet_view;
CREATE VIEW cabinet_view AS
	SELECT name, style, geo_loc
	FROM iris.cabinet;
GRANT SELECT ON cabinet_view TO PUBLIC;

-- redefine controller_report without cabinet.mile
DROP VIEW controller_report;
CREATE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, cab.geo_loc,
	trim(l.roadway || ' ' || l.road_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	cab.style AS "type", d.name AS device, d.pin,
	d.cross_loc, d.corridor, c.notes
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

-- drop mile column from cabinet
ALTER TABLE iris.cabinet DROP COLUMN mile;
