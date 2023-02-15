define([], function () {
    return {
        "nodes": [{
            "id": 1,
            "stereotype": "person",
            "name": "Charles IV, Holy Roman Emperor",
            "begin": "1346-01-01T00:00:00.000+00:00",
            "end": "1378-01-01T00:00:00.000+00:00",
            "properties": {"endPrecision": "day", "startPrecision": "day"}
        }, {
            "id": 4,
            "stereotype": "person",
            "name": "John of Bohemia",
            "begin": "1310-01-01T00:00:00.000+00:00",
            "end": "1346-01-01T00:00:00.000+00:00",
            "properties": {"endPrecision": "day", "startPrecision": "day"}
        }, {
            "id": 7,
            "stereotype": "person",
            "name": "Henry of Bohemia",
            "begin": "1307-01-01T00:00:00.000+00:00",
            "end": "1310-01-01T00:00:00.000+00:00",
            "properties": {"endPrecision": "day", "startPrecision": "day"}
        }, {
            "id": 10,
            "stereotype": "person",
            "name": "Rudolf I of Bohemia",
            "begin": "1306-01-01T00:00:00.000+00:00",
            "end": "1306-01-01T00:00:00.000+00:00",
            "properties": {"endPrecision": "day", "startPrecision": "day"}
        }, {
            "id": 13,
            "stereotype": "person",
            "name": "Wenceslaus III of Bohemia",
            "begin": "1301-01-01T00:00:00.000+00:00",
            "end": "1305-01-01T00:00:00.000+00:00",
            "properties": {"endPrecision": "day", "startPrecision": "day"}
        }, {
            "id": 16,
            "stereotype": "person",
            "name": "Wenceslaus II of Bohemia",
            "begin": "1278-01-01T00:00:00.000+00:00",
            "end": "1305-01-01T00:00:00.000+00:00",
            "properties": {"endPrecision": "day", "startPrecision": "day"}
        }, {
            "id": 19,
            "stereotype": "person",
            "name": "Ottokar II of Bohemia",
            "begin": "1253-01-01T00:00:00.000+00:00",
            "end": "1278-01-01T00:00:00.000+00:00",
            "properties": {"endPrecision": "day", "startPrecision": "day"}
        }, {
            "id": 20,
            "stereotype": "person",
            "name": "Wenceslaus I of Bohemia",
            "begin": "1230-01-01T00:00:00.000+00:00",
            "end": "1253-01-01T00:00:00.000+00:00",
            "properties": {"endPrecision": "day", "startPrecision": "day"}
        }, {
            "id": 21,
            "stereotype": "person",
            "name": "Ottokar I of Bohemia",
            "begin": "1192-01-01T00:00:00.000+00:00",
            "end": "1193-01-01T00:00:00.000+00:00",
            "properties": {"endPrecision": "day", "startPrecision": "day"}
        }, {
            "id": 22,
            "stereotype": "person",
            "name": "Ottokar I of Bohemia",
            "begin": "1192-01-01T00:00:00.000+00:00",
            "end": "1193-01-01T00:00:00.000+00:00",
            "properties": {"endPrecision": "day", "startPrecision": "day"}
        }], "edges": [{"id": 1, "stereotype": "relationship", "from": 1, "to": 4, "name": "predecessor"}, {"id": 2, "stereotype": "relationship", "from": 4, "to": 7, "name": "predecessor"}, {
            "id": 3, "stereotype": "relationship", "from": 7, "to": 10, "name": "predecessor"
        }, {"id": 4, "stereotype": "relationship", "from": 10, "to": 13, "name": "predecessor"}, {"id": 5, "stereotype": "relationship", "from": 13, "to": 16, "name": "predecessor"}, {
            "id": 6, "stereotype": "relationship", "from": 16, "to": 19, "name": "predecessor"
        }, {"id": 7, "stereotype": "relationship", "from": 19, "to": 20, "name": "predecessor"}, {"id": 8, "stereotype": "relationship", "from": 20, "to": 21, "name": "predecessor"}, {
            "id": 9, "stereotype": "relationship", "from": 22, "to": 25, "name": "predecessor"
        }]
    };
});
